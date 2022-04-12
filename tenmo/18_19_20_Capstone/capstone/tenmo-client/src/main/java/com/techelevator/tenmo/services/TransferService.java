package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransferService {
    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public TransferService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public BigDecimal getBalance(String token){
        try{
            return restTemplate.exchange(baseUrl + "account/balance", HttpMethod.GET, makeAuthEntity(token), BigDecimal.class).getBody();
        }
        catch (Exception e) {
            BasicLogger.log("Error getting balance " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public List<User> getUsers(String token){
        List<User> userList = new ArrayList<>();
        try {
            userList = Arrays.asList(restTemplate.exchange(baseUrl + "user", HttpMethod.GET, makeAuthEntity(token), User[].class).getBody());
        }
        catch (Exception e) {
            BasicLogger.log("Error doing transfer " + e.getMessage());;
        }
        return userList;
    }

    public void createTransfer(String token, Transfer t){
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity(t, headers);
            restTemplate.postForObject(baseUrl + "transfer", entity, Transfer.class);
        }
        catch (Exception e) {
            BasicLogger.log("Error doing transfer " + e.getMessage());
        }
    }

    public List<Transfer> listTransfers(String token){
        List<Transfer> transferList = new ArrayList<>();
        try {
            transferList = Arrays.asList(restTemplate.exchange(baseUrl + "/transfer", HttpMethod.GET, makeAuthEntity(token), Transfer[].class).getBody());
        }
        catch (Exception e) {
            BasicLogger.log("Error doing transfer " + e.getMessage());;
        }
        return transferList;
    }

    public Transfer getTransferById(String token, int id){
        Transfer transfer = new Transfer();
        try {
            transfer = restTemplate.exchange(baseUrl + "/transfer/{id}", HttpMethod.GET, makeAuthEntity(token), Transfer.class, id).getBody();
        }
        catch (Exception e) {
            BasicLogger.log("Error getting transfer " + e.getMessage());;
        }
        return transfer;
    }

    public List<Transfer> getPendingTransfers(String token){
        List<Transfer> transferList = new ArrayList<>();
        try {
            transferList = Arrays.asList(restTemplate.exchange(baseUrl + "/transfer/pending", HttpMethod.GET, makeAuthEntity(token), Transfer[].class).getBody());
        }
        catch (Exception e) {
            BasicLogger.log("Error getting transfer " + e.getMessage());;
        }
        return transferList;
    }

    public void updateTransfer(String token, int transferId, Transfer t){
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity(t, headers);
            restTemplate.postForObject(baseUrl + "transfer/{transferId)", entity, Transfer.class, transferId);
        }
        catch (Exception e) {
            BasicLogger.log("Error updating transfer transfer " + e.getMessage());
        }
    }

    private HttpEntity makeAuthEntity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        return entity;
    }
}
