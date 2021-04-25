package com.xkcoding.idea.plugins.yapi_helper.store;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.xkcoding.idea.plugins.yapi_helper.config.YApiHelperConfig;
import lombok.Data;

/**
 * @author lmx 2021/1/13 22:03
 **/
@Data
public class GlobalVariable {

    private Project project;

    private YApiHelperConfig YApiHelperConfig;

    private static GlobalVariable globalVariable = new GlobalVariable();

    public static GlobalVariable getInstance() {
        return globalVariable;
    }

    public static YApiHelperConfig getApiConfig() {
        if (globalVariable.getYApiHelperConfig() != null) {
            return globalVariable.getYApiHelperConfig();
        }
        return ServiceManager.getService(globalVariable.getProject(), YApiHelperConfig.class);
    }

    public static GlobalVariable setProject(Project project){
        globalVariable.project = project;
        return globalVariable;
    }

}
