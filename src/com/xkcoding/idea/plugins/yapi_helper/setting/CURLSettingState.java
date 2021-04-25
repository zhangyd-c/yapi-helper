package com.xkcoding.idea.plugins.yapi_helper.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.xkcoding.idea.plugins.yapi_helper.curl.enums.ArrayFormatEnum;
import com.xkcoding.idea.plugins.yapi_helper.curl.model.CURLModuleInfo;
import com.xkcoding.idea.plugins.yapi_helper.curl.model.FetchConfig;
import com.xkcoding.idea.plugins.yapi_helper.model.FilterFieldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lmx 2020/11/11 18:01
 */
@State(name = "com.xkcoding.idea.plugins.yapi_helper.setting.CURLSettingState",
        storages = {@Storage("ApiGeneratorPlusPlugin.xml")}
)
public class CURLSettingState implements PersistentStateComponent<CURLSettingState> {

    public String baseApi = "";

    public List<CURLModuleInfo> moduleInfoList = new ArrayList<>();

    public List<Module> modules;

    /**
     * qs.stringify({ a: ['b', 'c'] }, { arrayFormat: 'indices' })
     * // 'a[0]=b&a[1]=c'
     * qs.stringify({ a: ['b', 'c'] }, { arrayFormat: 'brackets' })
     * // 'a[]=b&a[]=c'
     * qs.stringify({ a: ['b', 'c'] }, { arrayFormat: 'repeat' })
     * // 'a=b&a=c'
     * qs.stringify({ a: ['b', 'c'] }, { arrayFormat: 'comma' })
     */
    public String arrayFormat = ArrayFormatEnum.repeat.name();

    public FilterFieldInfo filterFieldInfo = new FilterFieldInfo();

    public FetchConfig fetchConfig = new FetchConfig();

    @Nullable
    @Override
    public CURLSettingState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull CURLSettingState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
