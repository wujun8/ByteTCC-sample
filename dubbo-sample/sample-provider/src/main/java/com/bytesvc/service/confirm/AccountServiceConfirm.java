package com.bytesvc.service.confirm;

import org.bytesoft.bytetcc.supports.spring.aware.CompensableContextAware;
import org.bytesoft.compensable.CompensableContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bytesvc.ServiceException;
import com.bytesvc.service.IAccountService;

@Service("accountServiceConfirm")
public class AccountServiceConfirm implements IAccountService, CompensableContextAware {

	@SuppressWarnings("restriction")
	@javax.annotation.Resource(name = "jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	private CompensableContext compensableContext;

	@Transactional(rollbackFor = ServiceException.class)
	public void increaseAmount(String acctId, double amount) throws ServiceException {
		int value = this.jdbcTemplate.update(
				"update tb_account_one set amount = amount + ?, frozen = frozen - ? where acct_id = ?", amount, amount, acctId);
		if (value != 1) {
			throw new ServiceException("ERROR!");
		}
		System.out.printf("done increase: acct= %s, amount= %7.2f%n", acctId, amount);
	}

	@Transactional(rollbackFor = ServiceException.class)
	public void decreaseAmount(String acctId, double amount) throws ServiceException {
		int value = this.jdbcTemplate.update("update tb_account_one set frozen = frozen - ? where acct_id = ?", amount, acctId);
		System.out.printf("data-after-restart: %s%n", this.compensableContext.getVariable("data-after-restart"));
		if (value == 1) {
			throw new ServiceException("ERROR!");
		}
		System.out.printf("done decrease: acct= %s, amount= %7.2f%n", acctId, amount);
		System.out.printf("data-after-restart: %s%n", this.compensableContext.getVariable("data-after-restart"));
	}

	public void setCompensableContext(CompensableContext aware) {
		this.compensableContext = aware;
	}
}
