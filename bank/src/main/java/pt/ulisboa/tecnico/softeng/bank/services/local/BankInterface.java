package pt.ulisboa.tecnico.softeng.bank.services.local;



import java.util.ArrayList;
import java.util.List;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.FenixFramework;
import pt.ulisboa.tecnico.softeng.bank.domain.Account;
import pt.ulisboa.tecnico.softeng.bank.domain.Bank;
import pt.ulisboa.tecnico.softeng.bank.domain.Client;
import pt.ulisboa.tecnico.softeng.bank.domain.Operation;
import pt.ulisboa.tecnico.softeng.bank.exception.BankException;
import pt.ulisboa.tecnico.softeng.bank.services.local.dataobjects.AccountData;
//import pt.ulisboa.tecnico.softeng.bank.services.local.dataobjects.AccountData;
import pt.ulisboa.tecnico.softeng.bank.services.local.dataobjects.BankData;
import pt.ulisboa.tecnico.softeng.bank.services.local.dataobjects.BankOperationData;
import pt.ulisboa.tecnico.softeng.bank.services.local.dataobjects.ClientData;

public class BankInterface {

	@Atomic(mode = TxMode.WRITE)
	public static String processPayment(String IBAN, int amount) {
		for (Bank bank : FenixFramework.getDomainRoot().getBankSet()) {
			if (bank.getAccount(IBAN) != null) {
				return bank.getAccount(IBAN).withdraw(amount);
			}
		}
		throw new BankException();
	}

	@Atomic(mode = TxMode.WRITE)
	public static String cancelPayment(String paymentConfirmation) {
		Operation operation = getOperationByReference(paymentConfirmation);
		if (operation != null) {
			return operation.revert();
		}
		throw new BankException();
	}

	@Atomic(mode = TxMode.READ)
	public static BankOperationData getOperationData(String reference) {
		Operation operation = getOperationByReference(reference);
		if (operation != null) {
			return new BankOperationData(operation);
		}
		throw new BankException();
	}

	private static Operation getOperationByReference(String reference) {
		for (Bank bank : FenixFramework.getDomainRoot().getBankSet()) {
			Operation operation = bank.getOperation(reference);
			if (operation != null) {
				return operation;
			}
		}
		return null;
	}
	
	@Atomic(mode = TxMode.READ)
	public static List<BankData> getBanks() {
		List<BankData> banks = new ArrayList<>();
		for (Bank bank : FenixFramework.getDomainRoot().getBankSet()) {
			banks.add(new BankData(bank));
		}
		return banks;
	}
	
	@Atomic(mode = TxMode.READ)
	public static List<ClientData> getClients() {
		List<ClientData> clients = new ArrayList<>();
		for (Bank bank : FenixFramework.getDomainRoot().getBankSet()) {
			for (Client client : bank.getClientSet()) {
				clients.add(new ClientData(client));
			}
		}
		return clients;
	}
	
	@Atomic(mode = TxMode.WRITE)
	public static void createOperation(String bankCode, BankOperationData operationData) {
		new Operation(operationData.getType(), operationData.getAccount(), operationData.getValue());
	}
	
	@Atomic(mode = TxMode.WRITE)
	public static void createClient(String bankCode, ClientData clientData) {
		new Client(BankInterface.getBankByCode(bankCode), clientData.getName());
	}
	
	@Atomic(mode = TxMode.WRITE)
	public static void createBank(BankData bankData) {
		new Bank(bankData.getName(), bankData.getCode());
	}
	
	@Atomic(mode = TxMode.WRITE)
	public static void createAccount(String bankCode, String clientID, AccountData accountData) {
		new Account(BankInterface.getBankByCode(bankCode),BankInterface.getClientById(bankCode, clientID), accountData);
	}

	@Atomic(mode = TxMode.READ)
	public static BankData getBankDataByCode(String bankCode) {
		Bank bank = getBankByCode(bankCode);

		if (bank != null) {
			return new BankData(bank);
		} else {
			return null;
		}
	}
	private static Bank getBankByCode(String code) {
		for (Bank bank: FenixFramework.getDomainRoot().getBankSet()) {
			if (bank.getCode().equals(code)) {
				return bank;
			}
		}
		return null;
	}

	@Atomic(mode = TxMode.READ)
	public static ClientData getClientDataByID(String bankCode, String clientID) {
		Client client = getClientById(bankCode, clientID);
		if (client != null){
			return new ClientData(client);
		} else {
			return null;
		}
	}
	
	private static Client getClientById(String bankCode, String clientID){
		Bank bank = getBankByCode(bankCode);
		for (Client client: bank.getClientSet()) {
			if (client.getID().equals(clientID)) {
				return client;
			}
		}
		return null;
	}
}
