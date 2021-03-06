package com.xkcoding.idea.plugins.yapi_helper.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.containers.ContainerUtil;
import com.xkcoding.idea.plugins.yapi_helper.config.YApiHelperConfig;
import com.xkcoding.idea.plugins.yapi_helper.constant.TypeEnum;
import com.xkcoding.idea.plugins.yapi_helper.curl.enums.ArrayFormatEnum;
import com.xkcoding.idea.plugins.yapi_helper.model.FilterFieldInfo;
import com.xkcoding.idea.plugins.yapi_helper.normal.FieldInfo;
import com.xkcoding.idea.plugins.yapi_helper.setting.CURLSettingState;
import com.xkcoding.idea.plugins.yapi_helper.store.GlobalVariable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class FieldUtil {

    public static final Map<String, Object> normalTypes = new HashMap<>();

    public static final List<String> iterableTypes = Arrays.asList("List", "ArrayList", "Set", "Collection");
    /**
     * 泛型列表
     */
    public static final List<String> genericList = new ArrayList<>();

    public static final List<String> fileList = Arrays.asList("MultipartFile", "CommonsMultipartFile", "MockMultipartFile",
            "StandardMultipartFile");

    public static final List<String> mapTypeList = Arrays.asList("Map", "HashMap", "LinkedHashMap", "JSONObject");

    private static List<String> excludeParamTypes = Arrays.asList("RedirectAttributes", "HttpServletRequest", "HttpServletResponse");


    static {
        normalTypes.put("int", 1);
        normalTypes.put("boolean", false);
        normalTypes.put("byte", 1);
        normalTypes.put("short", 1);
        normalTypes.put("long", 1L);
        normalTypes.put("float", 1.0F);
        normalTypes.put("double", 1.0D);
        normalTypes.put("char", 'a');
        normalTypes.put("Boolean", false);
        normalTypes.put("Byte", 0);
        normalTypes.put("Short", (short) 0);
        normalTypes.put("Integer", 0);
        normalTypes.put("Long", 0L);
        normalTypes.put("Float", 0.0F);
        normalTypes.put("Double", 0.0D);
        normalTypes.put("String", "@string");
        normalTypes.put("Date", System.currentTimeMillis());
        normalTypes.put("BigDecimal", 0.111111);
        normalTypes.put("LocalTime", "HH:mm:ss");
        normalTypes.put("LocalDate", "yyyy-MM-dd");
        normalTypes.put("LocalDateTime", "yyyy-MM-dd HH:mm:ss");
        normalTypes.put("BigInteger", 0);
        normalTypes.put("MultipartFile", "@/path");
        normalTypes.put("CommonsMultipartFile", "@/path");
        normalTypes.put("MockMultipartFile", "@/path");
        normalTypes.put("StandardMultipartFile", "@/path");
        genericList.add("T");
        genericList.add("E");
        genericList.add("K");
        genericList.add("V");
    }

//    public static Object getValue(PsiType psiType) {
//        if (isIterableType(psiType)) {
//            PsiType type = PsiUtil.extractIterableTypeParameter(psiType, false);
//            if (type == null) {
//                return "[]";
//            }
//            if (isNormalType(type)) {
//                Object obj = normalTypes.get(type.getPresentableText());
//                if (obj == null) {
//                    return null;
//                }
//                return obj.toString() + "," + obj.toString();
//            }
//        }
//        Object value = normalTypes.get(psiType.getPresentableText());
//        return value == null ? "" : value;
//    }

    public static Object getValue(FieldInfo fieldInfo) {
        if (TypeEnum.ARRAY == fieldInfo.getParamType()) {
            if (isNormalType(fieldInfo.getIterableTypeStr())) {
                Object obj = normalTypes.get(fieldInfo.getIterableTypeStr());
                if (obj == null) {
                    return null;
                }
                return obj.toString() + "," + obj.toString();
            }
        }
        Object value = normalTypes.get(fieldInfo.getTypeText());
        return value == null ? "" : value;
    }

    /**
     * copy as curl时，上传格式为application/x-www-form-urlencoded。
     *
     * @param psiType
     * @return
     */
    public static String getValueForCurl(String keyName, PsiType psiType, CURLSettingState state) {
        if (isIterableType(psiType)) {
            PsiType type = PsiUtil.extractIterableTypeParameter(psiType, false);
            if (type == null) {
                return keyName + "=[]";
            }
            if (isNormalType(type)) {
                Object obj = normalTypes.get(type.getPresentableText());
                if (obj == null) {
                    return null;
                }
                String arrayFormat = StringUtils.isNotEmpty(state.arrayFormat) ? state.arrayFormat : ArrayFormatEnum.repeat.name();
                if (ArrayFormatEnum.indices.name().equals(arrayFormat)) {
                    return keyName + "[0]=" + obj.toString() + "&" + keyName + "[1]=" + obj.toString();
                } else if (ArrayFormatEnum.brackets.name().equals(arrayFormat)) {
                    return keyName + "[]=" + obj.toString() + "&" + keyName + "[]=" + obj.toString();
                } else if (ArrayFormatEnum.repeat.name().equals(arrayFormat)) {
                    return keyName + "=" + obj.toString() + "&" + keyName + "=" + obj.toString();
                } else if (ArrayFormatEnum.comma.name().equals(arrayFormat)) {
                    return keyName + "=" + obj.toString() + "," + obj.toString();
                }
            }
        }
        Object value = normalTypes.get(psiType.getPresentableText());
        return value == null ? "" : keyName + "=" + value.toString();
    }

    /**
     * copy as curl时，上传格式为application/x-www-form-urlencoded。
     *
     * @param fieldInfo
     * @return
     */
    public static String getValueForCurl(FieldInfo fieldInfo, CURLSettingState state) {
        String keyName = fieldInfo.getName();
        if (TypeEnum.ARRAY == fieldInfo.getParamType()) {
            if (isNormalType(fieldInfo.getIterableTypeStr())) {
                Object obj = normalTypes.get(fieldInfo.getIterableTypeStr());
                if (obj == null) {
                    return null;
                }
                String arrayFormat = StringUtils.isNotEmpty(state.arrayFormat) ? state.arrayFormat : ArrayFormatEnum.repeat.name();
                if (ArrayFormatEnum.indices.name().equals(arrayFormat)) {
                    return keyName + "[0]=" + obj.toString() + "&" + keyName + "[1]=" + obj.toString();
                } else if (ArrayFormatEnum.brackets.name().equals(arrayFormat)) {
                    return keyName + "[]=" + obj.toString() + "&" + keyName + "[]=" + obj.toString();
                } else if (ArrayFormatEnum.repeat.name().equals(arrayFormat)) {
                    return keyName + "=" + obj.toString() + "&" + keyName + "=" + obj.toString();
                } else if (ArrayFormatEnum.comma.name().equals(arrayFormat)) {
                    return keyName + "=" + obj.toString() + "," + obj.toString();
                }
            }
        }
        Object value = normalTypes.get(fieldInfo.getTypeText());
        return value == null ? "" : keyName + "=" + value.toString();
    }


    public static boolean isNormalType(String typeName) {
        return normalTypes.containsKey(typeName);
    }

    public static boolean isFileType(String typeName) {
        return fileList.contains(typeName);
    }

    public static boolean isIterableType(String typeName) {
        if (iterableTypes.contains(typeName)) {
            return true;
        }
        for (String iterableType : iterableTypes) {
            if (typeName.startsWith(iterableType + "<")) {
                return true;
            }
        }
        return false;
//        return typeName.startsWith("List<") || typeName.startsWith("Set<") || typeName.startsWith("Collection<");
    }

    public static boolean isIterableType(PsiType psiType) {
        return isIterableType(psiType.getPresentableText());
    }

    public static boolean isNormalType(PsiType psiType) {
        PsiClass psiClass = PsiUtil.resolveClassInType(psiType);
        if (psiClass != null) {
            if (psiClass.isEnum()) {
                return true;
            }
        }
        return isNormalType(psiType.getPresentableText());
    }

    public static boolean isGenericType(String typeName) {
        return genericList.contains(typeName);
    }

    public static boolean isMapType(String typeText) {
        for (String mapType : mapTypeList) {
            if (mapType.equals(typeText) || typeText.startsWith(mapType.concat("<"))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMapType(PsiType psiType) {
        return isMapType(psiType.getPresentableText());
    }

    public static PsiAnnotation findAnnotationByName(List<PsiAnnotation> annotations, String text) {
        if (annotations == null) {
            return null;
        }
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains(text)) {
                return annotation;
            }
        }
        return null;
    }

    public static List<FieldInfo> filterChildrenFiled(List<FieldInfo> items, FilterFieldInfo filterFieldInfo) {
        List<String> canonicalClassNameList = filterFieldInfo.getCanonicalClassNameList();
        List<String> includeFiledList = filterFieldInfo.getIncludeFiledList();
        List<String> excludeFiledList = filterFieldInfo.getExcludeFiledList();
        for (FieldInfo item : items) {
            List<FieldInfo> children = item.getChildren();
            int index = getIndexOnCanonicalClassNameList(item.getCanonicalText(), canonicalClassNameList);
            if (CollectionUtils.isNotEmpty(canonicalClassNameList) && index != -1) {

                if (includeFiledList.size() > index && StringUtils.isNotEmpty(includeFiledList.get(index))) {
                    String includeFieldStr = includeFiledList.get(index).concat(",");
                    children.removeIf(child -> !includeFieldStr.contains(child.getName() + ","));
                } else if (excludeFiledList.size() > index && StringUtils.isNotEmpty(excludeFiledList.get(index))) {
                    String excludeFieldStr = excludeFiledList.get(index).concat(",");
                    children.removeIf(child -> excludeFieldStr.contains(child.getName() + ","));
                }
                if (filterFieldInfo.excludeChildren) {
                    for (FieldInfo child : children) {
                        child.setChildren(ContainerUtil.newArrayList());
                    }
                }
            }
            item.setChildren(children);
        }
        return items;
    }

    public static int getIndexOnCanonicalClassNameList(String canonicalClassName, List<String> set) {
        for (String s : set) {
            if (canonicalClassName.startsWith(s)) {
                return set.indexOf(s);
            }
        }
        return -1;
    }

    public static List<PsiParameter> filterParameters(PsiParameter[] psiParameters) {
        List<PsiParameter> psiParameterList = new ArrayList<>();
        YApiHelperConfig apiConfig = GlobalVariable.getApiConfig();

        for (PsiParameter psiParameter : psiParameters) {
            PsiType psiType = psiParameter.getType();
            if (excludeParamTypes.contains(psiType.getPresentableText())) {
                continue;
            }
            boolean ignore = false;
            for (String annotationName : StringUtil.string2Set(apiConfig.excludeAnnotations)) {
                if (psiParameter.getText().contains(annotationName)) {
                    ignore = true;
                    break;
                }
            }
            if (ignore) {
                continue;
            }
            psiParameterList.add(psiParameter);
        }
        return psiParameterList;
    }
}

