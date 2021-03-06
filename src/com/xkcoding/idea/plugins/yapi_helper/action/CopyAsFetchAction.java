package com.xkcoding.idea.plugins.yapi_helper.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import com.xkcoding.idea.plugins.yapi_helper.curl.CurlUtils;
import com.xkcoding.idea.plugins.yapi_helper.exception.BizException;
import com.xkcoding.idea.plugins.yapi_helper.store.GlobalVariable;

/**
 * reference:
 * 使用 Fetch: https://developer.mozilla.org/zh-CN/docs/Web/API/Fetch_API/Using_Fetch
 * WorkerOrGlobalScope.fetch(): https://developer.mozilla.org/zh-CN/docs/Web/API/WindowOrWorkerGlobalScope/fetch
 */
public class CopyAsFetchAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        try {
//            Editor editor = actionEvent.getDataContext().getData(CommonDataKeys.EDITOR);
//            PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
            Project project = actionEvent.getProject();
            GlobalVariable.getInstance().setProject(project);
            CurlUtils curlUtils = new CurlUtils();
            curlUtils.copyAsFetch(actionEvent);
        } catch (BizException exception) {
            exception.printStackTrace();
        }
    }

}
