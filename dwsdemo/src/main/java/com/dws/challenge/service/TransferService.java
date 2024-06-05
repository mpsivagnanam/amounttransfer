package com.dws.challenge.service;

import com.dws.challenge.constant.ValidationConstant;
import com.dws.challenge.domain.Account;
import com.dws.challenge.validation.TransferAmountValidation;
import com.dws.challenge.domain.CommonResponse;
import com.dws.challenge.domain.TransferAmount;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
/**
 * Service class will handle the business logic of amount transfer and its validations
 */
@Service
@Slf4j
public class TransferService {

	
  @Getter
  private final AccountsRepository accountsRepository;
  
  @Getter
  private final TransferAmountValidation transferAmountValidation;
  
  @Getter
  private final NotificationService notificationService;

  /**
   * This constructor will initialize the services and repository
   * @param accountsRepository
   * @param transferAmountValidation
   * @param notificationService
   */
  @Autowired
  public TransferService(AccountsRepository accountsRepository, TransferAmountValidation transferAmountValidation, NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
	this.transferAmountValidation = transferAmountValidation;
	this.notificationService = notificationService;
  }
  
 
  /**
   * This method will validate the incoming request and if it success then it will do the transfer.
   * We have synchronized the amount transfer process for thread safe.
   * Once the transfer is completed then it will call notification engine asynchronously
   * @param transferAmount
   * @return
   */
  public CommonResponse transferAmount(TransferAmount transferAmount) {
	log.info("Entered in to transferAmount method. From account {}", transferAmount.getAccountFromId());
	CommonResponse commonResponse = this.transferAmountValidation.amountTransferValidation(transferAmount);
	if(!ObjectUtils.isEmpty(commonResponse.getErrorCode())) {
		log.error("Validation failed for from account {}", transferAmount.getAccountFromId());
		return commonResponse;
	}
	
	try {
		synchronized (this) {
			Account fromAccount =  this.accountsRepository.getAccount(transferAmount.getAccountFromId());
			Account toAccount=  this.accountsRepository.getAccount(transferAmount.getAccountToId());
			
			BigDecimal  fromBalance =	fromAccount.getBalance().subtract(transferAmount.getAmountToTransfer());
			fromAccount.setBalance(fromBalance);
			
			
			BigDecimal  toBalance = toAccount.getBalance().add(transferAmount.getAmountToTransfer());
			toAccount.setBalance(toBalance);
			
			this.accountsRepository.updateAccount(fromAccount);
			this.accountsRepository.updateAccount(toAccount);
			
			commonResponse = new CommonResponse();
			commonResponse.setData(Map.of("status", "success","accountDetails",fromAccount));
			
			amountTransferNotification(transferAmount, fromAccount, toAccount);
		}
		
	} catch(Exception e) {
		log.error("Validation failed for from account {} {} ", transferAmount.getAccountFromId(), e);
		return new CommonResponse(ValidationConstant.ERR_CODE_100, e.getMessage(), null);
	}
	log.info("Exited from transferAmount method. From account {}", transferAmount.getAccountFromId());
	return commonResponse;
  }

	/**
	 * Non-blocking notification process will be called
	 * @param transferAmount
	 * @param fromAccount
	 * @param toAccount
	 */
	private void amountTransferNotification(TransferAmount transferAmount, Account fromAccount, Account toAccount) {
		log.info("Enter in to amountTransferNotification method. From account {}", transferAmount.getAccountFromId());
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				sendNotification(transferAmount, fromAccount, toAccount);
			}
		});
	}

	/**
	 * This method will send the notification
	 * @param transferAmount
	 * @param fromAccount
	 * @param toAccount
	 */
	private void sendNotification(TransferAmount transferAmount, Account fromAccount, Account toAccount) {
		log.info("Enter in to sendNotification method. From account {}", transferAmount.getAccountFromId());
		this.notificationService.notifyAboutTransfer(toAccount, "Credited amount of " + transferAmount.getAmountToTransfer());
		this.notificationService.notifyAboutTransfer(fromAccount, "Debited amount of " + transferAmount.getAmountToTransfer());
		log.info("Exited from sendNotification method. From account {}", transferAmount.getAccountFromId());
	}
}
