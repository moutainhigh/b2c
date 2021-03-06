package com.framework.loippi.controller.allinpay;

import com.framework.loippi.entity.order.OrderFundFlow;
import com.framework.loippi.service.order.OrderFundFlowService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.framework.loippi.dao.ShopCommonMessageDao;
import com.framework.loippi.dao.ShopMemberMessageDao;
import com.framework.loippi.dao.order.ShopOrderDao;
import com.framework.loippi.dao.user.RdMmAccountInfoDao;
import com.framework.loippi.dao.user.RdMmAccountLogDao;
import com.framework.loippi.dao.user.RdMmBasicInfoDao;
import com.framework.loippi.dao.user.RdSysPeriodDao;
import com.framework.loippi.entity.ShopCommonMessage;
import com.framework.loippi.entity.ShopMemberMessage;
import com.framework.loippi.entity.order.ShopOrder;
import com.framework.loippi.entity.user.RdMmAccountInfo;
import com.framework.loippi.entity.user.RdMmAccountLog;
import com.framework.loippi.entity.user.RdMmBasicInfo;
import com.framework.loippi.service.TwiterIdService;
import com.framework.loippi.service.user.RdMmBasicInfoService;
import com.framework.loippi.utils.Paramap;

@Slf4j
@Controller
@RequestMapping("/admin/allinpayContract")
public class AllInPayContractController {

    @Resource
    private RdMmBasicInfoService rdMmBasicInfoService;
    @Resource
    private RdMmAccountInfoDao rdMmAccountInfoDao;
    @Resource
    private RdMmAccountLogDao rdMmAccountLogDao;
    @Resource
    private RdMmBasicInfoDao rdMmBasicInfoDao;
    @Resource
    private RdSysPeriodDao rdSysPeriodDao;
    @Autowired
    private TwiterIdService twiterIdService;
    @Resource
    private ShopCommonMessageDao shopCommonMessageDao;
    @Resource
    private ShopMemberMessageDao shopMemberMessageDao;
    @Resource
    private ShopOrderDao shopOrderDao;
    @Resource
    private OrderFundFlowService orderFundFlowService;

    /**
     *  会员网络签约回调
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/signBack.jhtml")
    public void allInPayContractBack(HttpServletRequest request, HttpServletResponse response) throws IOException{
        System.out.println("进入通联");
        String rps = request.getParameter("rps");
        System.out.println(rps);
        JSONObject jsonObject = JSONObject.parseObject(rps);
        String status = jsonObject.getString("status");
        if(status.equals("OK")){
            String contractNo = jsonObject.getString("contractNo");//如果成功，获取会员签约点子协议编号
            System.out.println(contractNo);
            String returnValue =jsonObject.getString("returnValue");
            JSONObject jsonObject1 = JSONObject.parseObject(returnValue);
            String bizUserId = jsonObject1.getString("bizUserId");
            //获取用户的会员编号以及签约成功点子协议编号
            RdMmBasicInfo rdMmBasicInfo = rdMmBasicInfoService.find("mmCode", bizUserId);
            RdMmAccountInfo rdMmAccountInfo = rdMmAccountInfoDao.findAccByMCode(bizUserId);
            if(rdMmBasicInfo!=null&&rdMmAccountInfo!=null){
                rdMmBasicInfo.setContractNo(contractNo);
                rdMmBasicInfo.setAllInContractStatus(1);
                rdMmAccountInfo.setAutomaticWithdrawal(1);
                rdMmBasicInfoService.update(rdMmBasicInfo);
                rdMmAccountInfoDao.update(rdMmAccountInfo);
            }
        }
    }

    /**
     *  会员自动分账成功接收回调
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/cutBack.jhtml")
    public void allInPayCutBack(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String rps = request.getParameter("rps");
        System.out.println(rps);
        JSONObject jsonObject = JSONObject.parseObject(rps);
        if(jsonObject.getString("status").equals("error")){//如果分账失败 则记录失败原因
            /*shopOrder.setCutStatus(3);
            shopOrder.setCutFailInfo(resp.getString("message")+",错误代码："+resp.getString("errorCode"));
            shopOrderDao.update(shopOrder);*/
        }
        if(jsonObject.getString("status").equals("OK")){
            String signedValue = jsonObject.getString("signedValue");
            JSONObject signedValueMap = JSONObject.parseObject(signedValue);
            String payStatus = signedValueMap.getString("payStatus");
            String bizOrderNo = signedValueMap.getString("bizOrderNo");
            List<ShopOrder> shopOrders = shopOrderDao.findByParams(Paramap.create().put("paySn",bizOrderNo));
            ShopOrder shopOrder = shopOrders.get(0);
            if(payStatus.equals("success")){//代付（分账）成功 修改订单内分账状态 扣减积分 生成积分日志
                //1.修改订单
                shopOrder.setCutStatus(2);
                shopOrder.setCutTime(new Date());
                shopOrderDao.update(shopOrder);
                //2.修改积分日志表状态
                String mmCode = shopOrder.getCutGetId();
                HashMap<String, Object> map = new HashMap<>();
                map.put("transTypeCode","WD");
                map.put("accType","SBB");
                map.put("trSourceType","BNK");
                map.put("trOrderOid",shopOrder.getId());
                map.put("accStatus",0);
                RdMmAccountLog rdMmAccountLog =rdMmAccountLogDao.findCutByOrderId(map);
                if(rdMmAccountLog!=null){
                    rdMmAccountLog.setAccStatus(2);
                    rdMmAccountLogDao.updateByCutOrderId(map);
                }
                //3.修改OrderFundFlow
                OrderFundFlow orderFundFlow = orderFundFlowService.find("orderId",shopOrder.getId());
                if(orderFundFlow!=null){
                    orderFundFlow.setCutFlag(1);
                    orderFundFlow.setCutPoint(shopOrder.getCutAmount());
                    orderFundFlow.setCutGetId(shopOrder.getCutGetId());
                    orderFundFlow.setCutTime(new Date());
                    orderFundFlowService.update(orderFundFlow);
                }
                /*RdMmAccountInfo accountInfo = rdMmAccountInfoDao.findAccByMCode(mmCode);*/
                //2.生成积分变更记录
                /*RdMmAccountLog rdMmAccountLog = new RdMmAccountLog();
                rdMmAccountLog.setMmCode(mmCode);
                List<RdMmBasicInfo> basicInfos = rdMmBasicInfoDao.findByParams(Paramap.create().put("mmCode",mmCode));
                rdMmAccountLog.setMmNickName(basicInfos.get(0).getMmNickName());
                rdMmAccountLog.setTransTypeCode("WD");
                rdMmAccountLog.setAccType("SBB");
                rdMmAccountLog.setTrSourceType("BNK");
                rdMmAccountLog.setTrOrderOid(shopOrder.getId());
                rdMmAccountLog.setBlanceBefore(accountInfo.getBonusBlance());
                rdMmAccountLog.setAmount(shopOrder.getCutAmount());
                rdMmAccountLog.setBlanceAfter(accountInfo.getBonusBlance().add(shopOrder.getCutAcc()));
                rdMmAccountLog.setTransDate(new Date());
                String period = rdSysPeriodDao.getSysPeriodService(new Date());
                if(period!=null){
                    rdMmAccountLog.setTransPeriod(period);
                }
                rdMmAccountLog.setPresentationFeeNow((shopOrder.getCutAcc()).subtract(shopOrder.getCutAmount()));
                rdMmAccountLog.setActualWithdrawals(shopOrder.getCutAmount());
                rdMmAccountLog.setTransDesc("平台订单支付自动分账提现");
                rdMmAccountLog.setAutohrizeDesc("平台订单支付自动分账提现");
                rdMmAccountLog.setStatus(3);
                rdMmAccountLog.setAccStatus(2);
                rdMmAccountLogDao.insert(rdMmAccountLog);
                //3.扣减用户积分
                accountInfo.setBonusBlance(accountInfo.getBonusBlance().subtract(shopOrder.getCutAcc()));
                accountInfo.setLastWithdrawalTime(new Date());
                rdMmAccountInfoDao.update(accountInfo);
                //4.生成通知消息
                ShopCommonMessage shopCommonMessage=new ShopCommonMessage();
                shopCommonMessage.setSendUid(accountInfo.getMmCode());
                shopCommonMessage.setType(1);
                shopCommonMessage.setOnLine(1);
                shopCommonMessage.setCreateTime(new Date());
                shopCommonMessage.setBizType(2);
                shopCommonMessage.setIsTop(1);
                shopCommonMessage.setCreateTime(new Date());
                shopCommonMessage.setTitle("自动提现积分扣减通知");
                shopCommonMessage.setContent("已帮您自动提现"+shopOrder.getCutAcc()+"奖励积分到钱包，请在奖励积分明细内查询");
                Long msgId = twiterIdService.getTwiterId();
                shopCommonMessage.setId(msgId);
                shopCommonMessageDao.insert(shopCommonMessage);
                ShopMemberMessage shopMemberMessage=new ShopMemberMessage();
                shopMemberMessage.setBizType(2);
                shopMemberMessage.setCreateTime(new Date());
                shopMemberMessage.setId(twiterIdService.getTwiterId());
                shopMemberMessage.setIsRead(0);
                shopMemberMessage.setMsgId(msgId);
                shopMemberMessage.setUid(Long.parseLong(accountInfo.getMmCode()));
                shopMemberMessageDao.insert(shopMemberMessage);*/
            }
            if(payStatus.equals("fail")){
                //分账失败 归还预扣积分
                HashMap<String, Object> map = new HashMap<>();
                map.put("transTypeCode","AWD");
                map.put("accType","SBB");
                map.put("trSourceType","BNK");
                map.put("trOrderOid",shopOrder.getId());
                map.put("accStatus",0);
                RdMmAccountLog rdMmAccountLog1 =rdMmAccountLogDao.findCutByOrderId(map);
                if(rdMmAccountLog1!=null){
                    rdMmAccountLog1.setAccStatus(1);
                    rdMmAccountLogDao.updateByCutOrderId(map);
                }
                String mmCode = shopOrder.getCutGetId();
                RdMmAccountInfo accountInfo = rdMmAccountInfoDao.findAccByMCode(mmCode);
                RdMmAccountLog rdMmAccountLog = new RdMmAccountLog();
                rdMmAccountLog.setMmCode(accountInfo.getMmCode());
                List<RdMmBasicInfo> basicInfos = rdMmBasicInfoDao.findByParams(Paramap.create().put("mmCode",accountInfo.getMmCode()));
                rdMmAccountLog.setMmNickName(basicInfos.get(0).getMmNickName());
                rdMmAccountLog.setTransTypeCode("CF");
                rdMmAccountLog.setAccType("SBB");
                rdMmAccountLog.setTrSourceType("BNK");
                rdMmAccountLog.setTrOrderOid(shopOrder.getId());
                rdMmAccountLog.setBlanceBefore(accountInfo.getBonusBlance());
                rdMmAccountLog.setAmount(shopOrder.getCutAcc());
                rdMmAccountLog.setBlanceAfter(accountInfo.getBonusBlance().add(shopOrder.getCutAcc()));
                rdMmAccountLog.setTransDate(new Date());
                String period = rdSysPeriodDao.getSysPeriodService(new Date());
                if(period!=null){
                    rdMmAccountLog.setTransPeriod(period);
                }
                rdMmAccountLog.setTransDesc("订单分账失败退还用户奖励积分");
                rdMmAccountLog.setAutohrizeDesc("订单分账失败退还用户奖励积分");
                rdMmAccountLog.setStatus(3);
                rdMmAccountLogDao.insert(rdMmAccountLog);
                accountInfo.setBonusBlance(accountInfo.getBonusBlance().add(shopOrder.getCutAcc()));
                rdMmAccountInfoDao.update(accountInfo);
                //4.生成通知消息
                ShopCommonMessage shopCommonMessage=new ShopCommonMessage();
                shopCommonMessage.setSendUid(accountInfo.getMmCode());
                shopCommonMessage.setType(1);
                shopCommonMessage.setOnLine(1);
                shopCommonMessage.setCreateTime(new Date());
                shopCommonMessage.setBizType(2);
                shopCommonMessage.setIsTop(1);
                shopCommonMessage.setCreateTime(new Date());
                shopCommonMessage.setTitle("自动提现失败积分退还通知");
                shopCommonMessage.setContent("订单自动提现失败，退还"+shopOrder.getCutAcc()+"奖励积分到积分账户");
                Long msgId = twiterIdService.getTwiterId();
                shopCommonMessage.setId(msgId);
                shopCommonMessageDao.insert(shopCommonMessage);
                ShopMemberMessage shopMemberMessage=new ShopMemberMessage();
                shopMemberMessage.setBizType(2);
                shopMemberMessage.setCreateTime(new Date());
                shopMemberMessage.setId(twiterIdService.getTwiterId());
                shopMemberMessage.setIsRead(0);
                shopMemberMessage.setMsgId(msgId);
                shopMemberMessage.setUid(Long.parseLong(accountInfo.getMmCode()));
                shopMemberMessageDao.insert(shopMemberMessage);
                //分账失败 修改订单
                shopOrder.setCutStatus(3);
                shopOrder.setCutAmount(BigDecimal.ZERO);
                shopOrder.setCutAcc(BigDecimal.ZERO);
                shopOrder.setCutFailInfo(signedValueMap.getString("payFailMessage"));
                shopOrderDao.update(shopOrder);
            }
         }
    }

    /**
     * 通联用户转账回调地址
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/transferBack.jhtml")
    public void transferBack(HttpServletRequest request, HttpServletResponse response) throws IOException{
        System.out.println("通联用户之间余额转账");
        String rps = request.getParameter("rps");
        System.out.println(rps);
        /*JSONObject jsonObject = JSONObject.parseObject(rps);
        String status = jsonObject.getString("status");
        if(status.equals("OK")){
            String contractNo = jsonObject.getString("contractNo");//如果成功，获取会员签约点子协议编号
            System.out.println(contractNo);
            String returnValue =jsonObject.getString("returnValue");
            JSONObject jsonObject1 = JSONObject.parseObject(returnValue);
            String bizUserId = jsonObject1.getString("bizUserId");
            //获取用户的会员编号以及签约成功点子协议编号
            RdMmBasicInfo rdMmBasicInfo = rdMmBasicInfoService.find("mmCode", bizUserId);
            RdMmAccountInfo rdMmAccountInfo = rdMmAccountInfoDao.findAccByMCode(bizUserId);
            if(rdMmBasicInfo!=null&&rdMmAccountInfo!=null){
                rdMmBasicInfo.setContractNo(contractNo);
                rdMmBasicInfo.setAllInContractStatus(1);
                rdMmAccountInfo.setAutomaticWithdrawal(1);
                rdMmBasicInfoService.update(rdMmBasicInfo);
                rdMmAccountInfoDao.update(rdMmAccountInfo);
            }
        }*/
    }
}
