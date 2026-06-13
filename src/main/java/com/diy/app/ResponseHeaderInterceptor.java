package com.diy.app;

import com.diy.framework.context.annotation.Component;
import com.diy.framework.web.mvc.view.ModelAndView;
import com.diy.framework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ResponseHeaderInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(final HttpServletRequest req, final HttpServletResponse resp, final Object handler, final ModelAndView mv) {
        resp.setHeader("X-App-Version", "1.0.0");
        resp.setHeader("X-Server-Name", "spring-diy");
    }
}
