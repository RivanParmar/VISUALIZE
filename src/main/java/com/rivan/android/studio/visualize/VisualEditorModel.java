/*
 * Copyright 2022 Rivan Parmar
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.rivan.android.studio.visualize;

import com.android.annotations.concurrency.Slow;
import com.android.ide.common.resources.ResourceResolver;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.resources.ResourceType;
import com.android.resources.ResourceUrl;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.devices.Device;
import com.android.tools.idea.AndroidPsiUtils;
import com.android.tools.idea.configurations.Configuration;
import com.android.tools.idea.configurations.ResourceResolverCache;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.util.Alarm;
import com.intellij.util.ui.update.MergingUpdateQueue;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static com.google.common.base.Verify.verifyNotNull;

public class VisualEditorModel implements Disposable, ModificationTracker {

    public static final int DELAY_AFTER_TYPING_MS = 250;

    @NotNull private final AndroidFacet facet;
    @NotNull private final VirtualFile file;

    @NotNull private final Configuration configuration;

    /** Model name. This can be used when multiple models are displayed at the same time */
    @Nullable private String modelDisplayName;
    /** Text to display when displaying a tooltip related to this model */
    @Nullable private String modelTooltip;

    private final long id;
    private final Set<Object> activations = Collections.newSetFromMap(new WeakHashMap<>());

    private final MergingUpdateQueue updateQueue;

    private final @NotNull AtomicReference<Disposable> themeUpdateComputation = new AtomicReference<>();
    private boolean disposed;

    private final BiFunction<Project, VirtualFile, PsiJavaFile> javaFileProvider;

    protected VisualEditorModel(@Nullable Disposable parent,
                                @Nullable String modelDisplayName,
                                @Nullable String modelTooltip,
                                @NotNull AndroidFacet facet,
                                @NotNull VirtualFile file,
                                @NotNull Configuration configuration) {
        this(parent, modelDisplayName, modelTooltip, facet, file, configuration, VisualEditorModel::getDefaultJavaFile);
    }

    @VisibleForTesting
    protected VisualEditorModel(@Nullable Disposable parent,
                                @Nullable String modelDisplayName,
                                @Nullable String modelTooltip,
                                @NotNull AndroidFacet facet,
                                @NotNull VirtualFile file,
                                @NotNull Configuration configuration,
                                @NotNull BiFunction<Project, VirtualFile, PsiJavaFile> javaFileProvider) {
        this.facet = facet;
        this.javaFileProvider = javaFileProvider;
        this.modelDisplayName = modelDisplayName;
        this.modelTooltip = modelTooltip;
        this.file = file;
        this.configuration = configuration;

        id = System.nanoTime() ^ file.getName().hashCode();
        if (parent != null) {
            Disposer.register(parent, this);
        }

        updateQueue = new MergingUpdateQueue("visual.editor.preview", DELAY_AFTER_TYPING_MS,
                true, null, this, null, Alarm.ThreadToUse.SWING_THREAD);
        updateQueue.setRestartTimerOnAdd(true);
    }

    @NotNull
    @VisibleForTesting
    public MergingUpdateQueue getUpdateQueue() {
        return updateQueue;
    }

    public boolean activate(@NotNull Object source) {
        if (getFacet().isDisposed()) {
            return false;
        }

        boolean wasActive;
        synchronized (activations) {
            wasActive = !activations.isEmpty();
            activations.add(source);
        }

        if (!wasActive) {
            // This was the first activation so enable listeners

            return true;
        } else {
            return false;
        }
    }

    public void updateTheme() {
        ResourceUrl themeUrl = ResourceUrl.parse(configuration.getTheme());
        if (themeUrl != null && themeUrl.type == ResourceType.STYLE) {
            Disposable computationToken = Disposer.newDisposable();
            Disposer.register(this, computationToken);
            Disposable oldComputation = themeUpdateComputation.getAndSet(computationToken);
            if (oldComputation != null) {
                Disposer.dispose(oldComputation);
            }
        }
    }

    @Slow
    private void updateTheme(@NotNull ResourceUrl themeUrl, @NotNull Disposable computationToken) {
        if (themeUpdateComputation.get() != computationToken) {
            return; // A new update has already been scheduled
        }

        try {
            ResourceResolver resolver = getResourceResolver();
            if (resolver.getTheme(themeUrl.name, themeUrl.isFramework()) == null) {
                String theme = configuration.getConfigurationManager().computePreferredTheme(configuration);
                if (themeUpdateComputation.get() != computationToken) {
                    return; // A new update has already been scheduled
                }
                ApplicationManager.getApplication().invokeLater(() -> configuration.setTheme(theme), a -> disposed);
            }
        } finally {
            if (themeUpdateComputation.compareAndSet(computationToken, null)) {
                Disposer.dispose(computationToken);
            }
        }
    }

    @Slow
    @NotNull
    private ResourceResolver getResourceResolver() {
        String theme = configuration.getTheme();
        Device device = configuration.getDevice();
        ResourceResolverCache resolverCache = configuration.getConfigurationManager().getResolverCache();
        FolderConfiguration config = configuration.getFullConfig();
        if (device != null && Configuration.CUSTOM_DEVICE_ID.equals(device.getId())) {
            // Remove the old custom device configuration only if it's different from the new one
            resolverCache.replaceCustomConfig(theme, config);
        }
        IAndroidTarget target = configuration.getTarget();
        return resolverCache.getResourceResolver(target, theme, config);
    }

    private void deactivate() {

    }

    /**
     * Notify model that it's not active. This means it can stop watching for events etc. It may be activated again in the future.
     *
     * @param source the source is used to keep track of the references that are using this model. Only when all the sources have called
     *               deactivate(Object), the model will be really deactivated.
     * @return true if the model was active before and was deactivated.
     */
    public boolean deactivate(@NotNull Object source) {
        boolean shouldDeactivate;
        synchronized (activations) {
            boolean removed = activations.remove(source);
            // If there are no more deactivations, call the private #deactivate()
            shouldDeactivate = removed && activations.isEmpty();
        }

        if (shouldDeactivate) {
            deactivate();
            return true;
        } else {
            return false;
        }
    }

    @NotNull
    public VirtualFile getVirtualFile() {
        return file;
    }

    @NotNull
    private static PsiJavaFile getDefaultJavaFile(Project project, VirtualFile virtualFile) {
        PsiJavaFile file = (PsiJavaFile) AndroidPsiUtils.getPsiFileSafely(project, virtualFile);
        return verifyNotNull(file);
    }

    @NotNull
    public PsiJavaFile getFile() {
        return javaFileProvider.apply(getProject(), file);
    }

    public long getId() {
        return id;
    }

    @NotNull
    public AndroidFacet getFacet() {
        return facet;
    }

    @NotNull
    public Module getModule() {
        return facet.getModule();
    }

    @NotNull
    public Project getProject() {
        return getModule().getProject();
    }

    @NotNull
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void dispose() {
        disposed = true;
        boolean shouldDeactivate;

        synchronized (activations) {
            // If there are no activations left, make sure we deactivate the model correctly
            shouldDeactivate = !activations.isEmpty();
            activations.clear();
        }

        if (shouldDeactivate) {
            deactivate(); // ensure listeners are unregistered if necessary
        }
    }

    @Override
    public long getModificationCount() {
        return 0;
    }

    public void setModelDisplayName(@Nullable String name) {
        modelDisplayName = name;
    }

    @Nullable
    public String getModelDisplayName() {
        return modelDisplayName;
    }

    @Nullable
    public String getModelTooltip() {
        return modelTooltip;
    }

    @NotNull
    @Override
    public String toString() {
       return VisualEditorModel.class.getSimpleName() + "for" + file;
    }
}
