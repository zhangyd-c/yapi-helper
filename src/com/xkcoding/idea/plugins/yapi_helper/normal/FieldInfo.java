package com.xkcoding.idea.plugins.yapi_helper.normal;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiUtil;
import com.xkcoding.idea.plugins.yapi_helper.config.YApiHelperConfig;
import com.xkcoding.idea.plugins.yapi_helper.constant.TypeEnum;
import com.xkcoding.idea.plugins.yapi_helper.constant.WebAnnotation;
import com.xkcoding.idea.plugins.yapi_helper.util.AssertUtils;
import com.xkcoding.idea.plugins.yapi_helper.util.DesUtil;
import com.xkcoding.idea.plugins.yapi_helper.util.FieldUtil;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

@Data
public class FieldInfo {

    private String name;
    private PsiType psiType;
    private boolean require;
    private String range;
    private String desc;
    private TypeEnum paramType;
    private List<FieldInfo> children;
    private FieldInfo parent;
    private List<PsiAnnotation> annotations;
    private Project project;
    @Deprecated
    private Map<PsiTypeParameter, PsiType> genericsMap;
    private Map<String, PsiType> javaGenericsMap;

    private String canonicalText;
    private String typeText;
    private String iterableTypeStr;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FieldInfo fieldInfo = (FieldInfo) o;
        return name.equals(fieldInfo.name) &&
                Objects.equals(parent, fieldInfo.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parent);
    }

    private static List<String> requiredTexts = Arrays.asList("@NotNull", "@NotBlank", "@NotEmpty", "@PathVariable");

    protected YApiHelperConfig config;

    public FieldInfo(Project project, PsiType psiType, String desc) {
        this(project, psiType, desc, new PsiAnnotation[0]);
    }

    public FieldInfo(Project project, String name, PsiType psiType, String desc, PsiAnnotation[] annotations) {
        this.project = project;
        config = ServiceManager.getService(project, YApiHelperConfig.class);
        RequireAndRange requireAndRange = getRequireAndRange(annotations);
        String fieldName = getParamName(name, annotations);
        this.name = fieldName == null ? "N/A" : fieldName;
        this.psiType = psiType;
        this.require = requireAndRange.isRequire();
        this.range = requireAndRange.getRange();
        this.desc = desc == null ? "" : desc;
        this.annotations = Arrays.asList(annotations);
        this.javaGenericsMap = resolveJavaGenerics(psiType);
        if (psiType != null) {
            this.setTypeText(psiType.getPresentableText());
            this.setCanonicalText(psiType.getCanonicalText());
            if (FieldUtil.isNormalType(psiType)) {
                paramType = TypeEnum.LITERAL;
            } else if (FieldUtil.isIterableType(psiType)) {
                paramType = TypeEnum.ARRAY;
            } else {
                paramType = TypeEnum.OBJECT;
            }
            if (needResolveChildren(psiType)) {
                this.children = listChildren(this);
            }
        } else {
            paramType = TypeEnum.OBJECT;
        }
    }

    public FieldInfo(Project project, FieldInfo parent, String name, PsiType psiType, String desc, PsiAnnotation[] annotations) {
        this.project = project;
        config = ServiceManager.getService(project, YApiHelperConfig.class);
        RequireAndRange requireAndRange = getRequireAndRange(annotations);
        String fieldName = getParamName(name, annotations);
        this.name = fieldName == null ? "N/A" : fieldName;
        this.psiType = psiType;
        this.require = requireAndRange.isRequire();
        this.range = requireAndRange.getRange();
        this.desc = desc == null ? "" : desc;
        this.annotations = Arrays.asList(annotations);
        this.parent = parent;
        if (psiType != null) {
            this.setTypeText(psiType.getPresentableText());
            this.setCanonicalText(psiType.getCanonicalText());
            if (FieldUtil.isNormalType(psiType)) {
                paramType = TypeEnum.LITERAL;
            } else if (FieldUtil.isIterableType(psiType)) {
                paramType = TypeEnum.ARRAY;
            } else {
                paramType = TypeEnum.OBJECT;
            }
            if (needResolveChildren(parent, psiType)) {
                this.children = listChildren(this);
            }
        } else {
            paramType = TypeEnum.OBJECT;
        }
    }

    public FieldInfo(Project project, PsiType psiType, String desc, PsiAnnotation[] annotations) {
        this(project, psiType.getPresentableText(), psiType, desc, annotations);
    }

    private String getParamName(String name, PsiAnnotation[] annotations) {
        PsiAnnotation requestParamAnnotation = getRequestParamAnnotation(annotations);
        if (requestParamAnnotation == null) {
            return name;
        }
        PsiNameValuePair[] attributes = requestParamAnnotation.getParameterList().getAttributes();
        if (attributes.length == 1 && attributes[0].getName() == null) {
            return attributes[0].getLiteralValue();
        }
        for (PsiNameValuePair psiNameValuePair : attributes) {
            String pairName = psiNameValuePair.getName();
            if ("value".equals(pairName) || "name".equals(pairName)) {
                return psiNameValuePair.getLiteralValue();
            }
        }
        return name;
    }

    private PsiAnnotation getRequestParamAnnotation(PsiAnnotation[] annotations) {
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains(WebAnnotation.RequestParam)) {
                return annotation;
            }
        }
        return null;
    }

    private List<FieldInfo> listChildren(FieldInfo fieldInfo) {
        PsiType psiType = fieldInfo.getPsiType();
        if (psiType == null) {
            return new ArrayList<>();
        }
        if (FieldUtil.isNormalType(psiType.getPresentableText())) {
            //基础类或基础包装类没有子域
            return new ArrayList<>();
        }
        List<FieldInfo> fieldInfos = new ArrayList<>();
        if (psiType instanceof PsiClassReferenceType) {
            //如果是集合类型
            if (FieldUtil.isIterableType(psiType)) {
                psiType = PsiUtil.extractIterableTypeParameter(psiType, false);
            }
            String typeName = psiType.getPresentableText();
            // 如果是泛型
            if (FieldUtil.isGenericType(typeName)) {
                Object tempType = getTypeByGenerics(typeName);
                if (tempType != null) {
                    if (tempType instanceof PsiType) {
                        PsiType genericType = (PsiType) tempType;
                        this.iterableTypeStr = genericType.getPresentableText();
                        if (FieldUtil.isNormalType(genericType.getPresentableText())
                                || FieldUtil.isMapType(genericType.getPresentableText())) {
                            return new ArrayList<>();
                        }
                        return listChildren(new FieldInfo(fieldInfo.getProject(), fieldInfo, genericType.getPresentableText(),
                                genericType, "", new PsiAnnotation[0]));
                    }
                }
                return Collections.emptyList();
            }
            if (typeName.startsWith("Map")) {
                fieldInfos.add(new FieldInfo(project, fieldInfo, typeName, null, "", new PsiAnnotation[0]));
                return fieldInfos;
            }
            if (typeName.contains("<")) {
                PsiClass outerClass = PsiUtil.resolveClassInType(psiType);
                for (PsiField outField : outerClass.getAllFields()) {
                    PsiType type = outField.getType();
                    if (config.getState().excludeFields.contains(outField.getName())) {
                        continue;
                    }
                    fieldInfos.add(new FieldInfo(project, fieldInfo, outField.getName(), type,
                            DesUtil.getDescription(outField.getDocComment()), outField.getAnnotations()));
                }
                return fieldInfos;
            }
            PsiClass psiClass = PsiUtil.resolveClassInType(psiType);
            if (psiClass == null) {
                return new ArrayList<>();
            }
            for (PsiField psiField : psiClass.getAllFields()) {
                if (config.getState().excludeFields.contains(psiField.getName())) {
                    continue;
                }
                fieldInfos.add(new FieldInfo(project, fieldInfo, psiField.getName(), psiField.getType(),
                        DesUtil.getDescription(psiField.getDocComment()), psiField.getAnnotations()));
            }
            return fieldInfos;
        }
        return new ArrayList<>();
    }

    private boolean needResolveChildren(PsiType psiType) {
        PsiClass psiClass = PsiUtil.resolveClassInType(psiType);
        if (psiClass != null) {
            if (psiClass.isEnum()) {
                return false;
            }
        }
        return !FieldUtil.isMapType(psiType);
    }

    private boolean needResolveChildren(FieldInfo parent, PsiType psiType) {
        if (parent == null) {
            return true;
        }
        PsiClass psiClass = PsiUtil.resolveClassInType(psiType);
        if (psiClass != null) {
            if (psiClass.isEnum()) {
                return false;
            }
        }
        if (FieldUtil.isMapType(psiType)) {
            return false;
        }
        Set<PsiType> resolvedTypeSet = new HashSet<>();
        FieldInfo p = parent;
        while (p != null) {
            resolvedTypeSet.add(p.getPsiType());
            p = p.getParent();
        }
        if (TypeEnum.ARRAY.equals(paramType)) {
            psiType = PsiUtil.extractIterableTypeParameter(psiType, false);
        }
        for (PsiType resolvedType : resolvedTypeSet) {
            if (resolvedType != null && resolvedType.equals(psiType)) {
                return false;
            }
        }
        return true;
    }

    private RequireAndRange getRequireAndRange(PsiAnnotation[] annotations) {
        if (annotations.length == 0) {
            return RequireAndRange.instance();
        }
        boolean require = false;
        String min = "";
        String max = "";
        String range = "N/A";
        for (PsiAnnotation annotation : annotations) {
            if (isParamRequired(annotation)) {
                require = true;
                break;
            }
        }
        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getText();
            if (qualifiedName.contains("Length") || qualifiedName.contains("Range") || qualifiedName.contains("Size")) {
                PsiAnnotationMemberValue minValue = annotation.findAttributeValue("min");
                if (minValue != null) {
                    min = minValue.getText();
                    break;
                }
            }
            if (qualifiedName.contains("Min")) {
                PsiAnnotationMemberValue minValue = annotation.findAttributeValue("value");
                if (minValue != null) {
                    min = minValue.getText();
                    break;
                }
            }
        }
        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getText();
            if (qualifiedName.contains("Length") || qualifiedName.contains("Range") || qualifiedName.contains("Size")) {
                PsiAnnotationMemberValue maxValue = annotation.findAttributeValue("max");
                if (maxValue != null) {
                    max = maxValue.getText();
                    break;
                }
            }
            if (qualifiedName.contains("Max")) {
                PsiAnnotationMemberValue maxValue = annotation.findAttributeValue("value");
                if (maxValue != null) {
                    max = maxValue.getText();
                    break;
                }
            }
        }
        if (StringUtils.isNotEmpty(min) || StringUtils.isNotEmpty(max)) {
            range = "[" + min + "," + max + "]";
        }
        return new RequireAndRange(require, range);
    }

    private boolean isParamRequired(PsiAnnotation annotation) {
        String annotationText = annotation.getText();
        if (annotationText.contains(WebAnnotation.RequestParam)) {
            PsiNameValuePair[] psiNameValuePairs = annotation.getParameterList().getAttributes();
            for (PsiNameValuePair psiNameValuePair : psiNameValuePairs) {
                if ("required".equals(psiNameValuePair.getName()) && "false".equals(psiNameValuePair.getLiteralValue())) {
                    return false;
                }
            }
            return true;
        }
        return requiredTexts.contains(annotationText.split("\\(")[0]);
    }

    public boolean hasChildren() {
        return AssertUtils.isNotEmpty(children);
    }

    /**
     * 提取泛型对应的PsiType
     *
     * @param psiType
     * @return
     * @deprecated as of JDK 1.0.5, replace by {@link #resolveJavaGenerics(PsiType)}
     */
    @Deprecated
    private Map<PsiTypeParameter, PsiType> resolveGenerics(PsiType psiType) {
        // 拆解参数类型中的泛型类
        PsiClassType psiClassType = (PsiClassType) psiType;
        PsiType[] parameters = psiClassType.getParameters();

        // 拆解参数类型中的泛型 如 T、V
        PsiClass resolve = ((PsiClassType) psiType).resolve();
        PsiTypeParameter[] typeParameters = resolve.getTypeParameters();
        int i = 0;
        Map<PsiTypeParameter, PsiType> map = new HashMap<>();
        for (PsiTypeParameter typeParameter : typeParameters) {
            map.put(typeParameter, parameters[i]);
            i++;
        }
        return map;
    }

    private Map<String, PsiType> resolveJavaGenerics(PsiType psiType) {
        // 拆解参数类型中的泛型类
        PsiClassType psiClassType = (PsiClassType) psiType;
        PsiType[] parameters = psiClassType.getParameters();

        // 拆解参数类型中的泛型 如 T、V
        PsiClass resolve = ((PsiClassType) psiType).resolve();
        PsiTypeParameter[] typeParameters = resolve.getTypeParameters();
        int i = 0;
        Map<String, PsiType> map = new HashMap<>();
        if (typeParameters.length == parameters.length) {
            for (PsiTypeParameter typeParameter : typeParameters) {
                map.put(typeParameter.getName(), parameters[i]);
                i++;
            }
        }
        return map;
    }

    /**
     * 根据泛型获取对应的PsiType
     *
     * @param psiType
     * @return
     */
    private PsiType getJavaTypeByGenerics(PsiType psiType) {
        if (null == psiType) {
            return null;
        }
        if (this.parent != null) {
            return this.parent.getJavaTypeByGenerics(psiType);
        }
        if (null != javaGenericsMap) {
            for (String keyStr : javaGenericsMap.keySet()) {
                if (keyStr.equals(psiType.getPresentableText())) {
                    return javaGenericsMap.get(keyStr);
                }
            }
        }
        return psiType;
    }

    private Object getTypeByGenerics(String genericName) {
        if (StringUtils.isBlank(genericName)) {
            return null;
        }
        if (this.parent != null) {
            return this.parent.getTypeByGenerics(genericName);
        }
        if (null != javaGenericsMap) {
            for (String genericKey : javaGenericsMap.keySet()) {
                if (genericKey.equals(genericName)) {
                    return javaGenericsMap.get(genericKey);
                }
            }
        }
        return null;
    }

    public boolean containRequestBodyAnnotation() {
        if (CollectionUtils.isNotEmpty(this.annotations)) {
            for (PsiAnnotation annotation : annotations) {
                if (annotation.getText().contains(WebAnnotation.RequestBody)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containPathVariableAnnotation() {
        if (CollectionUtils.isNotEmpty(this.annotations)) {
            for (PsiAnnotation annotation : annotations) {
                if (annotation.getText().contains(WebAnnotation.PathVariable)) {
                    return true;
                }
            }
        }
        return false;
    }

}
