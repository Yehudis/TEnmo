package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao{

    private final JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate, UserDao userDao){
        this.jdbcTemplate = jdbcTemplate;
        this.userDao = userDao;
    }

    @Override
    public BigDecimal getBalance(int userId) {
        String sql = "SELECT balance FROM account WHERE user_id = ?;";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, userId);
    }

    //
    @Override
    public void createTransfer(int id, TransferDTO t) {
        if(t.getAmount().compareTo(BigDecimal.ZERO) == 1) {
            List<User> userList = userDao.findAll();
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getId() == t.getUserId() && userList.get(i).getId() != id) {
                    String sql = "INSERT INTO transfer(transfer_type_id, transfer_status_id, account_from, account_to, amount)" +
                            " VALUES(?, ?, ?, ?, ?);";
                    fillTransferDetails(id, t);
                    if (t.getTransferTypeId() == 2) {
                        if (getBalance(id).compareTo(t.getAmount()) == -1){
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                        } else {
                            jdbcTemplate.update(sql, 2, 2, t.getAccountFrom(), t.getAccountTo(), t.getAmount());
                            updateBalance(t.getAccountFrom(), t.getAmount().negate());
                            updateBalance(t.getAccountTo(), t.getAmount());
                        }
                    } else {
                        jdbcTemplate.update(sql, 1, 1, t.getAccountFrom(), t.getAccountTo(), t.getAmount());
                    }
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private void fillTransferDetails(int id, TransferDTO t){
        String sql = "SELECT tenmo_user.user_id, account_id " +
                "FROM tenmo_user " +
                "JOIN account ON tenmo_user.user_id = account.user_id " +
                "WHERE tenmo_user.user_id = ? OR tenmo_user.user_id = ?;";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, id, t.getUserId());
        if (t.getTransferTypeId() == 2) {
            while (result.next()) {
                if (result.getInt("user_id") == id) {
                    t.setAccountFrom(result.getInt("account_id"));
                } else {
                    t.setAccountTo(result.getInt("account_id"));
                }
            }
        } else {
            while (result.next()) {
                if (result.getInt("user_id") == id) {
                    t.setAccountTo(result.getInt("account_id"));
                } else {
                    t.setAccountFrom(result.getInt("account_id"));
                }
            }
        }
    }

    @Override
    public List<TransferDTO> listTransfers(int id) {
        List<TransferDTO> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_type_id, username, amount " +
                "FROM tenmo_user " +
                "JOIN account ON tenmo_user.user_id = account.user_id " +
                "JOIN transfer ON account_id = account_from OR account_id = account_to " +
                "WHERE tenmo_user.user_id != ? AND (account_from IN (SELECT account_from FROM transfer " +
                "JOIN account ON account_id = account_from JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                "WHERE tenmo_user.user_id = ?) OR account_to IN (SELECT account_to FROM transfer " +
                "JOIN account ON account_id = account_to JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                "WHERE tenmo_user.user_id = ?))";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, id, id, id);
        while(result.next()){
            TransferDTO transfer = new TransferDTO();
            transfer.setTransferId(result.getInt("transfer_id"));
            transfer.setAmount(result.getBigDecimal("amount"));
            transfer.setTransferTypeId(result.getInt("transfer_type_id"));
            if (result.getInt("transfer_type_id") == 2){
                transfer.setToUsername(result.getString("username"));
            } else {
                transfer.setFromUsername(result.getString("username"));
            }
            transfers.add(transfer);
        }
        return transfers;
    }

    @Override
    public TransferDTO getTransferById(int id, int transferId) {
        TransferDTO transfer = new TransferDTO();
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_id, account_from, account_to, amount, username " +
                "FROM transfer " +
                "JOIN account ON account_id = account_from OR account_id = account_to " +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                "WHERE transfer_id = ? AND tenmo_user.user_id != ? AND (account_from IN (SELECT account_from FROM transfer " +
                "JOIN account ON account_id = account_from JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                "WHERE tenmo_user.user_id = ?) OR account_to IN (SELECT account_to FROM transfer " +
                "JOIN account ON account_id = account_to JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                "WHERE tenmo_user.user_id = ?))";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, transferId, id, id, id);
        if(result.next()){;
            transfer.setTransferId(result.getInt("transfer_id"));
            transfer.setTransferTypeId(result.getInt("transfer_type_id"));
            transfer.setTransferStatusId(result.getInt("transfer_status_id"));
            transfer.setAmount(result.getBigDecimal("amount"));
            if(result.getInt("account_id") == result.getInt("account_to")){
                transfer.setFromUsername("Me Myself and I");
                transfer.setToUsername(result.getString("username"));
            } else {
                transfer.setFromUsername(result.getString("username"));
                transfer.setToUsername("Me Myself and I");
            }
        }
        return transfer;
    }

    @Override
    public List<TransferDTO> getPendingTransfer(int id) {
        List<TransferDTO> transferList = new ArrayList<>();
        String sql = "SELECT transfer_id, username, amount FROM transfer " +
                "JOIN account ON account_id = account_to " +
                "JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                "WHERE transfer_status_id = 1 AND account_from IN (SELECT account_from FROM transfer " +
                "JOIN account ON account_id = account_from JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                "WHERE tenmo_user.user_id = ?)";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, id);
        while(result.next()){
            TransferDTO transfer = new TransferDTO();
            transfer.setTransferId(result.getInt("transfer_id"));
            transfer.setToUsername(result.getString("username"));
            transfer.setAmount(result.getBigDecimal("amount"));
            transferList.add(transfer);
        }
        return transferList;
    }

    @Override
    public void updateTransfer(int id, int transferId, Transfer t) {
        String sql = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?";
        if (t.getTransferStatusId() == 2){
            String balance = "SELECT * FROM transfer WHERE transfer_id = ?;";
            Transfer transfer = jdbcTemplate.queryForObject(balance, Transfer.class, transferId);
            if (getBalance(id).compareTo(transfer.getAmount()) == -1){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            } else {
                jdbcTemplate.update(sql, t.getTransferStatusId(), transferId);
                updateBalance(transfer.getAccountFrom(), transfer.getAmount().negate());
                updateBalance(transfer.getAccountTo(), transfer.getAmount());
            }
        } else if (t.getTransferStatusId() == 3){
            jdbcTemplate.update(sql, t.getTransferStatusId(), transferId);
        }
    }

    private void updateBalance(int account, BigDecimal amount){
        String sql = "UPDATE account SET balance = balance + ? WHERE account_id = ?;";
        jdbcTemplate.update(sql, amount, account);
    }

    //@ResponseStatus( code = HttpStatus.NOT_FOUND, reason = "Transfer Not Found")


}
