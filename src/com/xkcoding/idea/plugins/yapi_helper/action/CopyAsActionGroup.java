package com.xkcoding.idea.plugins.yapi_helper.action;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.xkcoding.idea.plugins.yapi_helper.icons.SdkIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author lmx 2020/12/22 14:52
 */

public class CopyAsActionGroup extends DefaultActionGroup {

    @Override
    public void update(@NotNull AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        boolean enabled = false;
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if (editor != null && null != psiFile && (psiFile.getFileType() instanceof JavaFileType)) {
            enabled = true;
        }
        // 是否显示该Action
        event.getPresentation().setEnabledAndVisible(enabled);
        // 设置该Action图标
        event.getPresentation().setIcon(SdkIcons.Logo);
    }
}
