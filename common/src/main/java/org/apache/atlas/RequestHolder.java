package org.apache.atlas;

import java.util.*;

/**
 * 请求级别信息持有器
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-12-01 11:44
 */
public class RequestHolder {
    private static final ThreadLocal<Locale> localeHolder = new ThreadLocal<>();
    static {
        RequestHolderCleaner.register(localeHolder);
    }
    public static Locale getLocale() {
        return localeHolder.get();
    }

    public static void setLocale(Locale locale) {
        localeHolder.set(locale);
    }
    public static class RequestHolderCleaner{
        private  static final Set<ThreadLocal> holders = new HashSet<>();

        public static void register(ThreadLocal holder) {
            holders.add(holder);
        }

        public static void clear() {
            for (ThreadLocal holder : holders) {
                holder.remove();
            }
        }
    }
}

