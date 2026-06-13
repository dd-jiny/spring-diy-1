package com.diy.framework.web.servlet;

import javax.servlet.http.HttpServletRequest;

public interface HandlerMapping {
    HandlerExecutionChain getHandler(final HttpServletRequest req) throws Exception;
}
