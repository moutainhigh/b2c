package com.framework.loippi.controller.selfMention;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.framework.loippi.consts.Constants;
import com.framework.loippi.controller.BaseController;
import com.framework.loippi.entity.order.ShopOrder;
import com.framework.loippi.entity.order.ShopOrderGoods;
import com.framework.loippi.entity.product.ShopGoods;
import com.framework.loippi.entity.product.ShopGoodsGoods;
import com.framework.loippi.entity.product.ShopGoodsSpec;
import com.framework.loippi.entity.user.RdSysPeriod;
import com.framework.loippi.entity.ware.RdInventoryWarning;
import com.framework.loippi.entity.ware.RdWareAllocation;
import com.framework.loippi.entity.ware.RdWareOrder;
import com.framework.loippi.entity.ware.RdWarehouse;
import com.framework.loippi.mybatis.paginator.domain.Order;
import com.framework.loippi.pojo.selfMention.GoodsType;
import com.framework.loippi.pojo.selfMention.OrderInfo;
import com.framework.loippi.result.auths.AuthsLoginResult;
import com.framework.loippi.result.selfMention.SelfMentionOrderResult;
import com.framework.loippi.result.selfMention.SelfMentionOrderStatistics;
import com.framework.loippi.result.selfMention.SelfMentionShopResult;
import com.framework.loippi.service.order.ShopOrderGoodsService;
import com.framework.loippi.service.order.ShopOrderService;
import com.framework.loippi.service.product.ShopGoodsGoodsService;
import com.framework.loippi.service.product.ShopGoodsService;
import com.framework.loippi.service.product.ShopGoodsSpecService;
import com.framework.loippi.service.user.RdSysPeriodService;
import com.framework.loippi.service.ware.RdInventoryWarningService;
import com.framework.loippi.service.ware.RdWareAllocationService;
import com.framework.loippi.service.ware.RdWarehouseService;
import com.framework.loippi.support.Page;
import com.framework.loippi.support.Pageable;
import com.framework.loippi.utils.ApiUtils;
import com.framework.loippi.utils.JacksonUtil;
import com.framework.loippi.utils.Paramap;
import com.framework.loippi.utils.StringUtil;
import com.framework.loippi.utils.Xerror;
import com.framework.loippi.vo.store.MentionProductVo;
import com.framework.loippi.vo.store.MentionWareGoodsVo;

@Controller("selfMentionController")
@Slf4j
public class SelfMentionController extends BaseController {

    @Resource
    private RdInventoryWarningService rdInventoryWarningService;
    @Resource
    private ShopGoodsService shopGoodsService;
    @Resource
    private ShopGoodsSpecService shopGoodsSpecService;
    @Resource
    private ShopGoodsGoodsService shopGoodsGoodsService;
    @Resource
    private ShopOrderService orderService;
    @Resource
    private RdWarehouseService rdWarehouseService;
    @Resource
    private RdWareAllocationService rdWareAllocationService;
    @Resource
    private RdSysPeriodService rdSysPeriodService;
    @Resource
    private ShopOrderService shopOrderService;
    @Resource
    private ShopOrderGoodsService shopOrderGoodsService;
    /**
     * 点击进入我的小店
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/mention/center.json")
    @ResponseBody
    public String inCenter(HttpServletRequest request) {
        AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);
        if(member==null){
            return ApiUtils.error("当前用户尚未登录");
        }
        String mmCode = member.getMmCode();//店主会员编号
        RdWarehouse rdWarehouse = rdWarehouseService.find("mmCode",mmCode);
        if(rdWarehouse==null){
            return ApiUtils.error("当前用户尚未开店");
        }
        String wareCode = rdWarehouse.getWareCode();//获取仓库号
        List<GoodsType> list=rdInventoryWarningService.findGoodsTypeByWareCode(wareCode);
        Integer goodsTypeNum=0;//商品种类数量
        if(list!=null&&list.size()>0){
            goodsTypeNum=list.size();
        }
        Integer mentionId = rdWarehouse.getMentionId();
        if(mentionId==null){
            return ApiUtils.error("自提店信息异常");
        }
        Integer dailyNum=shopOrderService.findDailyCountByMentionId(mentionId);
        Integer monthNum=shopOrderService.findMonthCountByMentionId(mentionId);
        List<OrderInfo> orderInfos=shopOrderService.findMonthOrderInfo(mentionId);
        BigDecimal total=BigDecimal.ZERO;
        if(orderInfos!=null&&orderInfos.size()>0){
            for (OrderInfo orderInfo : orderInfos) {
                total=total.add(orderInfo.getOrderAmount()).add(orderInfo.getUsePointNum()).subtract(orderInfo.getRefundAmount()).subtract(orderInfo.getRefundPoint());
            }
        }

        SelfMentionShopResult result=SelfMentionShopResult.build(goodsTypeNum,dailyNum,monthNum,total);
        result.setPortrait(member.getAvatar());
        result.setShopName(rdWarehouse.getWareName());
        return ApiUtils.success(result);
    }

    /**
     * 商品列表
     */
    @RequestMapping(value = "/api/mention/goodsList", method = RequestMethod.GET)
    @ResponseBody
    public String goodsList(HttpServletRequest request, Pageable pager) {
        AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);
        if(member==null){
            return ApiUtils.error("当前用户尚未登录");
        }

        String mmCode = member.getMmCode();
        RdWarehouse warehouse = rdWarehouseService.findByMmCode(mmCode);
        if (warehouse == null ) {
            return ApiUtils.error("该会员没有自提仓库");
        }
        String wareCode = warehouse.getWareCode();

        Paramap paramap = Paramap.create();
        //paramap.put("mmCode", member.getMmCode());
        paramap.put("wareCode", wareCode);
        paramap.put("wareStatus", 0);//0正常  1 停用
        if (wareCode == null ) {
            return ApiUtils.error("仓库代码为空");
        }
        pager.setOrderDirection(Order.Direction.DESC);
        pager.setOrderProperty("create_time");
        pager.setParameter(paramap);
        List<MentionWareGoodsVo> list = new ArrayList<MentionWareGoodsVo>();
        Page<RdInventoryWarning> goodsPage = rdInventoryWarningService.findByPage(pager);
        if (goodsPage.getContent().size()==0){
            return ApiUtils.success(list);//空的对象
        }
        /*RdWarehouse warehouse = rdWarehouseService.findByCode(wareCode);
        if (warehouse == null ) {
            return ApiUtils.error("未找到该仓库");
        }*/
        for (RdInventoryWarning inventoryWarning : goodsPage.getContent()) {
            ShopGoods shopGoods = shopGoodsService.find(Long.valueOf(inventoryWarning.getGoodsCode()));

            MentionWareGoodsVo wareGoodsVo = new MentionWareGoodsVo();
            wareGoodsVo.setWareCode(Optional.ofNullable(inventoryWarning.getWareCode()).orElse(""));
            wareGoodsVo.setGoodsName(Optional.ofNullable(shopGoods.getGoodsName()).orElse(""));
            wareGoodsVo.setGoodsImage(Optional.ofNullable(shopGoods.getGoodsImage()).orElse(""));
            wareGoodsVo.setSpecId(Optional.ofNullable(inventoryWarning.getSpecificationId()).orElse(0l));
            wareGoodsVo.setSpecGoodsSpec(Optional.ofNullable(shopGoods.getGoodsName()).orElse(""));
            wareGoodsVo.setGoodsRetailPrice(Optional.ofNullable(shopGoods.getGoodsRetailPrice()).orElse(BigDecimal.ZERO));
            wareGoodsVo.setGoodsMemberPrice(Optional.ofNullable(shopGoods.getGoodsMemberPrice()).orElse(BigDecimal.ZERO));
            wareGoodsVo.setPpv(Optional.ofNullable(shopGoods.getPpv()).orElse(BigDecimal.ZERO));
            wareGoodsVo.setInventory(Optional.ofNullable(inventoryWarning.getInventory()).orElse(0));
            Integer salesNum = orderService.countMentionSales(warehouse.getMentionId(),inventoryWarning.getSpecificationId());//销量
            if (salesNum==null){
                wareGoodsVo.setSales(0);
            }else {
                wareGoodsVo.setSales(salesNum);
            }
            String ware = inventoryWarning.getWareCode();
            Long specId = inventoryWarning.getSpecificationId();
            Integer pNum = rdInventoryWarningService.findProductInventory(ware,specId);
            wareGoodsVo.setProductInventory(pNum);//单品库存
            list.add(wareGoodsVo);
        }
        return ApiUtils.success(list);
    }

    /**
     * 单品商品列表
     */
    @RequestMapping(value = "/api/mention/productGoodsList", method = RequestMethod.GET)
    @ResponseBody
    public String productGoodsList(HttpServletRequest request, @RequestParam(required = false,defaultValue = "1",value = "currentPage")Integer currentPage) {
        AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);
        if(member==null){
            return ApiUtils.error("当前用户尚未登录");
        }

        String mmCode = member.getMmCode();
        RdWarehouse warehouse = rdWarehouseService.findByMmCode(mmCode);
        if (warehouse == null ) {
            return ApiUtils.error("该会员没有自提仓库");
        }
        String wareCode = warehouse.getWareCode();


        //该仓库所有单品的库存数
        List<RdInventoryWarning> inventoryWarningList = rdInventoryWarningService.findByWareCode(wareCode);
        Map<Long,Integer> inventoryMap = new HashMap<>();
        for (RdInventoryWarning inventoryWarning : inventoryWarningList) {
            Long goodsCode = Long.valueOf(inventoryWarning.getGoodsCode());//商品id
            ShopGoods goods = shopGoodsService.find(goodsCode);
            if (goods.getGoodsType()==3){//组合商品
                ShopGoodsSpec spec = shopGoodsSpecService.find(inventoryWarning.getSpecificationId());
                Map<String, String> specMap = JacksonUtil.readJsonToMap(spec.getSpecGoodsSpec());
                Map<String, String> goodsMap = JacksonUtil.readJsonToMap(spec.getSpecName());
                Set<String> keySpec = specMap.keySet();
                Set<String> keyGoods = goodsMap.keySet();
                Iterator<String> itSpec = keySpec.iterator();
                Iterator<String> itGoods = keyGoods.iterator();
                while (itSpec.hasNext() && itGoods.hasNext()) {
                    String specId = itSpec.next();//单品的规格id
                    String goodsId = itGoods.next();//单品的商品id
                    //拿到组合的具体数量情况
                    int joinNum=1;
                    Map<String,Object> mapGGs= new HashMap<>();
                    mapGGs.put("goodId",inventoryWarning.getGoodsCode());
                    mapGGs.put("combineGoodsId",goodsId);
                    ShopGoodsGoods shopGoodsGoodsList = shopGoodsGoodsService.findGoodsGoods(mapGGs);
                    if (shopGoodsGoodsList!=null){
                        joinNum= Optional.ofNullable(shopGoodsGoodsList.getJoinNum()).orElse(1);
                    }

                    Integer inventory = inventoryWarning.getInventory()*joinNum;

                    if (inventoryMap.containsKey(new Long(specId))){//存在
                        Integer num = inventoryMap.get(new Long(specId));
                        Integer total = num + inventory;
                        inventoryMap.put(new Long(specId),total);
                    }else {//不存在
                        inventoryMap.put(new Long(specId),inventory);
                    }

                }
            }else {//普通商品和换购商品(单品)
                if (inventoryMap.containsKey(inventoryWarning.getSpecificationId())){//存在
                    Integer num = inventoryMap.get(inventoryWarning.getSpecificationId());
                    inventoryMap.put(inventoryWarning.getSpecificationId(),num+inventoryWarning.getInventory());
                }else {//不存在
                    inventoryMap.put(inventoryWarning.getSpecificationId(),inventoryWarning.getInventory());
                }
            }

        }

        List<MentionWareGoodsVo> productResults = new ArrayList<MentionWareGoodsVo>();
            
        for(Long specId:inventoryMap.keySet()){//遍历map的键
            Integer num = inventoryMap.get(specId);
            RdInventoryWarning warning = rdInventoryWarningService.findInventoryWarningByWareAndSpecId(wareCode,specId);
            ShopGoods shopGoods = shopGoodsService.find(Long.valueOf(warning.getGoodsCode()));
            ShopGoodsSpec spec = shopGoodsSpecService.find(specId);
            MentionWareGoodsVo wareGoodsVo = new MentionWareGoodsVo();
            wareGoodsVo.setWareCode(Optional.ofNullable(wareCode).orElse(""));
            wareGoodsVo.setGoodsName(Optional.ofNullable(shopGoods.getGoodsName()).orElse(""));
            wareGoodsVo.setGoodsImage(Optional.ofNullable(shopGoods.getGoodsImage()).orElse(""));
            wareGoodsVo.setSpecId(Optional.ofNullable(specId).orElse(0l));
            wareGoodsVo.setSpecGoodsSpec(Optional.ofNullable(shopGoods.getGoodsName()).orElse(""));
            wareGoodsVo.setGoodsRetailPrice(Optional.ofNullable(shopGoods.getGoodsRetailPrice()).orElse(BigDecimal.ZERO));
            wareGoodsVo.setGoodsMemberPrice(Optional.ofNullable(shopGoods.getGoodsMemberPrice()).orElse(BigDecimal.ZERO));
            wareGoodsVo.setPpv(Optional.ofNullable(shopGoods.getPpv()).orElse(BigDecimal.ZERO));
            wareGoodsVo.setInventory(Optional.ofNullable(warning.getInventory()).orElse(0));

            wareGoodsVo.setProductInventory(num);//单品库存
            productResults.add(wareGoodsVo);
        }

        List<MentionWareGoodsVo> productResultList = new ArrayList<MentionWareGoodsVo>();

        int size = productResults.size();
        if (size==0){
            return ApiUtils.success(productResultList);
        }else if (size<=10){
            currentPage = 1;
            productResultList = productResults;
        }else {
            int totalPage = (int) Math.ceil((double) size/10);
            if (currentPage>totalPage){
                currentPage = totalPage;
            }
            int startNum = (currentPage - 1) * 10;
            int endNum = ((currentPage - 1) * 10)+10;
            if (endNum>=size){
                endNum = size;
            }
            productResultList = productResults.subList(startNum, endNum);
        }

        MentionProductVo resultPage = new MentionProductVo();
        resultPage.setPageNum(currentPage);
        resultPage.setPageSize(10);
        resultPage.setPages((int) Math.ceil((double) size/10));
        resultPage.setTotal(size);
        resultPage.setProductResults(productResultList);

        return ApiUtils.success(resultPage);
    }

    /**
     * 欠货商品列表
     */
    @RequestMapping(value = "/api/mention/oweGoodsList", method = RequestMethod.GET)
    @ResponseBody
    public String oweGoodsList(HttpServletRequest request) {
        AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);
        if(member==null){
            return ApiUtils.error("当前用户尚未登录");
        }

        String mmCode = member.getMmCode();
        RdWarehouse warehouse = rdWarehouseService.findByMmCode(mmCode);
        if (warehouse == null ) {
            return ApiUtils.error("该会员没有自提仓库");
        }
        String wareCode = warehouse.getWareCode();

        List<RdInventoryWarning> inventoryWarningList = rdInventoryWarningService.findByWareCodeAndOweInven(wareCode);
        if (inventoryWarningList.size()==0){
            return ApiUtils.error("无欠货商品");
        }
        List<MentionWareGoodsVo> list = new ArrayList<MentionWareGoodsVo>();
        for (RdInventoryWarning inventoryWarning : inventoryWarningList) {
            ShopGoods shopGoods = shopGoodsService.find(Long.valueOf(inventoryWarning.getGoodsCode()));

            MentionWareGoodsVo wareGoodsVo = new MentionWareGoodsVo();
            wareGoodsVo.setWareCode(Optional.ofNullable(inventoryWarning.getWareCode()).orElse(""));
            wareGoodsVo.setGoodsName(Optional.ofNullable(shopGoods.getGoodsName()).orElse(""));
            wareGoodsVo.setGoodsImage(Optional.ofNullable(shopGoods.getGoodsImage()).orElse(""));
            wareGoodsVo.setSpecId(Optional.ofNullable(inventoryWarning.getSpecificationId()).orElse(0l));
            wareGoodsVo.setSpecGoodsSpec(Optional.ofNullable(shopGoods.getGoodsName()).orElse(""));
            wareGoodsVo.setGoodsRetailPrice(Optional.ofNullable(shopGoods.getGoodsRetailPrice()).orElse(BigDecimal.ZERO));
            wareGoodsVo.setGoodsMemberPrice(Optional.ofNullable(shopGoods.getGoodsMemberPrice()).orElse(BigDecimal.ZERO));
            wareGoodsVo.setPpv(Optional.ofNullable(shopGoods.getPpv()).orElse(BigDecimal.ZERO));
            wareGoodsVo.setInventory(Optional.ofNullable(inventoryWarning.getInventory()).orElse(0));
            String ware = inventoryWarning.getWareCode();
            Long specId = inventoryWarning.getSpecificationId();
            list.add(wareGoodsVo);
        }
        return ApiUtils.success(list);
    }

    /**
     * 按自由创建发货单
     */
    @RequestMapping(value = "/api/mention/createOrder", method = RequestMethod.POST)
    @ResponseBody
    public String createOrder(@RequestParam Map<Long, Integer> specIdNumMap, HttpServletRequest request) throws Exception {
        // 检查不为空
        for (Map.Entry<Long, Integer> entry : specIdNumMap.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                return ApiUtils.error(Xerror.PARAM_INVALID);
            }
        }

        AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);

        String mmCode = member.getMmCode();
        RdWarehouse warehouseIn = rdWarehouseService.findByMmCode(mmCode);
        if (warehouseIn == null ) {
            return ApiUtils.error("该会员没有自提仓库");
        }
        String wareCode = warehouseIn.getWareCode();

        RdWareOrder rdWareOrder = new RdWareOrder();
        rdWareOrder.setId(twiterIdService.getTwiterId());
        String orderSn = "DH"+twiterIdService.getTwiterId();
        rdWareOrder.setOrderSn(orderSn);
        rdWareOrder.setStoreId(warehouseIn.getWareCode());
        rdWareOrder.setStoreName(warehouseIn.getWareName());
        rdWareOrder.setMCode(Optional.ofNullable(warehouseIn.getMmCode()).orElse(""));
        rdWareOrder.setConsigneeName(Optional.ofNullable(warehouseIn.getConsigneeName()).orElse(""));
        rdWareOrder.setWarePhone(Optional.ofNullable(warehouseIn.getWarePhone()).orElse(""));
        rdWareOrder.setOrderType(8);
        rdWareOrder.setOrderState(5);//待审
        RdSysPeriod nowPeriod = rdSysPeriodService.getPeriodService(new Date());
        if (nowPeriod==null){
            rdWareOrder.setCreationPeriod("");
        }else {
            rdWareOrder.setCreationPeriod(nowPeriod.getPeriodCode());
        }
        rdWareOrder.setCreateTime(new Date());
        rdWareOrder.setOrderDesc("欠货创建");


        RdWarehouse warehouseOut = rdWarehouseService.findByCode("20192514");//仓库
        //调拨单
        RdWareAllocation wareAllocation = new RdWareAllocation();
        wareAllocation.setWareCodeIn(warehouseIn.getWareCode());
        wareAllocation.setWareNameIn(warehouseIn.getWareName());
        wareAllocation.setWareCodeOut(warehouseOut.getWareCode());
        wareAllocation.setWareNameOut(warehouseOut.getWareName());
        wareAllocation.setAttachAdd("");
        wareAllocation.setStatus(2);
        wareAllocation.setAutohrizeBy("");
        wareAllocation.setAutohrizeDesc("");

        //查找入库仓库为负数的商品
        /*List<RdInventoryWarning> inventoryWarningList = rdInventoryWarningService.findByWareCodeAndOweInven(wareAllocation.getWareCodeIn());
        if (inventoryWarningList.size()==0){
            return ApiUtils.error("无欠货商品");
        }*/

        //创建调拨单和订单
        rdWareAllocationService.addAllocation(rdWareOrder,wareAllocation,specIdNumMap);

        return ApiUtils.success();
    }

    /**
     * 按欠货创建发货单
     */
    @RequestMapping(value = "/api/mention/createOweOrder", method = RequestMethod.POST)
    @ResponseBody
    public String createOweOrder( HttpServletRequest request) throws Exception {

        AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);

        String mmCode = member.getMmCode();
        RdWarehouse warehouseIn = rdWarehouseService.findByMmCode(mmCode);
        if (warehouseIn == null ) {
            return ApiUtils.error("该会员没有自提仓库");
        }
        String wareCode = warehouseIn.getWareCode();

        RdWareOrder rdWareOrder = new RdWareOrder();
        rdWareOrder.setId(twiterIdService.getTwiterId());
        String orderSn = "DH"+twiterIdService.getTwiterId();
        rdWareOrder.setOrderSn(orderSn);
        rdWareOrder.setStoreId(warehouseIn.getWareCode());
        rdWareOrder.setStoreName(warehouseIn.getWareName());
        rdWareOrder.setMCode(Optional.ofNullable(warehouseIn.getMmCode()).orElse(""));
        rdWareOrder.setConsigneeName(Optional.ofNullable(warehouseIn.getConsigneeName()).orElse(""));
        rdWareOrder.setWarePhone(Optional.ofNullable(warehouseIn.getWarePhone()).orElse(""));
        rdWareOrder.setOrderType(8);
        rdWareOrder.setOrderState(5);//待审
        RdSysPeriod nowPeriod = rdSysPeriodService.getPeriodService(new Date());
        if (nowPeriod==null){
            rdWareOrder.setCreationPeriod("");
        }else {
            rdWareOrder.setCreationPeriod(nowPeriod.getPeriodCode());
        }
        rdWareOrder.setCreateTime(new Date());
        rdWareOrder.setOrderDesc("欠货创建");


        RdWarehouse warehouseOut = rdWarehouseService.findByCode("20192514");//仓库
        //调拨单
        RdWareAllocation wareAllocation = new RdWareAllocation();
        wareAllocation.setWareCodeIn(warehouseIn.getWareCode());
        wareAllocation.setWareNameIn(warehouseIn.getWareName());
        wareAllocation.setWareCodeOut(warehouseOut.getWareCode());
        wareAllocation.setWareNameOut(warehouseOut.getWareName());
        wareAllocation.setAttachAdd("");
        wareAllocation.setStatus(2);
        wareAllocation.setAutohrizeBy("");
        wareAllocation.setAutohrizeDesc("");

        //查找入库仓库为负数的商品
        List<RdInventoryWarning> inventoryWarningList = rdInventoryWarningService.findByWareCodeAndOweInven(wareAllocation.getWareCodeIn());
        if (inventoryWarningList.size()==0){
            return ApiUtils.error("无欠货商品");
        }

        //创建调拨单和订单
        rdWareAllocationService.addAllocationOwe(rdWareOrder,wareAllocation,inventoryWarningList);

        return ApiUtils.success();
    }

    /**
     * 获取当前登录用户自提订单信息
     * @param request
     * @param orderState 30:待收货 40：已完成
     * @param pager
     * @return
     */
    @RequestMapping(value = "/api/mention/orders.json")
    @ResponseBody
    public String getOrders(HttpServletRequest request,Integer orderState,Pageable pager) {
        AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);
        if(member==null){
            return ApiUtils.error("当前用户尚未登录");
        }
        String mmCode = member.getMmCode();//店主会员编号
        RdWarehouse rdWarehouse = rdWarehouseService.find("mmCode",mmCode);
        if(rdWarehouse==null){
            return ApiUtils.error("当前用户尚未开店");
        }
        int pageNumber = pager.getPageNumber()-1;
        int pageSize = pager.getPageSize();
        List<ShopOrder> list=shopOrderService.findSelfOrderByPage(rdWarehouse,pageNumber,pageSize,orderState);
        HashMap<Long, List<ShopOrderGoods>> hashMap = new HashMap<>();
        if(list!=null&&list.size()>0){
            for (ShopOrder shopOrder : list) {
                List<ShopOrderGoods> orderGoods = shopOrderGoodsService.findList(Paramap.create().put("orderId",shopOrder.getId()));
                hashMap.put(shopOrder.getId(),orderGoods);
            }
            return ApiUtils.success(SelfMentionOrderResult.buildList(list,hashMap));
        }
        return ApiUtils.success();
    }

    /**
     * 按照月份统计自提店订单数及销售额
     * @param request
     * @param month
     * @return
     */
    @RequestMapping(value = "/api/mention/orderStatistics.json")
    @ResponseBody
    public String orderStatistics(HttpServletRequest request,String month) {
        AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);
        if(member==null){
            return ApiUtils.error("当前用户尚未登录");
        }
        String mmCode = member.getMmCode();//店主会员编号
        RdWarehouse rdWarehouse = rdWarehouseService.find("mmCode",mmCode);
        if(rdWarehouse==null){
            return ApiUtils.error("当前用户尚未开店");
        }
        if(StringUtil.isEmpty(month)){
            return ApiUtils.error("请选择需要查询的月份");
        }
        try {
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM");
            Date monthDate = format.parse(month);
            Calendar instance = Calendar.getInstance();
            instance.setTime(monthDate);
            instance.set(Calendar.DAY_OF_MONTH,1);
            Date firstDay = instance.getTime();
            instance.set(Calendar.DATE, instance.getActualMaximum(instance.DATE));
            Date lastDay = instance.getTime();
            java.text.SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            String str1 = format1.format(firstDay);
            String str2 = format1.format(lastDay);
            String timeLeft=str1+" 00:00:00";
            String timeRight=str2+" 23:59:59";
            HashMap<String, Object> map = new HashMap<>();
            map.put("timeLeft",timeLeft);
            map.put("timeRight",timeRight);
            map.put("mentionId",rdWarehouse.getMentionId());
            SelfMentionOrderStatistics orderStatistics=shopOrderService.statisticsSelfOrderByTime(map);
            if(orderStatistics!=null&&orderStatistics.getOrderIncome()==null){
                orderStatistics.setOrderIncome(BigDecimal.ZERO);
            }
            return ApiUtils.success(orderStatistics);
        } catch (ParseException e) {
            e.printStackTrace();
            return ApiUtils.error("请传入正确的月份");
        }
    }

    /**
     * 按照月份查询自提店调拨单
     * @param request
     * @param month
     * @return
     */
    @RequestMapping(value = "/api/mention/month/wareAllocation.json")
    @ResponseBody
    public String getWareAllocationMonth(HttpServletRequest request,String month) {
        AuthsLoginResult member = (AuthsLoginResult) request.getAttribute(Constants.CURRENT_USER);
        if(member==null){
            return ApiUtils.error("当前用户尚未登录");
        }
        String mmCode = member.getMmCode();//店主会员编号
        RdWarehouse rdWarehouse = rdWarehouseService.find("mmCode",mmCode);
        if(rdWarehouse==null){
            return ApiUtils.error("当前用户尚未开店");
        }
        if(StringUtil.isEmpty(month)){
            return ApiUtils.error("请选择需要查询的月份");
        }
        try {
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM");
            Date monthDate = format.parse(month);
            Calendar instance = Calendar.getInstance();
            instance.setTime(monthDate);
            instance.set(Calendar.DAY_OF_MONTH,1);
            Date firstDay = instance.getTime();
            instance.set(Calendar.DATE, instance.getActualMaximum(instance.DATE));
            Date lastDay = instance.getTime();
            java.text.SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            String str1 = format1.format(firstDay);
            String str2 = format1.format(lastDay);
            String timeLeft=str1+" 00:00:00";
            String timeRight=str2+" 23:59:59";
            List<RdWareAllocation> list = rdWareAllocationService.findList(Paramap.create().put("searchTimeLeft",timeLeft).put("searchTimeRight",timeRight).put("wareCodeIn",rdWarehouse.getWareCode()));
            return ApiUtils.success(list);
        } catch (ParseException e) {
            e.printStackTrace();
            return ApiUtils.error("请传入正确的月份");
        }
    }
}
