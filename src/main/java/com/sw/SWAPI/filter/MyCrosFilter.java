package com.sw.SWAPI.filter;


import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: LPL
 * @Date: 2021/3/23 9:31
 * @ProjectName: SWRALMIntegration
 * @Description:
 **/
@Component
public class MyCrosFilter implements Filter{
    /**
     * @author LPL
     * @date 2021/3/23 10:56
     * @description CrosFilter
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        response.setHeader("Access-Control-Allow-Origin","*");
        response.setHeader("Access-Control-Allow-Methods","POST,GET,OPTIONS,DELETE,PUT");
        response.setHeader("Access-Control-Max-Age","3600");
        response.setHeader("Access-Control-Allow-Headers","Content-Type");
        filterChain.doFilter(servletRequest,servletResponse);
    }
}
