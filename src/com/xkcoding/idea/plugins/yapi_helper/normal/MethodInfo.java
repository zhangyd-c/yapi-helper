package com.xkcoding.idea.plugins.yapi_helper.normal;

import com.intellij.lang.Language;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocTagValue;
import com.intellij.psi.util.PsiUtil;
import com.xkcoding.idea.plugins.yapi_helper.constant.WebAnnotation;
import com.xkcoding.idea.plugins.yapi_helper.http.MediaType;
import com.xkcoding.idea.plugins.yapi_helper.util.DesUtil;
import com.xkcoding.idea.plugins.yapi_helper.util.FieldUtil;
import com.xkcoding.idea.plugins.yapi_helper.util.MethodUtil;
import com.xkcoding.idea.plugins.yapi_helper.yapi.enums.RequestMethodEnum;
import com.xkcoding.idea.plugins.yapi_helper.yapi.model.YApiInterface;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;

@Data
public class MethodInfo implements Serializable {
    private static final long serialVersionUID = -9143203778013000538L;

    private static final String SLASH = "/";

    private String title;
    private String desc;
    private String packageName;
    private String className;

    // 返回类型文本
    private String returnStr;
    private String paramStr;
    private String methodName;
    private List<FieldInfo> requestFields;
    private List<FieldInfo> responseFields;
    private FieldInfo response;
    private PsiMethod psiMethod;
    private MediaType mediaType;
    private YApiInterface yApiInterface;

    private Language language;
    private RequestMethodEnum requestMethod;
    private String methodPath;
    private String classPath;
    private String funStr;
    private List<String> classAnnotationTexts;

    private List<String> excludeParamTypes = Arrays.asList("RedirectAttributes", "HttpServletRequest", "HttpServletResponse");

    public MethodInfo(PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
        this.language = psiMethod.getLanguage();
        this.mediaType = MethodUtil.getMediaType(psiMethod);
        this.setFunStr(psiMethod.getText());
        this.setRequestMethod(MethodUtil.getRequestMethod(psiMethod.getText()));
        this.setMethodPath(extraMethodPath(psiMethod));
        this.setClassPath(extraClassPath(psiMethod));
        this.setDesc(DesUtil.getInterfaceDesc(psiMethod));
        this.setTitle(DesUtil.getInterfaceTitle(psiMethod));
        PsiClass psiClass = psiMethod.getContainingClass();
        if (psiClass == null) {
            return;
        }
        this.setPackageName(PsiUtil.getName(psiClass));
        this.setClassName(psiClass.getName());

        List<String> classAnnotationTexts = new ArrayList<>();
        for (PsiAnnotation annotation : psiClass.getAnnotations()) {
            classAnnotationTexts.add(annotation.getText());
        }
        this.setClassAnnotationTexts(classAnnotationTexts);

        this.setParamStr(psiMethod.getParameterList().getText());
        this.setMethodName(psiMethod.getName());
        this.setRequestFields(listParamFieldInfos(psiMethod));
        PsiType returnType = psiMethod.getReturnType();
        if (returnType != null) {
            this.setReturnStr(returnType.getPresentableText());
            if (!"void".equals(psiMethod.getReturnType().getPresentableText())) {
                FieldInfo fieldInfo = new FieldInfo(psiMethod.getProject(), psiMethod.getReturnType(),
                        getReturnDesc(psiMethod.getDocComment()));
                this.response = fieldInfo;
                this.setResponseFields(fieldInfo.getChildren());
            }
        }
        yApiInterface = buildDocYApiInterface(psiMethod.getDocComment());
        System.out.println();
    }

    private YApiInterface buildDocYApiInterface(PsiDocComment docComment) {
        if (null == docComment) {
            return null;
        }
        YApiInterface yApiInterface = new YApiInterface();
        PsiDocTag[] tags = docComment.getTags();
        for (PsiDocTag tag : tags) {
            if ("res_body".equals(tag.getName())) {
//                StringUtil.testTag(tag);
                yApiInterface.setRes_body(tag.getText().replace("@res_body", "").replaceAll("\n *\\*", "\n"));
            }
            if ("res_body_type".equals(tag.getName())) {
//                StringUtil.testTag(tag);
                PsiDocTagValue valueElement = tag.getValueElement();
                String type = null;
                if (valueElement != null) {
                    type = valueElement.getText();
                }
                yApiInterface.setRes_body_type(type);
            }
            if ("res_body_is_json_schema".equals(tag.getName())) {
//                StringUtil.testTag(tag);
                PsiDocTagValue valueElement = tag.getValueElement();
                Boolean aBoolean = null;
                if (valueElement != null) {
                    try {
                        aBoolean = Boolean.parseBoolean(valueElement.getText());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                yApiInterface.setRes_body_is_json_schema(aBoolean);
            }
        }
        return yApiInterface;
    }

    public boolean containRequestBodyAnnotation() {
        return funStr.contains(WebAnnotation.RequestBody);
    }

    public boolean containResponseBodyAnnotation() {
        return funStr.contains(WebAnnotation.ResponseBody);
    }

    public boolean containRestControllerAnnotation() {
        for (String annotationText : classAnnotationTexts) {
            if (annotationText.contains(WebAnnotation.RestController)) {
                return true;
            }
        }
        return false;
    }

    public boolean containControllerAnnotation() {
        for (String annotationText : classAnnotationTexts) {
            if (annotationText.contains(WebAnnotation.Controller)) {
                return true;
            }
        }
        return false;
    }

    private List<FieldInfo> listParamFieldInfos(PsiMethod psiMethod) {
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        Map<String, String> paramNameDescMap = getParamDescMap(psiMethod.getDocComment());
        PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
        List<PsiParameter> psiParameterList = FieldUtil.filterParameters(psiParameters);
        for (PsiParameter psiParameter : psiParameterList) {
            PsiType psiType = psiParameter.getType();
            FieldInfo fieldInfo = new FieldInfo(
                    psiMethod.getProject(),
                    psiParameter.getName(),
                    psiType,
                    paramNameDescMap.get(psiParameter.getName()),
                    psiParameter.getAnnotations()
            );
            fieldInfoList.add(fieldInfo);
        }
//        for (PsiParameter psiParameter : psiParameters) {
//            PsiType psiType = psiParameter.getType();
//            if (excludeParamTypes.contains(psiType.getPresentableText())) {
//                continue;
//            }
//            FieldInfo fieldInfo = new FieldInfo(
//                    psiMethod.getProject(),
//                    psiParameter.getName(),
//                    psiType,
//                    paramNameDescMap.get(psiParameter.getName()),
//                    psiParameter.getAnnotations()
//            );
//            fieldInfoList.add(fieldInfo);
//        }
        return fieldInfoList;
    }

    private String extraClassPath(PsiMethod psiMethod) {
        String path = "";
        for (PsiAnnotation annotation : Objects.requireNonNull(psiMethod.getContainingClass()).getAnnotations()) {
            if (annotation.getText().contains("Mapping")) {
                path = getPathFromAnnotation(annotation);
                break;
            }
        }
        return path;
    }

    private String extraMethodPath(PsiMethod psiMethod) {
        String methodPath = "";
        for (PsiAnnotation annotation : psiMethod.getAnnotations()) {
            if (annotation.getText().contains("Mapping")) {
                methodPath = getPathFromAnnotation(annotation);
                break;
            }
        }
        return methodPath;
    }

    private String getPathFromAnnotation(PsiAnnotation annotation) {
        if (annotation == null) {
            return "";
        }
        PsiNameValuePair[] psiNameValuePairs = annotation.getParameterList().getAttributes();
        if (psiNameValuePairs.length == 1 && psiNameValuePairs[0].getName() == null) {
            return appendSlash(psiNameValuePairs[0].getLiteralValue());
        }
        if (psiNameValuePairs.length >= 1) {
            for (PsiNameValuePair psiNameValuePair : psiNameValuePairs) {
                if (psiNameValuePair.getName().equals("value") || psiNameValuePair.getName().equals("path")) {
                    return appendSlash(psiNameValuePair.getLiteralValue());
                }
            }
        }
        return "";
    }

    private String appendSlash(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        String p = path;
        if (!path.startsWith(SLASH)) {
            p = SLASH + path;
        }
        if (path.endsWith(SLASH)) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    private Map<String, String> getParamDescMap(PsiDocComment docComment) {
        Map<String, String> paramDescMap = new HashMap<>();
        if (docComment == null) {
            return paramDescMap;
        }
        for (PsiDocTag docTag : docComment.getTags()) {
            String tagValue = docTag.getValueElement() == null ? "" : docTag.getValueElement().getText();
            if ("param".equals(docTag.getName()) && StringUtils.isNotEmpty(tagValue)) {
                paramDescMap.put(tagValue, getParamDesc(docTag));
            }
        }
        return paramDescMap;
    }

    private String getParamDesc(String tagText) {
        String[] strings = tagText.replace("*", "").trim().split(" ");
        if (strings.length == 3) {
            String desc = strings[2];
            return desc.replace("\n", "");
        }
        return "";
    }

    private String getParamDesc(PsiDocTag docTag) {
        PsiElement[] dataElements = docTag.getDataElements();
        String desc = "";
        for (int i = 1; i < dataElements.length; i++) {
            desc = desc + dataElements[i].getText();
        }
        return desc;
    }

    private String getReturnDesc(PsiDocComment docComment) {
        return DesUtil.getTagContent(docComment, "return");
    }

}
