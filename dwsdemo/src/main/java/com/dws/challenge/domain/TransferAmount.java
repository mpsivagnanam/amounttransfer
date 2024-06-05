package com.dws.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;
/**
 * This class will be request class to transfer the amount
 */
@Data
public class TransferAmount {
	/**
	 * Set the from account
	 */
	@NotNull
	@NotEmpty
	private String accountFromId;
	
	/**
	 * Set the to account
	 */
	@NotNull
	@NotEmpty
	private String accountToId;
	
	/**
	 * Set the transfer amount
	 */
	@NotNull
	@Min(value = 0, message = "Initial balance must be positive.")
	private BigDecimal amountToTransfer;
	
	

}
