package com.xkcoding.idea.plugins.yapi_helper.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.TextTransferable;
import org.jetbrains.annotations.NotNull;
import com.xkcoding.idea.plugins.yapi_helper.curl.CurlUtils;
import com.xkcoding.idea.plugins.yapi_helper.util.NotificationUtil;

/**
 * @author lmx 2020/11/11 14:19
 */

public class CopyIPAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Project project = actionEvent.getProject();
        String baseApi = CurlUtils.getRealIP();
        CopyPasteManager.getInstance().setContents(new TextTransferable(baseApi));
        NotificationUtil.infoNotify("Native IP address：", baseApi, project);
    }

}
