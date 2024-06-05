package com.dws.challenge.validation;

import java.math.BigDecimal;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.dws.challenge.constant.ValidationConstant;
import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.CommonResponse;
import com.dws.challenge.domain.TransferAmount;
import com.dws.challenge.repository.AccountsRepository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class will validate the incoming request
 */
@Service
@Slf4j
public class TransferAmountValidation {
	
	  @Getter
	  private  AccountsRepository accountsRepository;

	  @Autowired
	  public TransferAmountValidation(AccountsRepository accountsRepository) {
	    this.accountsRepository = accountsRepository;
	  }

	/**
	 * This method will validate the from account, to account and transfer amount
	 * @param transferAmount
	 * @return
	 */
	public CommonResponse amountTransferValidation(TransferAmount transferAmount) {
		log.info("Entered in to amountTransferValidation method. From account {}", transferAmount.getAccountFromId());
		
		CommonResponse commonResponse = new CommonResponse();
		StringJoiner errorCode = new StringJoiner(",");
		StringJoiner errorDescription = new StringJoiner(",");
		if(ObjectUtils.isEmpty(this.accountsRepository.getAccount(transferAmount.getAccountFromId()))){
			errorCode.add(ValidationConstant.ERR_CODE_101);
			errorDescription.add(ValidationConstant.ERR_CODE_101_DESC+  transferAmount.getAccountFromId());
		}
		
		if(ObjectUtils.isEmpty(this.accountsRepository.getAccount(transferAmount.getAccountToId()))){
			errorCode.add(ValidationConstant.ERR_CODE_102);
			errorDescription.add(ValidationConstant.ERR_CODE_102_DESC+  transferAmount.getAccountToId());
		}
		
		if(!ObjectUtils.isEmpty(transferAmount.getAmountToTransfer())){
			
			if(transferAmount.getAmountToTransfer().intValue() < 1) {
				errorCode.add(ValidationConstant.ERR_CODE_103);
				errorDescription.add(ValidationConstant.ERR_CODE_103_DESC+  transferAmount.getAmountToTransfer());
			}
			
			Account fromAccount = this.accountsRepository.getAccount(transferAmount.getAccountFromId());
			
			if(!ObjectUtils.isEmpty(fromAccount)) {
				BigDecimal balance =  fromAccount.getBalance().subtract(transferAmount.getAmountToTransfer());
				if(balance.intValue() < 0) {
					errorCode.add(ValidationConstant.ERR_CODE_104);
					errorDescription.add(ValidationConstant.ERR_CODE_104_DESC+  fromAccount.getBalance());
				}
			}
			
		}
		
		commonResponse.setErrorCode(errorCode.toString());
		commonResponse.setErrorDescription(errorDescription.toString());
		log.info("Exited from amountTransferValidation method. From account {}", transferAmount.getAccountFromId());
		return commonResponse;
		
	}


	public TransferAmountValidation() {
		super();
	}
}
