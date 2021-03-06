package com.framework.loippi.result.app.cart;

import com.framework.loippi.entity.cart.ShopCart;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.framework.loippi.consts.GoodsState;
import com.framework.loippi.vo.cart.ShopCartVo;
import com.google.common.collect.Lists;

@Data
@Accessors(chain = true)
public class CartResult {

    /**
     * 品牌id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long brandId;

    /**
     * 品牌名称
     */
    private String brandName;
    /**
     * 品牌图标
     */
    private String brandIcon;
    /**
     * 购物车商品项
     */
    private List<CartItemResult> cartItems;

    /**
     * 商品种类数量
     */
    private Integer goodsTypeNum;

    /**
     * 满足包邮的条件金额
     */
    private BigDecimal shippingCouponAmount;


    public static List<CartResult> buildList(List<ShopCartVo> shopCarts, BigDecimal freightAmount) {
        if (CollectionUtils.isEmpty(shopCarts)) {
            return Lists.newArrayList();
        }

        // 根据品牌, 将购物车分组
        List<CartResult> cartResultList = Lists.newArrayList();
        for (ShopCartVo cart : shopCarts) {
            // 是否为失效商品（下架或删除）
            boolean isInvalid = cart.getGoods() != null &&
                cart.getGoods().getState() == GoodsState.GOODS_OPEN_STATE &&
                cart.getGoods().getGoodsShow() != null &&
                cart.getGoods().getGoodsShow() == GoodsState.GOODS_ON_SHOW &&
                cart.getGoods().getIsDel() != null &&
                cart.getGoods().getIsDel() == GoodsState.GOODS_NOT_DELETE;
            if (!isInvalid || cart.getGoodsSpec() == null) {
                // 失效商品商店id
                cart.setGoodsId(-1L);

            }


            // cartResultList首个添加
            if (cartResultList.size() == 0) {
                cartResultList.add(CartResult.build(cart,freightAmount));
            } else {
                // 是否cart的商店id存在cartResultList中
                boolean flag = false;
                for (CartResult result : cartResultList) {
                    // cartResultList中存在cart商店id, 将cart加入
                    if (result.getBrandId().equals(cart.getBrandId())) {
                        result.getCartItems().add(CartItemResult.build(cart));
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    cartResultList.add(CartResult.build(cart, freightAmount));
                }
            }
        }

        return cartResultList;
    }

    public static CartResult build(ShopCartVo cart, BigDecimal freightAmount) {
        Optional<ShopCartVo> optCart = Optional.ofNullable(cart);
        CartResult cartResult = new CartResult()
            .setBrandId(optCart.map(ShopCartVo::getBrandId).orElse(-1L))
            .setBrandName(optCart.map(ShopCartVo::getBrandName).orElse("失效品牌"))
            .setBrandIcon(optCart.map(ShopCart::getBrandIcon).orElse(""))
            .setCartItems(Lists.newArrayList(CartItemResult.build(cart)));
        cartResult.setShippingCouponAmount(freightAmount);
        cartResult.setGoodsTypeNum(cartResult.getCartItems().size());
        return cartResult;
    }
}
