package com.framework.loippi.service.impl.user;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.framework.loippi.dao.user.RdMmAccountLogDao;
import com.framework.loippi.dao.user.RdSysPeriodDao;
import com.framework.loippi.entity.user.RdMmAccountLog;
import com.framework.loippi.service.impl.GenericServiceImpl;
import com.framework.loippi.service.user.RdMmAccountLogService;



/**
 * SERVICE - RdMmAccountLog(会员账户交易日志表)
 * 
 * @author dzm
 * @version 2.0
 */
@Service
public class RdMmAccountLogServiceImpl extends GenericServiceImpl<RdMmAccountLog, Long> implements RdMmAccountLogService {
	
	@Autowired
	private RdMmAccountLogDao rdMmAccountLogDao;
	@Autowired
	private RdSysPeriodDao rdSysPeriodDao;

	
	@Autowired
	public void setGenericDao() {
		super.setGenericDao(rdMmAccountLogDao);
	}

	@Override
	public RdMmAccountLog findByTransNumber(Integer transNumber) {
		return rdMmAccountLogDao.findByTransNumber(transNumber);
	}

	@Override
	public int updateCancellWD(Integer transNumber) {
		String period = rdSysPeriodDao.getSysPeriodService(new Date());
		Map<String,Object> map = new HashMap<>();
		map.put("transNumber",transNumber);
		map.put("status",-2);
		map.put("accStatus",1);
		map.put("transPeriod",period);
		map.put("autohrizeDesc","用户主动取消");
		map.put("autohrizeTime",new Date());
		return rdMmAccountLogDao.updateCancellWD(map);
	}

	@Override
	public RdMmAccountLog findCutByOrderId(HashMap<String, Object> map) {
		return rdMmAccountLogDao.findCutByOrderId(map);
	}

	@Override
	public void updateByCutOrderId(HashMap<String, Object> map) {
		rdMmAccountLogDao.updateByCutOrderId(map);
	}

	@Override
	public void updateNickNameByMCode(String nickName, String mmCode) {
		Map<String,Object> map = new HashMap<>();
		map.put("mmNickName",nickName);
		map.put("mmCode",mmCode);
		rdMmAccountLogDao.updateNickNameByMCode(map);
	}
}
