package com.diy.framework.web.servlet.handler;

import com.diy.framework.context.support.ApplicationObjectSupport;
import com.diy.framework.core.Ordered;
import com.diy.framework.web.servlet.HandlerExecutionChain;
import com.diy.framework.web.servlet.HandlerMapping;
import com.diy.framework.web.servlet.config.annotation.InterceptorRegistry;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractHandlerMapping extends ApplicationObjectSupport implements HandlerMapping, Ordered {
    private int order = 0;
    private InterceptorRegistry interceptorRegistry = new InterceptorRegistry();

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setInterceptorRegistry(final InterceptorRegistry interceptorRegistry) {
        this.interceptorRegistry = interceptorRegistry;
    }

    protected abstract Object getHandlerInternal(HttpServletRequest req) throws Exception;

    @Override
    public final HandlerExecutionChain getHandler(HttpServletRequest req) throws Exception {
        final Object handler = getHandlerInternal(req);
        if (handler == null) return null;
        return new HandlerExecutionChain(handler, this.interceptorRegistry.getInterceptors());
    }
}
