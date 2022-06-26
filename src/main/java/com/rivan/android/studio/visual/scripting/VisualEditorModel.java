package com.rivan.android.studio.visual.scripting;

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

    private final MergingUpdateQueue updateQueue;

    private boolean disposed;

    private final BiFunction<Project, VirtualFile, PsiJavaFile> javaFileProvider;

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

        return false;
    }

    public void updateTheme() {
        ResourceUrl themeUrl = ResourceUrl.parse(configuration.getTheme());
        if (themeUrl != null && themeUrl.type == ResourceType.STYLE) {
            Disposable computationToken = Disposer.newDisposable();
            Disposer.register(this, computationToken);
        }
    }

    @Slow
    private void updateTheme(@NotNull ResourceUrl themeUrl, @NotNull Disposable computationToken) {

        try {
            ResourceResolver resolver = getResourceResolver();
            if (resolver.getTheme(themeUrl.name, themeUrl.isFramework()) == null) {
                String theme = configuration.getConfigurationManager().computePreferredTheme(configuration);

                ApplicationManager.getApplication().invokeLater(() -> configuration.setTheme(theme), a -> disposed);
            }
        } finally {

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

    public boolean deactivate(@NotNull Object source) {
        return false;
    }

    @NotNull
    public VirtualFile getFile() {
        return file;
    }

    @NotNull
    private static PsiJavaFile getDefaultJavaFile(Project project, VirtualFile virtualFile) {
        PsiJavaFile file = (PsiJavaFile) AndroidPsiUtils.getPsiFileSafely(project, virtualFile);
        return verifyNotNull(file);
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
