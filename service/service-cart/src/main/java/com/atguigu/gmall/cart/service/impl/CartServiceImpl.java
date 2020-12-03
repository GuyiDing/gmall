package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @title: CartServiceImpl
 * @Author LiuXianKun
 * @Date: 2020/11/29 21:18
 */
@SuppressWarnings("Duplicates")
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
        CartInfo cartInfo = (CartInfo) redisTemplate.opsForHash().get(userId, skuId.toString());
        // List<CartInfo> cartInfoList = cartInfoMapper.selectList(new QueryWrapper<CartInfo>().eq("user_id", userId));
        if (!StringUtils.isEmpty(cartInfo)) {
            BigDecimal price = productFeignClient.getPrice(skuId);
            cartInfo.setSkuPrice(price);
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            cartInfoMapper.update(cartInfo, new QueryWrapper<CartInfo>().eq("sku_id", skuId).eq("user_id", userId));
        } else {
            //该用户第一次添加商品 需要新建一个购物车
            CartInfo info = new CartInfo();
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            info.setSkuNum(skuNum);
            info.setCartPrice(skuInfo.getPrice());
            info.setImgUrl(skuInfo.getSkuDefaultImg());
            info.setSkuName(skuInfo.getSkuName());
            info.setSkuId(skuId);
            info.setUserId(userId);
            info.setIsChecked(1);
            cartInfoMapper.insert(info);
            //更新缓存
            redisTemplate.opsForHash().put(userId,skuId.toString(),info);
            return info;
        }
        return cartInfo;
    }

    @Override
    public CartInfo toCart(Long skuId, Integer skuNum, String userId) {

//        CartInfo cartInfo = cartInfoMapper.selectOne(new QueryWrapper<CartInfo>().eq("sku_id", skuId)
//                .eq("user_id", userId));
        CartInfo cartInfo = (CartInfo) redisTemplate.opsForHash().get(userId, skuId.toString());
        cartInfo.setSkuNum(skuNum);
        return cartInfo;
    }


    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {
        //如果没有真实用户  只有临时用户
        if (StringUtils.isEmpty(userId)) {
            return findCartInfo(userTempId);
        } else {
            //如果有真实用户  没有临时用户
            if (StringUtils.isEmpty(userTempId)) {
                return findCartInfo(userId);
            } else {
                //如果真实用户和临时用户都有
                return mergeCartInfoList(userId, userTempId);
            }
        }
    }

    @Override
    public void checkedCart(Long skuId, Integer isChecked, String userId) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        cartInfoMapper.update(cartInfo, new QueryWrapper<CartInfo>().eq("user_id", userId).eq("is_checked", 1));
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(new QueryWrapper<CartInfo>().eq("is_checked", 1).eq("user_id", userId));
        if (!CollectionUtils.isEmpty(cartInfoList)) {
            cartInfoList.forEach(cartInfo -> {
                cartInfo.setSkuPrice(productFeignClient.getPrice(cartInfo.getSkuId()));
            });
        }
        return cartInfoList;
    }

    @Override
    public void deleteCart(Long skuId, String userId) {
        cartInfoMapper.delete(new QueryWrapper<CartInfo>().eq("sku_id", skuId).eq("user_id", userId));
    }


    private List<CartInfo> mergeCartInfoList(String userId, String userTempId) {
        List<CartInfo> cartInfoListByUserId = cartInfoMapper.selectList(new QueryWrapper<CartInfo>().eq("user_id", userId));
        if (!CollectionUtils.isEmpty(cartInfoListByUserId)) {
            cartInfoListByUserId.forEach(cartInfo -> {
                cartInfo.setSkuPrice(productFeignClient.getPrice(cartInfo.getSkuId()));
            });
        }
        List<CartInfo> cartInfoListByTempId = cartInfoMapper.selectList(new QueryWrapper<CartInfo>().eq("user_id", userTempId));
        if (!CollectionUtils.isEmpty(cartInfoListByTempId)) {
            cartInfoListByTempId.forEach(cartInfo -> {
                cartInfo.setSkuPrice(productFeignClient.getPrice(cartInfo.getSkuId()));
            });
        }
        //真实用户的购物车为空 返回临时用户的购物车
        if (CollectionUtils.isEmpty(cartInfoListByUserId)) {
            //如果临时用户的购物车也为空
            if (CollectionUtils.isEmpty(cartInfoListByTempId)) {
                return null;
            } else {
                //返回临时用户的购物车集合 并且需要更新自己的user_id
                CartInfo cartInfo = new CartInfo();
                cartInfo.setUserId(userId);

                cartInfoMapper.update(cartInfo, new QueryWrapper<CartInfo>().eq("user_id", userTempId));
                return cartInfoListByTempId;
            }
        } else {
            //真实用户的购物车有  临时用户的购物车为空
            if (CollectionUtils.isEmpty(cartInfoListByTempId)) {
                //返回真实用户的购物车集合
                return cartInfoListByUserId;
            } else {
                //真实用户的购物车以及临时用户的购物车都不为null
                //需要判断临时用户的购物车有没有在真实用户的购物车存在
                // 如果存在,追加数量,如果不存在,把自己的user_id改为真实的并且删除该购物车
                //把真实用户的购物车集合转成map 使用skuId作为判断是否重复的依据如果重复  追加数量并且删除自身记录
                Map<Long, CartInfo> cartInfoMap = cartInfoListByUserId.stream().collect(Collectors.toMap(CartInfo::getSkuId, cartInfoByUserId -> {
                    return cartInfoByUserId;
                }));
                //遍历临时用户的购物车
                cartInfoListByTempId.forEach(cartInfoByTempId -> {
                    CartInfo cartInfoByUserId = cartInfoMap.get(cartInfoByTempId.getSkuId());
                    if (null != cartInfoByUserId) {
                        //有重复数据 追加数量并且删除自身记录
                        cartInfoByUserId.setSkuNum(cartInfoByTempId.getSkuNum() + cartInfoByUserId.getSkuNum());
                        cartInfoByUserId.setIsChecked(1);
                        cartInfoMapper.deleteById(cartInfoByTempId.getId());  //根据主键删除 和updateById略有不同
                    } else {
                        //没有重复的
                        cartInfoMap.put(cartInfoByTempId.getSkuId(), cartInfoByTempId);
                        cartInfoByTempId.setUserId(userId);
                        cartInfoMapper.updateById(cartInfoByTempId);
                    }
                });
                return new ArrayList<>(cartInfoMap.values());
            }
        }
    }

    //查询用户购物车集合  参数  ： 用户ID的意思  （是什么用户暂不考虑）
    private List<CartInfo> findCartInfo(String uid) {

        //1:从缓存查询

//        List<CartInfo> cartInfoList =
//                cartInfoMapper.selectList(new QueryWrapper<CartInfo>()
//                        .eq("user_id", uid));
        List<CartInfo> cartInfoList = redisTemplate.opsForHash().values(uid);
        //2：缓存中有购物车 也应该重新查询实时价格
        if (!CollectionUtils.isEmpty(cartInfoList)) {
            cartInfoList.forEach((cartInfo) -> {
                //实时价格
                cartInfo.setSkuPrice(productFeignClient.getPrice(cartInfo.getSkuId()));
            });
        }
        return cartInfoList;
    }

}
