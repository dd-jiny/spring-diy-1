package com.diy.app;

import com.diy.framework.web.servlet.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

//@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private static final Set<String> AUTH_REQUIRED = Set.of(
            "POST /lectures",
            "POST /api/lectures"
    );

    @Override
    public boolean preHandle(final HttpServletRequest req, final HttpServletResponse resp, final Object handler) {
        final String key = req.getMethod() + " " + req.getRequestURI();
        if (!AUTH_REQUIRED.contains(key)) return true;

        final String authorization = req.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            log.warn("[AUTH] blocked {}", key);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }
}
