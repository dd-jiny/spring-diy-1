package com.diy.app;

import com.diy.framework.context.annotation.Component;
import com.diy.framework.web.servlet.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RequestTimingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestTimingInterceptor.class);

    private static final String START_TIME_ATTR = RequestTimingInterceptor.class.getName() + ".startTime";

    @Override
    public boolean preHandle(final HttpServletRequest req, final HttpServletResponse resp, final Object handler) {
        req.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(final HttpServletRequest req, final HttpServletResponse resp, final Object handler, final Exception ex) {
        final Long start = (Long) req.getAttribute(START_TIME_ATTR);
        if (start == null) return;

        final long elapsedMs = System.currentTimeMillis() - start;
        if (ex != null) {
            log.info("[{} {}] {}ms (failed: {})", req.getMethod(), req.getRequestURI(), elapsedMs, ex.getMessage());
        } else {
            log.info("[{} {}] {}ms", req.getMethod(), req.getRequestURI(), elapsedMs);
        }
    }
}
