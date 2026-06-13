package com.diy.framework.web.servlet;

import com.diy.framework.web.mvc.view.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class HandlerExecutionChain {

    private static final Logger log = LoggerFactory.getLogger(HandlerExecutionChain.class);

    private final Object handler;
    private final List<HandlerInterceptor> interceptors;
    private int interceptorIndex = -1;

    public HandlerExecutionChain(final Object handler, final List<HandlerInterceptor> interceptors) {
        this.handler = handler;
        this.interceptors = (interceptors != null) ? new ArrayList<>(interceptors) : new ArrayList<>();
    }

    public Object getHandler() {
        return this.handler;
    }

    public boolean applyPreHandle(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        final int interceptorsSize = this.interceptors.size();

        for (int i = 0; i < interceptorsSize; i++) {
            final HandlerInterceptor interceptor = this.interceptors.get(i);
            if (!interceptor.preHandle(req, resp, this.handler)) {
                return false;
            }
            this.interceptorIndex = i;
        }

        return true;
    }

    public void applyPostHandle(final HttpServletRequest req, final HttpServletResponse resp, final ModelAndView mv) throws Exception {
        for (int i = this.interceptors.size() - 1; i >= 0; i--) {
            this.interceptors.get(i).postHandle(req, resp, this.handler, mv);
        }
    }

    public void triggerAfterCompletion(final HttpServletRequest req, final HttpServletResponse resp, final Exception ex) {
        for (int i = this.interceptorIndex; i >= 0; i--) {
            try {
                this.interceptors.get(i).afterCompletion(req, resp, this.handler, ex);
            } catch (Throwable t) {
                log.error("HandlerInterceptor.afterCompletion threw exception", t);
            }
        }
    }
}
