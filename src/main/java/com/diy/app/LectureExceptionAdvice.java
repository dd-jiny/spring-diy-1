package com.diy.app;

import com.diy.framework.web.mvc.anotation.ControllerAdvice;
import com.diy.framework.web.mvc.anotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@ControllerAdvice
public class LectureExceptionAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, String> handleIllegalArgument(final IllegalArgumentException ex, final HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return Map.of(
                "error", "BAD_REQUEST",
                "message", ex.getMessage() != null ? ex.getMessage() : "Bad request"
        );
    }
}
