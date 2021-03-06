package com.framework.loippi.controller.trade;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.framework.loippi.consts.CartConstant;
import com.framework.loippi.consts.Constants;
import com.framework.loippi.controller.BaseController;
import com.framework.loippi.entity.cart.ShopCartExchange;
import com.framework.loippi.entity.order.ShopOrder;
import com.framework.loippi.entity.order.ShopOrderGoods;
import com.framework.loippi.entity.product.ShopGoodsFreightRule;
import com.framework.loippi.entity.user.RdMmAccountInfo;
import com.framework.loippi.entity.user.RdMmAddInfo;
import com.framework.loippi.entity.user.RdMmBasicInfo;
import com.framework.loippi.entity.user.RdMmRelation;
import com.framework.loippi.entity.user.RdRanks;
import com.framework.loippi.mybatis.paginator.domain.Order;
import com.framework.loippi.param.cart.CartAddParam;
import com.framework.loippi.result.app.cart.CartExchangeCheckOutResult;
import com.framework.loippi.result.app.cart.CartExchangeResult;
import com.framework.loippi.result.auths.AuthsLoginResult;
import com.framework.loippi.service.order.ShopOrderService;
import com.framework.loippi.service.product.ShopCartExchangeService;
import com.framework.loippi.service.product.ShopGoodsFreightRuleService;
import com.framework.loippi.service.product.ShopGoodsService;
import com.framework.loippi.service.user.RdMmAccountInfoService;
import com.framework.loippi.service.user.RdMmAddInfoService;
import com.framework.loippi.service.user.RdMmBasicInfoService;
import com.framework.loippi.service.user.RdMmRelationService;
import com.framework.loippi.service.user.RdRanksService;
import com.framework.loippi.support.Pageable;
import com.framework.loippi.utils.ApiUtils;
import com.framework.loippi.utils.Paramap;
import com.framework.loippi.utils.Xerror;
import com.framework.loippi.vo.cart.ShopCartExchangeVo;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * 换购商品购物车
 */
@Controller("redemptionCartController")
@Slf4j
public class RedemptionCartController extends BaseController {
    @Resource
    private ShopGoodsService goodsService;
    @Resource
    private ShopCartExchangeService shopCartExchangeService;
    @Resource
    private RdMmRelationService rdMmRelationService;
    @Resource
    private ShopGoodsFreightRuleService shopGoodsFreightRuleService;
    @Resource
    private RdRanksService rdRanksService;
    @Resource
    private RdMmBasicInfoService rdMmBasicInfoService;
    @Resource
    private RdMmAddInfoService rdMmAddInfoService;
    @Resource
    private RdMmAccountInfoService rdMmAccountInfoService;
    @Resource
    private ShopOrderService shopOrderService;
    /**
     * 换购商品购物车列表
     */
    @RequestMapping(value = "/api/redemption/cart/list", method = RequestMethod.POST)
    @ResponseBody
    public String list(Pageable pageable, HttpServletRequest request) {
        AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);
        pageable.setParameter(Paramap.create().put("memberId", member.getMmCode()));
        pageable.setOrderDirection(Order.Direction.DESC);
        pageable.setOrderProperty("id");
        List<ShopCartExchangeVo> shopCartExchangeVos = shopCartExchangeService.listWithGoodsAndSpec(pageable);
        RdMmRelation rdMmRelation = rdMmRelationService.find("mmCode",member.getMmCode());
        ShopGoodsFreightRule shopGoodsFreightRule = shopGoodsFreightRuleService.find("memberGradeId",rdMmRelation.getRank());
        List<CartExchangeResult> cartResults = CartExchangeResult.buildList(shopCartExchangeVos,shopGoodsFreightRule.getMinimumOrderAmount());
        return ApiUtils.success(cartResults);
    }

    /**
     * 移出换购商品购物车
     */
    @RequestMapping(value = "/api/redemption/cart/remove", method = RequestMethod.POST)
    @ResponseBody
    public String remove(String cartIds, HttpServletRequest request) {
        if (StringUtils.isEmpty(cartIds)) {
            return ApiUtils.error(Xerror.PARAM_INVALID);
        }

        // cartIds 购物车id字符串, 用逗号隔开
        String[] idsStr = cartIds.split(",");
        Long[] ids = new Long[idsStr.length];
        for (int i = 0; i < idsStr.length; i++) {
            ids[i] = Long.valueOf(idsStr[i]);
        }
        // 删除条数
        shopCartExchangeService.deleteAll(ids);
        return ApiUtils.success();
    }

    /**
     * 加入换购商品购物车
     */
    @RequestMapping(value = "/api/redemption/cart/add", method = RequestMethod.POST)
    @ResponseBody
    public String add(@Valid CartAddParam param, BindingResult vResult, HttpServletRequest request) {
        if (vResult.hasErrors()) {
            return ApiUtils.error(Xerror.PARAM_INVALID);
        }
        AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);
        RdMmRelation rdMmRelation = rdMmRelationService.find("mmCode", member.getMmCode());
        RdRanks rdRanks = rdRanksService.find("rankId", rdMmRelation.getRank());
        shopCartExchangeService.saveExchangeCart(param.getGoodsId(), member.getMmCode(), rdRanks.getRankId(),
                param.getCount(), param.getSpecId(),
                CartConstant.SAVE_TYPE_ADD_TO_CART);
        return ApiUtils.success();
    }

    /**
     * 更新换购商品购物车
     */
    @RequestMapping(value = "/api/redemption/cart/update", method = RequestMethod.POST)
    @ResponseBody
    public String update(@RequestParam long cartId, @RequestParam int num, HttpServletRequest request) {
        if (num < 1) {
            return ApiUtils.error(Xerror.PARAM_INVALID);
        }
        AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);
        //根据商品规格id查询商品规格
        shopCartExchangeService.updateNum(cartId, num, Long.parseLong(member.getMmCode()));
        return ApiUtils.success();
    }

    /**
     * 批量更新换购商品购物车
     */
    @RequestMapping(value = "/api/redemption/cart/updateBatch", method = RequestMethod.POST)
    @ResponseBody
    public String updateBatch(@RequestParam Map<Long, Integer> cartIdNumMap, HttpServletRequest request) {
        // 检查不为空
        for (Map.Entry<Long, Integer> entry : cartIdNumMap.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                return ApiUtils.error(Xerror.PARAM_INVALID);
            }
        }
        AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);
        shopCartExchangeService.updateNumBatch(cartIdNumMap, Long.parseLong(member.getMmCode()));
        return ApiUtils.success();
    }

    /**
     * 换购商品购物车结算
     */
    @RequestMapping(value = "/api/redemption/cart/checkout", method = RequestMethod.POST)
    @ResponseBody
    public String checkout(@RequestParam String cartIds,HttpServletRequest request, Long addressId) {
        if (StringUtils.isBlank(cartIds)) {
            return ApiUtils.error(Xerror.PARAM_INVALID);
        }
        AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);

        //订单类型相关
        RdMmBasicInfo rdMmBasicInfo = rdMmBasicInfoService.find("mmCode", member.getMmCode());
        RdMmRelation rdMmRelation = rdMmRelationService.find("mmCode", member.getMmCode());
        RdMmAccountInfo accountInfo = rdMmAccountInfoService.find("mmCode", member.getMmCode());
        if(accountInfo==null||accountInfo.getRedemptionStatus()==null||accountInfo.getRedemptionStatus()!=0){
            return ApiUtils.error("换购积分账户尚未激活");
        }
        RdRanks rdRanks = rdRanksService.find("rankId", rdMmRelation.getRank());
        ShopGoodsFreightRule shopGoodsFreightRule = shopGoodsFreightRuleService.find("memberGradeId",rdMmRelation.getRank());
        // 获取收货地址
        RdMmAddInfo addr = null;
        if (addressId != null) {
            addr = rdMmAddInfoService.find("aid", addressId);
        } else {
            List<RdMmAddInfo> addrList = rdMmAddInfoService.findList("mmCode", member.getMmCode());
            if (CollectionUtils.isNotEmpty(addrList)) {
                addr = addrList.stream()
                        .filter(item -> item.getDefaultadd() != null && item.getDefaultadd() == 1)
                        .findFirst()
                        .orElse(addrList.get(0));
            }
        }
        Map<String, Object> map = shopCartExchangeService
                .queryTotalPrice(cartIds, member.getMmCode(),addr);
        // 购物车数据
        if (map.get("error").equals("true")) {
            return ApiUtils.error((String) map.get("message"));
        }
        List<ShopCartExchange> cartList = Lists.newArrayList();
        if (StringUtils.isNotEmpty(cartIds) && !"null".equals(cartIds)) {
            String[] cartId = cartIds.split(",");
            if (cartId != null && cartId.length > 0) {
                cartList = shopCartExchangeService.findList("ids", cartId);
            }
        }
        if (CollectionUtils.isEmpty(cartList)) {
            return ApiUtils.error(Xerror.PARAM_INVALID);
        }
        CartExchangeCheckOutResult result = CartExchangeCheckOutResult
                .build(map, cartList, addr,accountInfo,shopGoodsFreightRule.getMinimumOrderAmount());
        return ApiUtils.success(result);
    }

    /**
     * 换购商品购物车数量
     */
    @RequestMapping(value = "/api/redemption/cart/count", method = RequestMethod.POST)
    @ResponseBody
    public String countOfMemberCart(HttpServletRequest request) {
        AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);
        Long count = shopCartExchangeService.count(Paramap.create().put("memberId", member.getMmCode()));
        return ApiUtils.success(count);
    }

    /**
     * 换购商品再次购买
     * @param orderId 原换购订单id
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/redemption/cart/buyAgain", method = RequestMethod.POST)
    @ResponseBody
    public String buyAgain(Long orderId, HttpServletRequest request) {
        try {
            if (orderId == null) {
                return ApiUtils.error(Xerror.PARAM_INVALID);
            }
            AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);
            ShopOrder order = shopOrderService.findWithOrderGoodsById(orderId);
            if (order == null || order.getShopOrderGoodses() == null || order.getShopOrderGoodses().size() < 1) {
                return ApiUtils.error("没有购买记录");
            }
            if (order.getOrderType()==null||order.getOrderType()!=5) {
                return ApiUtils.error("订单类型错误");
            }
            List<ShopCartExchange> cartList = new ArrayList<>();
            for (ShopOrderGoods item : order.getShopOrderGoodses()) {
                ShopCartExchange cart = new ShopCartExchange();
                cart.setGoodsId(item.getGoodsId());
                cart.setMemberId(Long.parseLong(item.getBuyerId()));
                cart.setGoodsNum(item.getGoodsNum());
                cart.setSpecId(item.getSpecId());
                if(item.getIsPresentation()==null||item.getIsPresentation()!=1){
                    cartList.add(cart);
                }
            }
            RdMmRelation rdMmRelation = rdMmRelationService.find("mmCode", member.getMmCode());
            RdRanks rdRanks = rdRanksService.find("rankId", rdMmRelation.getRank());
            List<Long> list = shopCartExchangeService.saveCartList(cartList, member.getMmCode(),rdRanks);
            return checkout(Joiner.on(",").join(list), request, null);
        } catch (Exception e) {
            log.error("再次购买错误", e);
            return ApiUtils.error(e.getMessage());
        }
    }
}
