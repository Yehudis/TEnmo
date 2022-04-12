package com.techelevator.tenmo;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.*;

import java.math.BigDecimal;
import java.util.List;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final TransferService transferService = new TransferService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    public static void main(String[] args) throws Exception {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

	private void viewCurrentBalance() {
        System.out.println("Your current balance is: $" + transferService.getBalance(currentUser.getToken()));
	}

	private void viewTransferHistory() {
            List<Transfer> transferList = transferService.listTransfers(currentUser.getToken());
            for (Transfer transfer : transferList) {
                if(transfer.getTransferTypeId() == 2){
                    System.out.println("Transfer ID: " + transfer.getTransferId() + " From/To: " + transfer.getToUsername() + " Amount: " + transfer.getAmount());
                } else if (transfer.getTransferTypeId() == 1 || transfer.getTransferTypeId() == 3) {
                    System.out.println("Transfer ID: " + transfer.getTransferId() + " From/To: " + transfer.getFromUsername() + " Amount: " + transfer.getAmount());
                }
            }

            //todo don't allow choice of invalid option (add exception)
            int transferId = consoleService.promptForInt("Please enter transfer ID to view details (0 to cancel): ");
            if (transferId == 0) {
                mainMenu();
            }

            Transfer transfer = transferService.getTransferById(currentUser.getToken(), transferId);

            System.out.println("Transfer Details");
            System.out.println("Id: " + transfer.getTransferId());
            System.out.println("From: " + transfer.getFromUsername());
            System.out.println("To: " + transfer.getToUsername());
            System.out.println("Type: " + transfer.getType());
            System.out.println("Status: " + transfer.getStatus());
            System.out.println("Amount: " + transfer.getAmount());
        }

	private void viewPendingRequests() {
        List<Transfer> pendingRequests = transferService.getPendingTransfers(currentUser.getToken());
        for(Transfer transfer : pendingRequests){
            System.out.println("Pending Transfer ID: " + transfer.getTransferId() +
                    " To: " + transfer.getToUsername() +
                    " Amount: " + transfer.getAmount());
        }

        //todo don't allow to choose invalid options (add exception)
        int transferId = consoleService.promptForInt("Please enter transfer ID to approve/reject (0 to cancel): ");
        if(transferId == 0){
            mainMenu();
        }
        int approveReject = consoleService.promptForInt("1: Approve\n" +
                "2: Reject\n" +
                "0: Don't approve or reject\n" +
                "---------\n" +
                "Please choose an option: ");
        Transfer t = new Transfer();
        if (approveReject == 0){
            mainMenu();
        } else if (approveReject == 1){
            t.setTransferStatusId(2);
        } else if (approveReject == 2){
            t.setTransferStatusId(3);
        }
        transferService.updateTransfer(currentUser.getToken(), transferId, t);
	}

	private void sendBucks() {
        sendRequestTransfer(2);
    }

    private void requestBucks() {
        sendRequestTransfer(1);

    }

    private void sendRequestTransfer(int type) {
        List<User> userList = transferService.getUsers(currentUser.getToken());
        for (User user : userList) {
            System.out.println("User ID: " + user.getId() + " Name: " + user.getUsername());
        }

        //todo don't allow to choose invalid options (add exception)
        int transferTo = consoleService.promptForInt("Enter ID of user you are sending to (0 to cancel): ");
        if (transferTo == 0) {
            consoleService.printMainMenu();
        } else {
            Transfer t = new Transfer();
            t.setUserId(transferTo);
            t.setAmount(consoleService.promptForBigDecimal("Enter amount: "));
            t.setTransferTypeId(type);

            if (type == 2) {
                BigDecimal balance = transferService.getBalance(currentUser.getToken());
                if (balance.compareTo(t.getAmount()) == -1) {
                    System.out.println("Insufficient Funds");
                } else {
                    transferService.createTransfer(currentUser.getToken(), t);
                }
            } else {
                transferService.createTransfer(currentUser.getToken(), t);
            }

        }
    }



}
