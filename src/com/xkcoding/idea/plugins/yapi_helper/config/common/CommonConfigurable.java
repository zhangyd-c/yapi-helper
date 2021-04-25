package com.xkcoding.idea.plugins.yapi_helper.config.common;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import com.xkcoding.idea.plugins.yapi_helper.store.GlobalVariable;

import javax.swing.*;

/**
 * @author lmx 2021/1/20 20:15
 **/

public class CommonConfigurable implements Configurable {

    private Project project;
    private CommonConfigurableUI commonConfigurableUI;

    public CommonConfigurable(Project project) {
        this.project = project;
        GlobalVariable.getInstance().setProject(project);
//        oldState = ServiceManager.getService(project, CURLSettingState.class);
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "common";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        commonConfigurableUI = new CommonConfigurableUI();
        return commonConfigurableUI.getPanel();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }
}
