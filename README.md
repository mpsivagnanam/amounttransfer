# DWS Challemge

Creating amount transfer functionality

## Excersice : Create Amount transfer API

 Your task is to add functionality for a transfer of money between accounts. Transfers should be
 specified by providing:
 
	**accountFrom id**
	
	**accountTo id**
	
	**amount to transfer between accounts**
	
 The amount to transfer should always be a positive number. It should not be possible for an account to end
 up with negative balance (we do not support overdrafts!


### API Details

Created common api to send all the notification. Based on the channel respective notification will be trigger

**API URL : http://localhost:8080/v1/accounts/transfer**

**Method : Post**

**Request :** 

{
    "accountFromId":"account002",
    "accountToId":"account001",
    "amountToTransfer":200
}

**Response :**

{
    "data": {
        "status": "success",
        "accountDetails": {
            "accountId": "account002",
            "balance": 1800
        }
    }
}

### Implementation

We have implemented amount transfer with single responsibility principle and open close principle.

**AccountsController:** Added transfer amount api in controller

**TransferService:** Service class will handle the business logic of amount transfer and its validations. We have synchronized the amount transfer process for thread safe. Once the transfer is completed then it will call notification engine asynchronously

**TransferAmount:** Transfer amount class will be DTO class to hold the request details

**CommonResponse:** Common response class will return the api response details

### Validation

**TransferAmountValidation:** This validation class will validate the incoming request and if it success then it will do the transfer else it will return error Details

### Test

Please run the below class to validate the amount transfer

**AmountTransferTest:** We have added unit testing and we have covered positive and negative scenario of amount transfer




