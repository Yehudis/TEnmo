package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class TransferDTO {
    private int transferId;
    private int userId;
    private String fromUsername;
    private String toUsername;
    private int transferTypeId;
    private int transferStatusId;
    private int accountFrom;
    private int accountTo;
    private BigDecimal amount;

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }
    public void setToUsername(String toUsername) {
        this.toUsername = toUsername;
    }
    public void setTransferTypeId(int transferTypeId) {
        this.transferTypeId = transferTypeId;
    }
    public void setTransferStatusId(int transferStatusId) {
        this.transferStatusId = transferStatusId;
    }
    public void setAccountFrom(int accountFrom) {
        this.accountFrom = accountFrom;
    }
    public void setAccountTo(int accountTo) {
        this.accountTo = accountTo;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getTransferId() {
        return transferId;
    }
    public int getUserId() {
        return userId;
    }
    public String getFromUsername() {
        return fromUsername;
    }
    public String getToUsername() {
        return toUsername;
    }
    public int getTransferTypeId() {
        return transferTypeId;
    }
    public int getTransferStatusId() {
        return transferStatusId;
    }
    public int getAccountFrom() {
        return accountFrom;
    }
    public int getAccountTo() {
        return accountTo;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public String getType(){
        return transferTypeId == 1 ? "Request" : "Send";
    }
    public String getStatus(){
        if(transferStatusId == 1){
            return "Pending";
        } else if (transferStatusId == 2){
            return "Approved";
        }
        return "Rejected";
    }

    @Override
    public String toString(){
        return "Transfer Details: \n" +
                "Id: " + getTransferId() + "\n " +
                "From: " + getFromUsername() + "\n " +
                "To: " + getToUsername() + "\n " +
                "Type: " + getType() + "\n " +
                "Status: " + getStatus() + "\n " +
                "Amount: " + getAmount() + "\n ";
    }
}
