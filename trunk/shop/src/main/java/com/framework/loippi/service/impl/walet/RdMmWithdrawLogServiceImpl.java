package com.framework.loippi.service.impl.walet;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.framework.loippi.dao.user.RdMmBasicInfoDao;
import com.framework.loippi.dao.walet.RdMmWithdrawLogDao;
import com.framework.loippi.entity.walet.RdMmWithdrawLog;
import com.framework.loippi.service.impl.GenericServiceImpl;
import com.framework.loippi.service.wallet.RdMmWithdrawLogService;

/**
 * @author :ldq
 * @date:2020/4/2
 * @description:dubbo com.framework.loippi.service.impl.walet
 */
@Service
@Transactional
public class RdMmWithdrawLogServiceImpl extends GenericServiceImpl<RdMmWithdrawLog, Long> implements RdMmWithdrawLogService {

	@Autowired
	private RdMmWithdrawLogDao rdMmWithdrawLogDao;
	@Autowired
	private RdMmBasicInfoDao rdMmBasicInfoDao;

	@Autowired
	public void setGenericDao() {
		super.setGenericDao(rdMmWithdrawLogDao);
	}

	@Override
	public void updateStatusBySnAndMCode(int withdrawStatus, String withdrawSn) {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("withdrawStatus",withdrawStatus);
		map.put("withdrawSn",withdrawSn);
		rdMmWithdrawLogDao.updateStatusBySnAndMCode(map);
	}
}
