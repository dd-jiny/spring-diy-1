package com.diy.framework.web.servlet.config.annotation;

import com.diy.framework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InterceptorRegistry {

    private final List<HandlerInterceptor> interceptors = new ArrayList<>();

    public InterceptorRegistry addInterceptor(final HandlerInterceptor interceptor) {
        this.interceptors.add(interceptor);
        return this;
    }

    public List<HandlerInterceptor> getInterceptors() {
        return Collections.unmodifiableList(this.interceptors);
    }
}
