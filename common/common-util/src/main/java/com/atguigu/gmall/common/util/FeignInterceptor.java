package com.atguigu.gmall.common.util;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @title: RequestInterceptor
 * @Author LiuXianKun
 * @Date: 2020/11/30 11:23
 *
 * 先执行该接口的实现方法
 * 再进行远程调用
 * 谁用谁实例化  不试用@Compoment
 */


public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (null != servletRequestAttributes) { //MQ不适用该request  而是socket
            HttpServletRequest request = servletRequestAttributes.getRequest();
            if (null != request) {
                String userId = request.getHeader("userId");
                requestTemplate.header("userId", userId);
            }
            String userTempId = request.getHeader("userTempId");
            if (null != userTempId) {
                requestTemplate.header("userTempId", userTempId);
            }
        }

    }
}
