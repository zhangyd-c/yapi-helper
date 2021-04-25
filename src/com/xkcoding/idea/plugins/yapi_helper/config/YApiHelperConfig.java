package com.xkcoding.idea.plugins.yapi_helper.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.xkcoding.idea.plugins.yapi_helper.model.FilterFieldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


@State(name = "YApiHelperConfig",
        storages = {@Storage("YApiHelperPlugin.xml")}
)
public class YApiHelperConfig implements PersistentStateComponent<YApiHelperConfig> {

//    public Set<String> excludeFieldNames = new HashSet<>();
    public String excludeFields = "serialVersionUID";
    public String excludeAnnotations = "@CurrentId,@CurrentUserId;@CurrentMerchantId,";
//    public Set<String> excludeAnnotationNames = new HashSet<>();
    public String dirPath = "";
    public String prefix = "└";
    public Boolean cnFileName = false;
    public Boolean overwrite = true;

    public String yApiServerUrl = "";
    public String projectToken = "";
    public String projectId = "";
    public Boolean autoCat = false;
    public Boolean apiDone = true;
    public String defaultCat = "yapi_helper";
    public Boolean ignoreResponse = false;
    public String tag = "";
//    public Set<String> tags = new HashSet<>();

    public Boolean isMultiModule = false;
    public Boolean isUseDefaultToken = false;
    public Boolean matchWithModuleName = false;
    public List<YApiProjectConfigInfo> yApiProjectConfigInfoList = new ArrayList<>();
    public FilterFieldInfo filterFieldInfo = new FilterFieldInfo();


    @Nullable
    @Override
    public YApiHelperConfig getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull YApiHelperConfig state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
