package com.xkcoding.idea.plugins.yapi_helper.setting;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author lmx 2020/11/11 17:44
 */

public class GroupSettingConfigurable implements Configurable {
    @Override
    public String getDisplayName() {
        return "YApi Helper";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return null;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }
}
