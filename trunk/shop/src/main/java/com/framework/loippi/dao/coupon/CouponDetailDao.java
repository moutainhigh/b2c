package com.framework.loippi.dao.coupon;

import com.framework.loippi.entity.coupon.CouponDetail;
import com.framework.loippi.mybatis.dao.GenericDao;

import java.util.ArrayList;

public interface CouponDetailDao extends GenericDao<CouponDetail, Long> {
    void updateList(ArrayList<CouponDetail> details);
}
