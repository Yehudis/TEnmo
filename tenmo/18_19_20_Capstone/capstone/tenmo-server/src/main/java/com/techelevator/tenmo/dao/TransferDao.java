package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {
    BigDecimal getBalance(int id);

    void createTransfer(int id, TransferDTO t);

    List<TransferDTO> listTransfers(int id);

    TransferDTO getTransferById(int id, int transferId);

    List<TransferDTO> getPendingTransfer(int id);

    void updateTransfer(int id, int transferId, Transfer t);
}

