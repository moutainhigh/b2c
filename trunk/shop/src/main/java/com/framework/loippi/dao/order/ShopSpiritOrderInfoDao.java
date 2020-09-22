package com.framework.loippi.dao.order;

import java.util.Map;

import com.framework.loippi.entity.order.ShopSpiritOrderInfo;
import com.framework.loippi.mybatis.dao.GenericDao;

/**
 * @author :ldq
 * @date:2020/9/18
 * @description:dubbo com.framework.loippi.dao.order
 */
public interface ShopSpiritOrderInfoDao extends GenericDao<ShopSpiritOrderInfo, Long> {
	ShopSpiritOrderInfo findByOrderIdAndSpecId(Map<String, Object> map);
}
