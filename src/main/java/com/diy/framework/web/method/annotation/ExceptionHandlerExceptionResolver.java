package com.diy.framework.web.method.annotation;

import com.diy.framework.beans.factory.BeanFactoryUtils;
import com.diy.framework.context.ApplicationContext;
import com.diy.framework.context.support.ApplicationObjectSupport;
import com.diy.framework.web.http.converter.HttpMessageConverter;
import com.diy.framework.web.mvc.anotation.ControllerAdvice;
import com.diy.framework.web.mvc.anotation.ExceptionHandler;
import com.diy.framework.web.mvc.view.ModelAndView;
import com.diy.framework.web.servlet.HandlerExceptionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExceptionHandlerExceptionResolver extends ApplicationObjectSupport implements HandlerExceptionResolver {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerExceptionResolver.class);

    private final List<HttpMessageConverter> messageConverters;
    private final Map<Class<? extends Throwable>, ExceptionHandlerMethod> handlers = new HashMap<>();

    public ExceptionHandlerExceptionResolver(final List<HttpMessageConverter> messageConverters) {
        this.messageConverters = messageConverters;
    }

    @Override
    protected void initApplicationContext(final ApplicationContext context) {
        final Map<String, Object> adviceBeans = BeanFactoryUtils.beansOfAnnotated(context, ControllerAdvice.class);
        for (final Object advice : adviceBeans.values()) {
            for (final Method method : advice.getClass().getDeclaredMethods()) {
                final ExceptionHandler annotation = method.getDeclaredAnnotation(ExceptionHandler.class);
                if (annotation == null) continue;
                for (final Class<? extends Throwable> exType : annotation.value()) {
                    handlers.put(exType, new ExceptionHandlerMethod(advice, method));
                }
            }
        }
    }

    @Override
    public ModelAndView resolveException(final HttpServletRequest req, final HttpServletResponse resp, final Object handler, final Exception ex) {
        final ExceptionHandlerMethod handlerMethod = findHandlerMethod(ex.getClass());
        if (handlerMethod == null) return null;

        try {
            handlerMethod.method.setAccessible(true);
            final Object[] args = resolveArguments(handlerMethod.method, ex, req, resp);
            final Object result = handlerMethod.method.invoke(handlerMethod.bean, args);

            if (result != null) {
                writeAsJson(result, req, resp);
            }
            return new ModelAndView(null);
        } catch (Exception invocationEx) {
            log.error("@ExceptionHandler invocation failed", invocationEx);
            return null;
        }
    }

    private Object[] resolveArguments(final Method method, final Exception ex, final HttpServletRequest req, final HttpServletResponse resp) {
        final Parameter[] params = method.getParameters();
        final Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            final Class<?> type = params[i].getType();
            if (Throwable.class.isAssignableFrom(type) && type.isInstance(ex)) {
                args[i] = ex;
            } else if (HttpServletRequest.class.isAssignableFrom(type)) {
                args[i] = req;
            } else if (HttpServletResponse.class.isAssignableFrom(type)) {
                args[i] = resp;
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    private ExceptionHandlerMethod findHandlerMethod(final Class<?> exClass) {
        Class<?> current = exClass;
        while (current != null && Throwable.class.isAssignableFrom(current)) {
            final ExceptionHandlerMethod m = handlers.get(current);
            if (m != null) return m;
            current = current.getSuperclass();
        }
        return null;
    }

    private void writeAsJson(final Object body, final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        final String accept = req.getHeader("Accept");
        final Class<?> bodyType = body.getClass();
        for (final HttpMessageConverter converter : messageConverters) {
            if (converter.canWrite(bodyType, accept)) {
                final List<String> supported = converter.getSupportedMediaTypes();
                final String contentType = supported.isEmpty() ? null : supported.get(0);
                converter.write(body, contentType, resp);
                return;
            }
        }
        throw new IllegalStateException("No HttpMessageConverter can write " + bodyType);
    }

    private static class ExceptionHandlerMethod {
        final Object bean;
        final Method method;

        ExceptionHandlerMethod(final Object bean, final Method method) {
            this.bean = bean;
            this.method = method;
        }
    }
}
