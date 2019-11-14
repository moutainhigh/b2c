package com.framework.loippi.service.coupon;

import java.util.ArrayList;

import com.framework.loippi.entity.coupon.CouponDetail;
import com.framework.loippi.service.GenericService;
import com.framework.loippi.support.Pageable;

/**
 * 优惠券单体详情
 */
public interface CouponDetailService extends GenericService<CouponDetail, Long> {
    void updateList(ArrayList<CouponDetail> details);

	Object findLogResultByPage(Pageable pageable);
}