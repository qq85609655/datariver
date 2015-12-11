package org.apache.atlas.web.filters;

import com.google.inject.Singleton;
import org.apache.atlas.RequestHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;

/**
 * locale信息管理过滤器<br>
 * <li>持有当前请求的locale信息</li>
 * <li>可修改请求的locale信息</li>
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-12-01 11:38
 */
@Singleton
public class RequestLevelHolderFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(RequestLevelHolderFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("LocaleManageFilter初始化");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            RequestHolder.setLocale(request.getLocale());
        } catch (Throwable throwable) {
            LOG.error("设置holder异常",throwable);
        }finally {
            chain.doFilter(request, response);
        }
        RequestHolder.RequestHolderCleaner.clear();
    }

    @Override
    public void destroy() {
        LOG.info("LocaleManageFilter销毁");
    }
}
