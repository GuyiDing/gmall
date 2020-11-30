package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * @title: CartServiceImpl
 * @Author LiuXianKun
 * @Date: 2020/11/29 21:18
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private CartInfoMapper cartInfoMapper;
    @Resource
    private ProductFeignClient productFeignClient;

    @Override
    public CartInfo addToCart(Long skuId, Integer skuNum, String userId) {
        // 获取购物车的key
        String cartKey = getCartKey(userId);
        CartInfo cartInfo = cartInfoMapper.selectOne(new QueryWrapper<CartInfo>().eq("sku_id", skuId)
                .eq("user_id", userId));
        if (!StringUtils.isEmpty(cartInfo)) {
            BigDecimal price = productFeignClient.getPrice(skuId);
            cartInfo.setCartPrice(price);
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            cartInfo.setIsChecked(1);
            cartInfoMapper.updateById(cartInfo);
        } else {
            //该用户第一次添加商品 需要新建一个购物车
            CartInfo cartInfo1 = new CartInfo();
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            cartInfo1.setSkuNum(skuNum);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setSkuId(skuId);
            cartInfo1.setUserId(userId);
            cartInfo1.setIsChecked(1);
            cartInfoMapper.insert(cartInfo1);
            cartInfo = cartInfo1;
        }
        // 更新缓存
        redisTemplate.boundHashOps(cartKey).put(skuId.toString(), cartInfo);
        setCartInfoExpire(cartKey);
        return cartInfo;
    }

    private void setCartInfoExpire(String cartKey) {
        redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }
}
