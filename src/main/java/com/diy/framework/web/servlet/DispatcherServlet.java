package com.diy.framework.web.servlet;

import com.diy.framework.beans.factory.BeanFactoryUtils;
import com.diy.framework.context.ApplicationContext;
import com.diy.framework.context.support.WebApplicationContextUtils;
import com.diy.framework.core.Ordered;
import com.diy.framework.web.mvc.view.ModelAndView;
import com.diy.framework.web.mvc.view.View;
import com.diy.framework.web.mvc.view.ViewResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    private List<HandlerMapping> handlerMappings;
    private List<HandlerAdapter> handlerAdapters;
    private List<HandlerExceptionResolver> handlerExceptionResolvers;
    private List<ViewResolver> viewResolvers;

    @Override
    public void init() throws ServletException {
        initStrategies(initWebApplicationContext());
        super.init();
    }

    private ApplicationContext initWebApplicationContext() {
        return WebApplicationContextUtils.getWebApplicationContext(getServletContext(), ApplicationContext.APPLICATION_CONTEXT_ATTRIBUTE);
    }

    private void initStrategies(final ApplicationContext context) {
        initHandlerMappings(context);
        initHandlerAdapters(context);
        initHandlerExceptionResolvers(context);
        initViewResolvers(context);
    }

    private void initHandlerExceptionResolvers(final ApplicationContext context) {
        try {
            final Map<String, HandlerExceptionResolver> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class);
            this.handlerExceptionResolvers = new ArrayList<>(matchingBeans.values());
        } catch (RuntimeException e) {
            this.handlerExceptionResolvers = new ArrayList<>();
        }
    }

    private void initHandlerMappings(final ApplicationContext context) {
        final Map<String, HandlerMapping> matchingBeans =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class);
        this.handlerMappings = new ArrayList<>(matchingBeans.values());

        this.handlerMappings.sort(Comparator.comparingInt(o -> ((Ordered) o).getOrder()));
    }

    private void initHandlerAdapters(final ApplicationContext context) {
        final Map<String, HandlerAdapter> matchingBeans =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class);
        this.handlerAdapters = new ArrayList<>(matchingBeans.values());
    }

    private void initViewResolvers(final ApplicationContext context) {
        final Map<String, ViewResolver> matchingBeans =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ViewResolver.class);
        this.viewResolvers = new ArrayList<>(matchingBeans.values());

        this.viewResolvers.sort(Comparator.comparingInt(o -> ((Ordered) o).getOrder()));
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req, resp);
    }

    private void doDispatch(final HttpServletRequest req, final HttpServletResponse resp) {
        HandlerExecutionChain mappedHandler = null;
        Exception dispatchException = null;
        try {
            mappedHandler = getHandler(req);
            if (mappedHandler == null) return;

            if (!mappedHandler.applyPreHandle(req, resp)) return;

            final HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

            final ModelAndView mv = ha.handle(req, resp, mappedHandler.getHandler());

            mappedHandler.applyPostHandle(req, resp, mv);

            if (mv != null && mv.getViewName() != null) render(mv, req, resp);
        } catch (Exception e) {
            final Object handler = (mappedHandler != null) ? mappedHandler.getHandler() : null;
            final ModelAndView resolved = processHandlerException(req, resp, handler, e);
            if (resolved == null) {
                dispatchException = e;
                throw new RuntimeException(e);
            }
            try {
                if (resolved.getViewName() != null) render(resolved, req, resp);
            } catch (Exception renderEx) {
                dispatchException = renderEx;
                throw new RuntimeException(renderEx);
            }
        } finally {
            if (mappedHandler != null) {
                mappedHandler.triggerAfterCompletion(req, resp, dispatchException);
            }
        }
    }

    private ModelAndView processHandlerException(final HttpServletRequest req, final HttpServletResponse resp, final Object handler, final Exception ex) {
        if (this.handlerExceptionResolvers != null) {
            for (final HandlerExceptionResolver resolver : this.handlerExceptionResolvers) {
                final ModelAndView mv = resolver.resolveException(req, resp, handler, ex);
                if (mv != null) return mv;
            }
        }
        return null;
    }

    protected HandlerExecutionChain getHandler(final HttpServletRequest req) throws Exception {
        if (this.handlerMappings != null) {
            for (final HandlerMapping mapping : this.handlerMappings) {
                final HandlerExecutionChain handler = mapping.getHandler(req);
                if (handler != null) {
                    return handler;
                }
            }
        }
        return null;
    }

    protected HandlerAdapter getHandlerAdapter(final Object handler) throws ServletException {
        if (this.handlerAdapters != null) {
            for (final HandlerAdapter adapter : this.handlerAdapters) {
                if (adapter.supports(handler)) {
                    return adapter;
                }
            }
        }

        throw new ServletException("No adapter for handler [" + handler +
                "]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
    }

    private void render(final ModelAndView mv, final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        final String viewName = mv.getViewName();

        final View view = resolveViewName(viewName);

        view.render(mv.getModel(), req, resp);
    }

    private View resolveViewName(final String viewName) {
        if (this.viewResolvers != null) {
            for (ViewResolver viewResolver : this.viewResolvers) {
                View view = viewResolver.resolveViewName(viewName);
                if (view != null) {
                    return view;
                }
            }
        }

        return null;
    }

}
