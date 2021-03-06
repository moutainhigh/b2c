package com.framework.loippi.dto;

import com.framework.loippi.entity.order.ShopOrderGoods;
import com.framework.loippi.mybatis.eitity.GenericEntity;
import com.framework.loippi.mybatis.ext.annotation.Column;
import com.framework.loippi.mybatis.ext.annotation.Table;
import com.framework.loippi.utils.validator.Words;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Entity - 导出订单表Excel
 *
 * @author zijing
 * @version 2.0
 */
@Data
public class ShopOrderExcel  {

    /** 订单索引id */
    private String id;
    /** 买家id */
    private String buyerId;
    /** 买家姓名 */
    private String buyerName;
    /** 买家手机号码 */
    private String buyerPhone;
    /** 商品数量 */
    private String goodsCount;
    /** 商品总价格 */
    private BigDecimal goodsAmount;
    /** 运费价格 */
    private java.math.BigDecimal shippingFee;
    /** 订单pv值 */
    private String ppv;
    /** 优惠总金额 */
    private BigDecimal discount;
    /** 积分抵扣金额 */
    private BigDecimal pointRmbNum;
    /** 订单应付金额(现金支付) */
    private BigDecimal orderAmount;
    /** 收货人名称*/
    private String receiverName;
    /**  收货人手机*/
    private String receiverPhone;
    /** 订单类型  1.零售价 2.会员价 3.PV大单价 4.优惠额度 5 积分 */
    private String orderTypeName;
    /** 订单状态：0:已取消;5待审核;10:待付款;20:待发货;30:待收货;40:交易完成;50:已提交;60:已确认; */
    private String orderStateName;
    /** 录入业务周期:YYYYMM */
    private String creationPeriod;
    /** 支付方式名称 */
    private String paymentName;
    /** 订单生成时间 */
    private Date createTime;
    /** 支付(付款)时间 */
    private Date paymentTime;
    /** 更新时间 */
    private Date updateTime;
}
