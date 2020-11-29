package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.gateway.constant.RedisConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @title: LoginGlobalFilter
 * @Author LiuXianKun
 * @Date: 2020/11/29 15:55
 */
@Component
public class LoginGlobalFilter implements GlobalFilter, Ordered {
    private AntPathMatcher pathMatcher = new AntPathMatcher();
    @Value("${authUrls.url}")
    private String[] authUrl;
    @Value("${authUrls.loginUrl}")
    private String loginUrl;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //判断是否登录    token   Http请求的时候的请求或响应吗？
        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();
        System.out.println("loginUrl = " + loginUrl);
        System.out.println("当前的请求路径 = " + path);
        String rawSchemeSpecificPart = request.getURI().getRawSchemeSpecificPart();
        System.out.println("全路径 = " + rawSchemeSpecificPart);  //包括http://
        //获取token
        String userId = getToken(request);

        //1：访问 搜索微服务 订单微服务 详情微服务 商品微服务 异步请求
        if (pathMatcher.match("/*/auth/**", path)) {
            if (null == userId) {
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }

        }
        //2：内部资源  网关不给访问
        if (pathMatcher.match("/**/inner/**", path)) {
            return out(response, ResultCodeEnum.PERMISSION);
        }

        //3：访问页面微服务的时候  同步请求
        //首页 /
        //搜索 list.html
        //商品详情 /4312412432.html
        //购物车  /cart.html
        //结算页面  /trade.html
        //订单页面 order.html
        //支付页面  pay.html
        for (String url : authUrl) {
            if (url.equals(path)) {
                //判断是否登录
                //未登录  重定向到登录页面   保存的用户信息
                if (null == userId) {
                    //设置重定向的地址 注意 路径可能带中文   从哪里来回哪里去
                    try {
                        response.getHeaders().add(HttpHeaders.LOCATION, loginUrl + URLEncoder.encode(rawSchemeSpecificPart, "utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //设置response支持重定向
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    return response.setComplete();  //必不可少
                }
            }
        }

        //用户登录了的状态  需要传递用户id给后续的其他微服务
        if (null != userId) {
            request.mutate().header("userId", userId);
        }
        //如果用户未登录  则创建临时用户ID
        String userTempId = getUserTempId(request);
        if (null != userTempId) {
            request.mutate().header("userTempId", userTempId);
        }

        return chain.filter(exchange);
    }
    //获取用户的临时ID
    private String getUserTempId(ServerHttpRequest request) {
        String userTempId = request.getHeaders().getFirst("userTempId");
        if (null == userTempId) {
            //2：没有 再获取Cookie
            HttpCookie cookie = request.getCookies().getFirst("userTempId");
            if (null != cookie) {
                userTempId = cookie.getValue();
            }
        }
        return userTempId;
    }

    //不允许访问时候的响应结果
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        Result result = Result.build(null, resultCodeEnum);
        String s = JSONObject.toJSONString(result);
        //为了防止乱码，设置响应头编码格式
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        //将json设置到响应报文的响应体中
        Mono body = Mono.just(response.bufferFactory().wrap(s.getBytes(StandardCharsets.UTF_8)));

        return response.writeWith(body);
    }

    private String getToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst("token");
        if (null != token) {
            if (redisTemplate.hasKey(RedisConst.USER_KEY_PREFIX + token)) {
                return (String) redisTemplate.opsForValue().get(RedisConst.USER_KEY_PREFIX + token);  //k=token v=userId
            }
        } else {
            //3：没有 再获取Cookie
            HttpCookie cookie = request.getCookies().getFirst("token");
            if (null != cookie) {
                String value = cookie.getValue();
                if (null != value) {
                    if (redisTemplate.hasKey(RedisConst.USER_KEY_PREFIX + value)) {
                        return (String) redisTemplate.opsForValue().get(RedisConst.USER_KEY_PREFIX + value);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
