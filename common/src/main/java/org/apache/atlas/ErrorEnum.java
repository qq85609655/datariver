package org.apache.atlas;

import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 错误信息
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-30 11:11
 */
public enum ErrorEnum {
    PROPERTY_NOT_ENTITY("PROPERTY_NOT_ENTITY", "属性对应的值不是一个实体", 0),
    ILLEGAL_ARGUMENT("ILLEGAL_ARGUMENT", "参数{0}非法", 1),
    TIME_FORMAT_ERROR("TIME_FORMAT_ERROR", "时间格式错误", 0),
    GREMLIN_QUERY_ERROR("GREMLIN_QUERY_ERROR", "Gremlin查询异常", 0),
    SYS_ERR("SYS_ERR", "系统异常", 0),;
    private String code;
    private String msg;
    private int paramCount;

    ErrorEnum(String code, String msg, int paramCount) {
        this.code = code;
        this.msg = msg;
        this.paramCount = paramCount;
    }

    public String getCode() {
        return code;
    }

    public String getMsg(Object... params) {
        return MessageSource.getMessage(this, params);
    }

    public int getParamCount() {
        return paramCount;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        RequestHolder.setLocale(Locale.CHINA);
        System.out.println(ErrorEnum.ILLEGAL_ARGUMENT.getMsg("dd"));
    }

    static class MessageSource {
        private static final Map<String, ResourceBundle> cachedBundles = new ConcurrentHashMap<>();
        private static final String ERROR_MESSAGE_RESOURCE = "errorMessage";

        private static ResourceBundle getBundle(String name, Locale locale) {
            String key = name + locale.getDisplayName();
            if (!cachedBundles.containsKey(key)) {
                synchronized (cachedBundles) {
                    if (!cachedBundles.containsKey(key)) {
                        cachedBundles.put(key, ResourceBundle.getBundle(name, locale));
                    }
                }
            }
            return cachedBundles.get(key);
        }

        public static String getMessage(ErrorEnum errorEnum, Object... params) {
            ResourceBundle bundle = getBundle(ERROR_MESSAGE_RESOURCE, RequestHolder.getLocale());
            String msg = null;
            try {
                //防止中文乱码
                msg = new String(bundle.getString(errorEnum.getCode()).getBytes("ISO-8859-1"), "UTF-8");
            } catch (Exception e) {
                //ignore
            }
            if (StringUtils.isEmpty(msg)) {
                msg = errorEnum.msg;
            }
            params = parseParams(errorEnum, params);
            if (params != null) {
                MessageFormat format = new MessageFormat(msg, RequestHolder.getLocale());
                msg = format.format(params);
            }
            return msg;
        }

        private static Object[] parseParams(ErrorEnum errorEnum, Object[] params) {
            if (params == null) {
                params = new String[0];
            }
            //补充空值用于格式化，防止出现“{0}”类似字样
            List<Object> objects = new ArrayList<>(Arrays.asList(params));
            while (objects.size() <= errorEnum.getParamCount()) {
                objects.add(StringUtils.EMPTY);
            }
            params = objects.toArray();
            return params;
        }
    }
}
