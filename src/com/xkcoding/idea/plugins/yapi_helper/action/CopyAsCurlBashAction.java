package com.xkcoding.idea.plugins.yapi_helper.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import com.xkcoding.idea.plugins.yapi_helper.constant.CUrlClientType;
import com.xkcoding.idea.plugins.yapi_helper.curl.CurlUtils;
import com.xkcoding.idea.plugins.yapi_helper.exception.BizException;
import com.xkcoding.idea.plugins.yapi_helper.store.GlobalVariable;
import com.xkcoding.idea.plugins.yapi_helper.util.NotificationUtil;

/**
 * @author lmx 2020/11/11 14:19
 */

public class CopyAsCurlBashAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        try {
//            Editor editor = actionEvent.getDataContext().getData(CommonDataKeys.EDITOR);
//            PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
            Project project = actionEvent.getProject();
            GlobalVariable.getInstance().setProject(project);
//        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
//        PsiClass selectedClass = PsiTreeUtil.getContextOfType(referenceAt, PsiClass.class);
//        if (selectedClass == null) {
//            NotificationUtil.errorNotify("this operate only support in class file", project);
//            return;
//        }
            CurlUtils curlUtils = new CurlUtils();
            curlUtils.copyAsCUrl(actionEvent, CUrlClientType.BASH);
        } catch (BizException e) {
            e.printStackTrace();
            if (StringUtils.isNotBlank(e.getMessage())) {
                NotificationUtil.errorNotify(e.getMessage());
            }
        }
    }

}
