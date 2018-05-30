package com.bytesvc.service.impl;

import com.bytesvc.ext.TransactionContextRegistry;
import com.bytesvc.ext.TransactionContextWrapper;
import org.bytesoft.compensable.Compensable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bytesvc.ServiceException;
import com.bytesvc.service.IAccountService;

@Service("accountService")
@Compensable(interfaceClass = IAccountService.class, confirmableKey = "accountServiceConfirm", cancellableKey = "accountServiceCancel")
public class AccountServiceImpl implements IAccountService {

	@SuppressWarnings("restriction")
	@javax.annotation.Resource(name = "jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Transactional(rollbackFor = ServiceException.class)
	public void increaseAmount(String acctId, double amount) throws ServiceException {
		int value = this.jdbcTemplate.update("update tb_account_one set frozen = frozen + ? where acct_id = ?", amount, acctId);
		if (value != 1) {
			throw new ServiceException("ERROR!");
		}
		System.out.printf("exec increase: acct= %s, amount= %7.2f%n", acctId, amount);
	}

	@Transactional(rollbackFor = ServiceException.class)
	public void decreaseAmount(String acctId, double amount) throws ServiceException {
		int value = this.jdbcTemplate.update(
				"update tb_account_one set amount = amount - ?, frozen = frozen + ? where acct_id = ?", amount, amount, acctId);
		if (value != 1) {
			throw new ServiceException("ERROR!");
		}
		System.out.printf("exec decrease: acct= %s, amount= %7.2f%n", acctId, amount);
		// throw new ServiceException("rollback");
		TransactionContextWrapper contextWrapper = TransactionContextRegistry.getInstance().getCurrentContextWrapper();
		String param1 = (String) contextWrapper.getAttachment("p1");
		System.out.println("p1 from consumer: " + param1);
		param1 = (String) contextWrapper.getAttachment("gp1");
		System.out.println("gp1 from consumer: " + param1);
		contextWrapper.addAttachment("p2", "new param");
		contextWrapper.addGlobalAttachment("gp2", "I'm everywhere");
	}

}
