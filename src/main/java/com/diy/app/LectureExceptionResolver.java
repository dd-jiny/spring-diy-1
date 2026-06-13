package com.diy.app;

import com.diy.framework.web.mvc.view.ModelAndView;
import com.diy.framework.web.servlet.HandlerExceptionResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

//@Component
public class LectureExceptionResolver implements HandlerExceptionResolver {

    private static final Logger log = LoggerFactory.getLogger(LectureExceptionResolver.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ModelAndView resolveException(final HttpServletRequest req, final HttpServletResponse resp, final Object handler, final Exception ex) {
        if (!(ex instanceof IllegalArgumentException)) return null;

        try {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            final String message = ex.getMessage() != null ? ex.getMessage() : "Bad request";
            final String body = objectMapper.writeValueAsString(Map.of(
                    "error", "BAD_REQUEST",
                    "message", message
            ));
            resp.getWriter().write(body);
            return new ModelAndView(null);
        } catch (IOException e) {
            log.error("Failed to write error response", e);
            return null;
        }
    }
}
