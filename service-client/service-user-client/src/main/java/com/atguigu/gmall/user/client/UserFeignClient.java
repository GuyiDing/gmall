package com.atguigu.gmall.user.client;

import com.atguigu.gmall.model.user.UserAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @title: UserFeignClient
 * @Author LiuXianKun
 * @Date: 2020/12/3 14:44
 */
@FeignClient("service-user")
public interface UserFeignClient {

    @GetMapping("/api/user/passport/inner/address")
    List<UserAddress> getUserAddressList();
}
