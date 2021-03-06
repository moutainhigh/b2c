package com.framework.loippi.controller.coupon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.framework.loippi.consts.Constants;
import com.framework.loippi.consts.CouponConstant;
import com.framework.loippi.controller.GenericController;
import com.framework.loippi.entity.Principal;
import com.framework.loippi.entity.coupon.Coupon;
import com.framework.loippi.entity.coupon.CouponDetail;
import com.framework.loippi.entity.coupon.CouponPayDetail;
import com.framework.loippi.entity.coupon.CouponTransLog;
import com.framework.loippi.entity.user.RdMmBasicInfo;
import com.framework.loippi.mybatis.paginator.domain.Order;
import com.framework.loippi.result.common.coupon.CouponPayDetailResult;
import com.framework.loippi.result.common.coupon.CouponUserLogResult;
import com.framework.loippi.service.TwiterIdService;
import com.framework.loippi.service.coupon.CouponDetailService;
import com.framework.loippi.service.coupon.CouponPayDetailService;
import com.framework.loippi.service.coupon.CouponService;
import com.framework.loippi.service.coupon.CouponTransLogService;
import com.framework.loippi.service.user.RdMmBasicInfoService;
import com.framework.loippi.support.Page;
import com.framework.loippi.support.Pageable;
import com.framework.loippi.utils.DateConverter;
import com.framework.loippi.utils.StringUtil;

@Controller("shopCouponController")
@RequestMapping("/admin/plarformShopCoupon")
public class ShopCouponController extends GenericController {

    @Resource
    private CouponService couponService;
    @Resource
    private CouponPayDetailService couponPayDetailService;
    @Resource
    private CouponDetailService couponDetailService;
    @Resource
    private CouponTransLogService couponTransLogService;
    @Resource
    private TwiterIdService twiterIdService;
    @Resource
    private RdMmBasicInfoService rdMmBasicInfoService;

    /**
     * 优惠券列表
     * @param model
     * @param pageNo
     * @param pageSize
     * @param coupon
     * @param sendTimeStr
     * @param useTimeStr
     * @return
     */
/*    @RequestMapping(value = "/queryCouponConditions",method = RequestMethod.POST)
    public String queryCouponConditions(ModelMap model,
                       @RequestParam(required = false, value = "pageNo", defaultValue = "1") int pageNo,
                       @RequestParam(required = false, value = "pageSize", defaultValue = "20") int pageSize,
                       @ModelAttribute Coupon coupon,
                       @RequestParam(required = false, value = "sendTimeStr") String sendTimeStr,
                       @RequestParam(required = false, value = "useTimeStr") String useTimeStr) {
        Pageable pager = new Pageable();
        pager.setPageSize(pageSize);
        pager.setPageNumber(pageNo);
        pager.setOrderProperty("create_time");
        pager.setOrderDirection(Order.Direction.DESC);
        if (!StringUtil.isEmpty(sendTimeStr)) {
            coupon.setSearchSendTime(sendTimeStr);
        }
        if (!StringUtil.isEmpty(useTimeStr)) {
            coupon.setSearchUseTime(useTimeStr);
        }
        pager.setParameter(coupon);
        Page<Coupon> couponList = couponService.findByPage(pager);
        model.addAttribute("page", couponList);
        return "";
    }*/

    /**
     * 优惠券创建
     * @param request
     * @param coupon 优惠券对象
     * @param model
     * @param attr
     * @return
     */
    @RequestMapping(value = "/saveCoupon",method = RequestMethod.POST)
    public String saveCoupon(HttpServletRequest request, @ModelAttribute Coupon coupon, ModelMap model, RedirectAttributes attr,
                             @RequestParam(required = false, value = "couponId") Long couponId
    ) {
        if(StringUtil.isEmpty(coupon.getCouponName())){
            model.addAttribute("msg", "优惠券名称不可以为空");
            return Constants.MSG_URL;
        }
        if(coupon.getCouponValue()==null){
            model.addAttribute("msg", "优惠券面值不可以为空");
            return Constants.MSG_URL;
        }
        if(coupon.getCouponPrice()==null){
            model.addAttribute("msg", "优惠券价格不可以为空");
            return Constants.MSG_URL;
        }
        if(coupon.getWhetherPresent()==null){
            model.addAttribute("msg", "优惠券需注明是否可以赠送");
            return Constants.MSG_URL;
        }
        if(coupon.getReduceType()==null){
            model.addAttribute("msg", "优惠券优惠类型不可以为空");
            return Constants.MSG_URL;
        }
        if(coupon.getUseScope()==null){
            model.addAttribute("msg", "优惠券使用范围不可以为空");
            return Constants.MSG_URL;
        }
        if(coupon.getReceiveType()==null){
            model.addAttribute("msg", "优惠券获取方式不可以为空");
            return Constants.MSG_URL;
        }
        if(coupon.getPersonLimitNum()==null){
            model.addAttribute("msg", "优惠券会员领取数量限制不可以为空");
            return Constants.MSG_URL;
        }
        if(coupon.getTotalLimitNum()==null){
            model.addAttribute("msg", "优惠券总发行数量不可以为空");
            return Constants.MSG_URL;
        }
        if((coupon.getReduceType()==3||coupon.getReduceType()==4)&&coupon.getCouponValue().compareTo(BigDecimal.TEN)!=-1){
            model.addAttribute("msg", "优惠券折扣不可大于十折");
            return Constants.MSG_URL;
        }
        if(coupon.getSendStartTime()==null){
            model.addAttribute("msg", "优惠券开始发放时间为空");
            return Constants.MSG_URL;
        }
        if(coupon.getSendEndTime()==null){
            model.addAttribute("msg", "优惠券结束发放时间为空");
            return Constants.MSG_URL;
        }
        if(coupon.getUseStartTime()==null){
            model.addAttribute("msg", "优惠券开始使用时间为空");
            return Constants.MSG_URL;
        }
        if(coupon.getUseEndTime()==null){
            model.addAttribute("msg", "优惠券结束使用时间为空");
            return Constants.MSG_URL;
        }
        if(coupon.getReduceType()== CouponConstant.DISCOUNT_TYPE_KNOCK){
            if((coupon.getMinAmount()!=null&&coupon.getMinAmount().doubleValue()>0)||(coupon.getMinMi()!=null&&coupon.getMinMi().doubleValue()>0)){
                model.addAttribute("msg", "立减券不可有mi值或者金额限制");
                return Constants.MSG_URL;
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(coupon.getSendEndTime());
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MILLISECOND,0);
        coupon.setSendEndTime(calendar.getTime());
        calendar.setTime(coupon.getUseEndTime());
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MILLISECOND,0);
        coupon.setUseEndTime(calendar.getTime());
        System.out.println(coupon);
        coupon.setId(couponId);
        Subject subject = SecurityUtils.getSubject();
        if(subject!=null){
            Principal principal = (Principal) subject.getPrincipal();
            if (principal != null && principal.getId() != null) {
                Long id = principal.getId();
                String username = principal.getUsername();
                Map<String, String> map =couponService.saveOrEditCoupon(coupon,id,username);
                if (map == null || StringUtil.isEmpty(map.get("code"))) {
                    model.addAttribute("msg", "保存活动信息失败！");
                    return Constants.MSG_URL;
                }

                String code = map.get("code");
                if (StringUtil.isEmpty(code) || code.equals("0")) {
                    String errorMsg = map.get("msg");
                    model.addAttribute("msg", errorMsg);
                    return Constants.MSG_URL;
                }
                //model.addAttribute("msg", "成功");
                return "redirect:coupon/list.jhtml";
            }
        }
        model.addAttribute("msg", "请登录后再进行优惠券相关操作");
        return Constants.MSG_URL;
    }

    /**
     * 优惠券信息查询
     * @param request
     * @param model
     * @param attr
     * @param couponId 优惠券id
     * @return
     */
    @RequestMapping(value = "/viewCouponContent",method = RequestMethod.POST)
    public String viewCouponContent(HttpServletRequest request,  ModelMap model, RedirectAttributes attr,
                             @RequestParam(required = true, value = "couponId") Long couponId
    ) {
        if(couponId==null){
            model.addAttribute("msg", "请传入查询当前优惠券id");
            return Constants.MSG_URL;
        }
        Coupon coupon = couponService.find(couponId);
        if(coupon==null){
            model.addAttribute("msg", "当前id对应优惠券不存在");
            return Constants.MSG_URL;
        }
        model.addAttribute("coupon",coupon);
        return "activity/shop_activity/coupon_edit";//TODO 跳转到展示优惠券详情页面
    }

    /**
     * 根据优惠券id对优惠券进行审核
     * @param request
     * @param model
     * @param attr
     * @param couponId 优惠券id
     * @return
     */
    @RequestMapping(value = "/auditCoupon",method = RequestMethod.POST)
    public String auditCoupon(HttpServletRequest request,  ModelMap model, RedirectAttributes attr,
                                    @RequestParam(required = true, value = "couponId") Long couponId,
                                    @RequestParam(required = true, value = "targetStatus") Integer targetStatus
    ) {
        if(couponId==null){
            model.addAttribute("msg", "请传入查询当前优惠券id");
            return Constants.MSG_URL;
        }
        if(targetStatus!=2&&targetStatus!=3){
            model.addAttribute("msg", "是否审核通过状态码不正确");
            return Constants.MSG_URL;
        }
        Coupon coupon = couponService.find(couponId);
        if(coupon==null){
            model.addAttribute("msg", "当前id对应优惠券不存在");
            return Constants.MSG_URL;
        }
        Integer status = coupon.getStatus();
        if(status!=1){
            model.addAttribute("msg", "当前优惠券不处于待审核状态");
            return Constants.MSG_URL;
        }
        Principal user = (Principal) SecurityUtils.getSubject().getPrincipal();
        if(user==null){
            model.addAttribute("msg", "请登录后进行审核操作");
            return Constants.MSG_URL;
        }
        try {
            couponService.updateCouponState(coupon,targetStatus,user.getId(),user.getUsername());
            if(targetStatus==2){
                model.addAttribute("msg", "审核成功");
            }
            if(targetStatus==3){
                model.addAttribute("msg", "审核失败");
            }
            return "redirect:coupon/list.jhtml";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("msg", "网络异常，请稍后重试");
            return Constants.MSG_URL;
        }
    }

    /**
     * 优惠券订单列表
     * @param request
     * @param pageable
     * @param model
     * @param
     * @return
     */
    @RequestMapping(value = "/Coupon/findCouponPayDetailList")
    public String findCouponPayDetailList(HttpServletRequest request,Pageable pageable,ModelMap model,@ModelAttribute CouponPayDetail param) {
            DateConverter converter = new DateConverter();
        if (param.getPaymentTimeStarS()!=null&&param.getPaymentTimeEndS()!=null&&!"".equals(param.getPaymentTimeStarS())&&!"".equals(param.getPaymentTimeEndS())){
            Date dateStar = converter.convert(param.getPaymentTimeStarS());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateStar);
            calendar.set(Calendar.HOUR_OF_DAY,00);
            calendar.set(Calendar.MINUTE,00);
            calendar.set(Calendar.SECOND,01);
            calendar.set(Calendar.MILLISECOND,0);
            param.setPaymentTimeStar(calendar.getTime());

            Date dateEnd = converter.convert(param.getPaymentTimeEndS());
            calendar.setTime(dateEnd);
            calendar.set(Calendar.HOUR_OF_DAY,23);
            calendar.set(Calendar.MINUTE,59);
            calendar.set(Calendar.SECOND,59);
            calendar.set(Calendar.MILLISECOND,0);
            param.setPaymentTimeEnd(calendar.getTime());
            System.out.println("pstar="+param.getPaymentTimeStarS());
            System.out.println("pend="+param.getPaymentTimeEndS());
        }
        if (param.getCreateTimeStarS()!=null&&param.getCreateTimeEndS()!=null&&!"".equals(param.getCreateTimeStarS())&&!"".equals(param.getCreateTimeEndS())){
            Date dateStar = converter.convert(param.getCreateTimeStarS());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateStar);
            calendar.set(Calendar.HOUR_OF_DAY,00);
            calendar.set(Calendar.MINUTE,00);
            calendar.set(Calendar.SECOND,01);
            calendar.set(Calendar.MILLISECOND,0);
            param.setCreateTimeStar(calendar.getTime());
            Date dateEnd = converter.convert(param.getCreateTimeEndS());
            calendar.setTime(dateEnd);
            calendar.set(Calendar.HOUR_OF_DAY,23);
            calendar.set(Calendar.MINUTE,59);
            calendar.set(Calendar.SECOND,59);
            calendar.set(Calendar.MILLISECOND,0);
            param.setCreateTimeEnd(calendar.getTime());
            System.out.println("cstar="+param.getCreateTimeStarS());
            System.out.println("cend="+param.getCreateTimeEndS());
        }
        pageable.setParameter(param);
        pageable.setOrderProperty("create_time");
        pageable.setOrderDirection(Order.Direction.DESC);
        Page<CouponPayDetail> byPage = couponPayDetailService.findByPage(pageable);
        System.out.println("ss="+byPage.getContent());
        model.addAttribute("page", byPage);
        return "/activity/shop_activity/couponbuy_list";
    }

    /**
     * 优惠券订单详情
     * @param request
     * @param model
     * @param id
     * @param
     * @return
     */
    @RequestMapping(value = "/Coupon/detail/findCouponPayDetail")
    public String findCouponPayDetail(HttpServletRequest request,ModelMap model,@RequestParam(required = true, value = "id")Long id) {
        if(id==null){
            model.addAttribute("msg", "请传入优惠券订单id");
            return Constants.MSG_URL;
        }
        CouponPayDetail couponPayDetail = couponPayDetailService.find(id);
        List<CouponDetail> couponDetailList = new ArrayList<>();
        if(couponPayDetail!=null){
            couponDetailList = couponDetailService.findListByBuyOrderId(couponPayDetail.getId());
        }
        model.addAttribute("page", CouponPayDetailResult.build(couponPayDetail,couponDetailList));
        return "/activity/shop_activity/couponbuy_edit";
    }

    /**
     * 优惠券领取使用明细
     * @param request
     * @param pageable
     * @param model
     * @param param
     * @return
     */
    @RequestMapping(value = "/Coupon/findCouponUserLogList")
    public String findCouponUseLogList(HttpServletRequest request,Pageable pageable,ModelMap model,@ModelAttribute CouponUserLogResult param) {
        DateConverter converter = new DateConverter();
        if (param.getReceiveTimeStarS()!=null&&param.getReceiveTimeEndS()!=null&&!"".equals(param.getReceiveTimeStarS())&&!"".equals(param.getReceiveTimeEndS())){
            Date dateStar = converter.convert(param.getReceiveTimeStarS());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateStar);
            calendar.set(Calendar.HOUR_OF_DAY,00);
            calendar.set(Calendar.MINUTE,00);
            calendar.set(Calendar.SECOND,01);
            calendar.set(Calendar.MILLISECOND,0);
            param.setReceiveTimeStar(calendar.getTime());

            Date dateEnd = converter.convert(param.getReceiveTimeEndS());
            calendar.setTime(dateEnd);
            calendar.set(Calendar.HOUR_OF_DAY,23);
            calendar.set(Calendar.MINUTE,59);
            calendar.set(Calendar.SECOND,59);
            calendar.set(Calendar.MILLISECOND,0);
            param.setReceiveTimeEnd(calendar.getTime());
        System.out.println("star="+param.getReceiveTimeStarS());
        System.out.println("end="+param.getReceiveTimeEndS());
        }
        pageable.setParameter(param);
        pageable.setOrderProperty("receive_time");
        pageable.setOrderDirection(Order.Direction.DESC);
        Object result = couponDetailService.findLogResultByPage(pageable);
        System.out.println("ss="+result);
        model.addAttribute("page", result);
        return "/activity/shop_activity/couponuse_list";
    }

    /**
     * 优惠券基本信息列表
     *
     */
    @RequestMapping("/coupon/list")
    public String list(ModelMap model,
                       @RequestParam(required = false, value = "pageNo", defaultValue = "1") int pageNo,
                       @RequestParam(required = false, value = "pageSize", defaultValue = "20") int pageSize,
                       @ModelAttribute Coupon coupon) {
        //参数整理
        Pageable pager = new Pageable();
        pager.setPageNumber(pageNo);
        pager.setPageSize(pageSize);
        pager.setOrderProperty("create_time");
        pager.setOrderDirection(Order.Direction.DESC);
        pager.setParameter(coupon);
        Page<Coupon> page = couponService.findByPage(pager);
        model.addAttribute("page", page);
        return "/activity/shop_activity/coupon_list";
    }

    /**
     * 优惠券转账日志查询
     *
     */
    @RequestMapping("/coupon/translog")
    public String translogSearch(ModelMap model,
                       @RequestParam(required = false, value = "pageNo", defaultValue = "1") int pageNo,
                       @RequestParam(required = false, value = "pageSize", defaultValue = "20") int pageSize,
                       @ModelAttribute CouponTransLog couponTransLog) {
        //参数整理
        Pageable pager = new Pageable();
        pager.setPageNumber(pageNo);
        pager.setPageSize(pageSize);
        pager.setOrderProperty("trans_time");
        pager.setOrderDirection(Order.Direction.DESC);
        pager.setParameter(couponTransLog);
        Page<CouponTransLog> page = couponTransLogService.findByPage(pager);
        model.addAttribute("page", page);
        return "activity/shop_activity/transfer _list";
    }

    /**
     * 前往新增优惠券页面
     * @param model
     * @param request
     * @return
     */
    @RequestMapping("/forward/edit")
    public String edit(Model model, HttpServletRequest request) {

        return "activity/shop_activity/coupon_edit";
    }

    /**
     * 优惠券下架
     * @param request
     * @param model
     * @param attr
     * @param couponId
     * @return
     */
    @RequestMapping("/shelves")
    public String shelves(HttpServletRequest request,  ModelMap model, RedirectAttributes attr,
                          @RequestParam(required = true, value = "couponId") Long couponId){
        if(couponId==null){
            model.addAttribute("msg", "请传入优惠券id");
            return Constants.MSG_URL;
        }
        Coupon coupon = couponService.find(couponId);
        if(coupon==null){
            model.addAttribute("msg", "当前优惠券不存在");
            return Constants.MSG_URL;
        }
        Principal user = (Principal) SecurityUtils.getSubject().getPrincipal();
        if(user==null){
            model.addAttribute("msg", "请登录后进行审核操作");
            return Constants.MSG_URL;
        }
        coupon.setUpdateId(user.getId());
        coupon.setUpdateTime(new Date());
        coupon.setUpdateName(user.getUsername());
        try {
            HashMap<String,Object> map=couponService.shelvesOrOverdueCoupon(coupon);
            Boolean flag = (Boolean) map.get("flag");
            if(flag){
                return "redirect:coupon/list.jhtml"; 
            }
            String msg = (String) map.get("msg");
            model.addAttribute("msg", msg);
            return Constants.MSG_URL;
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("msg", "网络异常，请稍后重试");
            return Constants.MSG_URL;
        }
    }

    /**
     * 优惠券禁用
     * @param request
     * @param model
     * @param couponDetailId
     * @return
     */
    @RequestMapping("/coupon/disable")
    public String couponDisable(HttpServletRequest request,  ModelMap model,@RequestParam(required = true, value = "couponDetailId") Long couponDetailId){
        if(couponDetailId==null){
            model.addAttribute("msg", "请传入优惠券详情id");
            return Constants.MSG_URL;
        }
        CouponDetail couponDetail = couponDetailService.find(couponDetailId);
        if(couponDetail==null){
            model.addAttribute("msg", "当前优惠券详情不存在");
            return Constants.MSG_URL;
        }
        Principal user = (Principal) SecurityUtils.getSubject().getPrincipal();
        if(user==null){
            model.addAttribute("msg", "请登录后进行审核操作");
            return Constants.MSG_URL;
        }
        if (couponDetail.getUseState()==4){
            model.addAttribute("msg", "当前优惠券详情已禁用，请不要重复操作");
            return Constants.MSG_URL;
        }

        couponDetail.setUseState(4);
        couponDetailService.update(couponDetail);
        model.addAttribute("msg", "禁用成功");
        return Constants.MSG_URL;

    }

    /**
     * 查询优惠券信息
     * @param request
     * @param pageNo
     * @param pageSize
     * @param model
     * @param coupon
     * @return
     */
    @RequestMapping(value = "/coupon/select")
    public String select(HttpServletRequest request,@RequestParam(required = false, value = "pageNo", defaultValue = "1") int pageNo,
                         @RequestParam(required = false, value = "pageSize", defaultValue = "20") int pageSize,ModelMap model,@ModelAttribute Coupon coupon) {
        Pageable pageable = new Pageable();
        pageable.setPageNumber(pageNo);
        pageable.setPageSize(pageSize);
        pageable.setParameter(coupon);
        pageable.setOrderProperty("create_time");
        pageable.setOrderDirection(Order.Direction.DESC);
        model.addAttribute("page", couponService.findByPage(pageable));
        return "/activity/shop_activity/coupon_select";
    }

    /**
     * 后台发放优惠券
     * @param request
     * @param model
     * @param ticketId 优券id
     * @param num 发放数量
     * @param mmCode 会员编号
     * @param remark 备注
     * @return
     */
    @RequestMapping(value = "/couponTicket/send",method = RequestMethod.POST)
    public String sendCouponTicket(HttpServletRequest request, ModelMap model,Long ticketId,Integer num,String mmCode,String remark) {
        Subject subject = SecurityUtils.getSubject();
        String username="";
        if(subject!=null){
            Principal principal = (Principal) subject.getPrincipal();
            if (principal != null && principal.getId() != null) {
                Long id = principal.getId();
                username = principal.getUsername();
            }else {
                model.addAttribute("msg", "请登录后进行操作");
                return Constants.MSG_URL;
            }
        }else {
            model.addAttribute("msg", "请登录后进行操作");
            return Constants.MSG_URL;
        }
        if(ticketId==null){
            model.addAttribute("msg", "请选择需要赠送的优惠券id");
            return Constants.MSG_URL;
        }
        if(StringUtil.isEmpty(mmCode)){
            model.addAttribute("msg", "请选择需要赠送会员编号");
            return Constants.MSG_URL;
        }
        if(num==null){
            model.addAttribute("msg", "请选择需要赠送的优惠券张数");
            return Constants.MSG_URL;
        }
        Coupon coupon = couponService.find(ticketId);
        if(coupon==null){
            model.addAttribute("msg", "当前选择的优惠券不存在");
            return Constants.MSG_URL;
        }
        if(num<1){
            model.addAttribute("msg", "请选择正确的优惠券赠送数量");
            return Constants.MSG_URL;
        }
        RdMmBasicInfo basicInfo = rdMmBasicInfoService.findByMCode(mmCode);
        if(basicInfo==null){
            model.addAttribute("msg", "当前选择的会员不存在");
            return Constants.MSG_URL;
        }
        couponDetailService.sendCouponTicket(coupon,num,basicInfo,remark,username);
        model.addAttribute("msg", "优惠券发放成功");
        model.addAttribute("flag", 1);
        //return Constants.MSG_URL;
        return "/common/travelTicket/grantCoupons/back_message";
        /*return "redirect:/admin/plarformShopCoupon/coupon/list.jhtml";*/
    }

    /**
     * 获取优惠券信息跳转后台发放优惠券
     * @param model
     * @param couponId
     * @return
     */
    @RequestMapping("/couponTicket/findById")
    public String findTicketById(ModelMap model, @RequestParam(required = true, value = "couponId") Long couponId) {
        if(couponId==null){
            model.addAttribute("msg", "优惠券id不可以为空");
            return Constants.MSG_URL;
        }
        Coupon coupon = couponService.find(couponId);
        if(coupon==null){
            model.addAttribute("msg", "优惠券信息异常");
            return Constants.MSG_URL;
        }
        model.addAttribute("data", coupon);
        model.addAttribute("flag", 1);
        return "/common/travelTicket/grantCoupons/edit";
    }
}
