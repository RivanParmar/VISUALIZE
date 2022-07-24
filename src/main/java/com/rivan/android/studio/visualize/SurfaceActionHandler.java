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

import com.intellij.ide.CopyProvider;
import com.intellij.ide.CutProvider;
import com.intellij.ide.DeleteProvider;
import com.intellij.ide.PasteProvider;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.ide.CopyPasteManager;
import org.jetbrains.annotations.NotNull;

public class SurfaceActionHandler implements DeleteProvider, CutProvider, CopyProvider, PasteProvider {

    protected final VisualEditorSurface<?> surface;
    private CopyPasteManager copyPasteManager;

    public SurfaceActionHandler(@NotNull VisualEditorSurface<?> surface) {
        this(surface, CopyPasteManager.getInstance());
    }

    protected SurfaceActionHandler(@NotNull VisualEditorSurface<?> surface, @NotNull CopyPasteManager copyPasteManager) {
        this.surface = surface;
        this.copyPasteManager = copyPasteManager;
    }

    @Override
    public void performCopy(@NotNull DataContext dataContext) {
        //if (!surface.getSelectionModel().isEmpty()) {
            //copyPasteManager.setContents(surface.getSelectionAsTransferable());
        //}
    }

    @Override
    public boolean isCopyEnabled(@NotNull DataContext dataContext) {
        return false;
    }

    @Override
    public boolean isCopyVisible(@NotNull DataContext dataContext) {
        return true;
    }

    @Override
    public void performCut(@NotNull DataContext dataContext) {

    }

    @Override
    public boolean isCutEnabled(@NotNull DataContext dataContext) {
        return false;
    }

    @Override
    public boolean isCutVisible(@NotNull DataContext dataContext) {
        return true;
    }

    @Override
    public void deleteElement(@NotNull DataContext dataContext) {

    }

    @Override
    public boolean canDeleteElement(@NotNull DataContext dataContext) {
        return false;
    }

    @Override
    public void performPaste(@NotNull DataContext dataContext) {

    }

    @Override
    public boolean isPastePossible(@NotNull DataContext dataContext) {
        return false;
    }

    @Override
    public boolean isPasteEnabled(@NotNull DataContext dataContext) {
        return false;
    }

    /*private boolean hasNonEmptySelection() {
        return !surface.getSelectionModel().isEmpty();
    }*/
}
