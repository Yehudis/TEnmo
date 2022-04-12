package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;
import com.techelevator.tenmo.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class TransferController {

    private TransferDao transferDao;
    private UserDao userDao;

    public TransferController(TransferDao transferDao, UserDao userDao) {
        this.transferDao = transferDao;
        this.userDao = userDao;
    }

    @GetMapping(path = "/account/balance")
    public BigDecimal getAccountBalance(Principal principal) {
        return transferDao.getBalance(userDao.findIdByUsername(principal.getName()));
    }

    @PostMapping(path = "/transfer")
    public void createTransfer(Principal principal, @Valid @RequestBody TransferDTO t) {
        transferDao.createTransfer(userDao.findIdByUsername(principal.getName()), t);
    }

    @GetMapping(path = "/user")
    public List<User> getUsers(Principal principal){
        List<User> users = userDao.findAll();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(principal.getName())){
                users.remove(users.get(i));
            }
        }
        return users;
    }

    @GetMapping(path = "/transfer")
    public List<TransferDTO> list(Principal principal){
        return transferDao.listTransfers(userDao.findIdByUsername(principal.getName()));
    }

    @GetMapping(path = "/transfer/{transferId}")
    public TransferDTO getTransfer(Principal principal, @PathVariable int transferId) {
        return transferDao.getTransferById(userDao.findIdByUsername(principal.getName()), transferId);
    }

    @GetMapping(path = "/transfer/pending")
    public List<TransferDTO> getPendingTransfer(Principal principal){
        return transferDao.getPendingTransfer(userDao.findIdByUsername(principal.getName()));
    }

    @PutMapping(path = "/transfer/{transferId}")
    public void updateTransfer(Principal principal, @PathVariable int transferId, @RequestBody Transfer t){
        transferDao.updateTransfer(userDao.findIdByUsername(principal.getName()), transferId, t);
    }



}
