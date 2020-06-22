package com.framework.loippi.entity.ware;

import com.framework.loippi.mybatis.eitity.GenericEntity;
import com.framework.loippi.mybatis.ext.annotation.Column;
import com.framework.loippi.mybatis.ext.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 调拨单
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rd_ware_allocation")
public class RdWareAllocation implements GenericEntity {
    private static final long serialVersionUID = 5081846432919091193L;
    @Column(name = "WID" )
    private int wId;//id
    @Column(name = "WARE_CODE_IN" )
    private String wareCodeIn;//入库代码  yyyyxxxx
    @Column(name = "WARE_NAME_IN" )
    private String wareNameIn;//入库名
    @Column(name = "WARE_CODE_OUT" )
    private String wareCodeOut;//出库代码  yyyyxxxx
    @Column(name = "WARE_NAME_OUT" )
    private String wareNameOut;//出库名
    @Column(name = "ATTACH_ADD" )
    private String attachAdd;//附件地址
    @Column(name = "STATUS" )
    private int status;//状态 -2：拒绝 -1：已取消 1：新单（保存状态） 2：待审 3：已授权
    @Column(name = "WARE_ORDER_SN" )
    private String wareOrderSn;//授权人
    @Column(name = "AUTOHRIZE_BY" )
    private String autohrizeBy;//授权人
    @Column(name = "AUTOHRIZE_TIME" )
    private Date autohrizeTime;//授权时间
    @Column(name = "AUTOHRIZE_DESC" )
    private String autohrizeDesc;//授权说明（同意或不同意的理由）

    //查询字段
    private String searchTimeLeft;//查询时间左边限
    private String searchTimeRight;//查询时间右边限
}
