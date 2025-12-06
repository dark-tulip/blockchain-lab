package ru.amvera.services;

import org.hyperledger.fabric.gateway.*;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeoutException;

@Service
public class CreditFabricService {

    // пути под твою машину — как у test-network
    private static final Path NETWORK_CONFIG_PATH = Paths.get(
            "/Users/tansh/edu/fabric-samples/test-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml"
    );

    private static final Path ORG1_MSP_BASE = Paths.get(
            "/Users/tansh/edu/fabric-samples/test-network/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp"
    );

    private static final String MSP_ID = "Org1MSP";
    private static final String IDENTITY_LABEL = "admin";
    private static final String CHANNEL_NAME = "mychannel";
    private static final String CHAINCODE_NAME = "chaincode";

    private final Contract contract;

    static {
        // to SDK, все hostnames из connection-org1.yaml надо мапить на localhost
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }

    public CreditFabricService() throws Exception {
        this.contract = initContract();
    }

    private Contract initContract() throws Exception {
        // Wallet (файловый) рядом с приложением
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);

        // wallet identity Admin@org1
        putAdminIdentity(wallet);

        // Gateway по connection-org1.yaml
        Gateway.Builder builder = Gateway.createBuilder()
                .identity(wallet, IDENTITY_LABEL)
                .networkConfig(NETWORK_CONFIG_PATH)
                .discovery(true);

        Gateway gateway = builder.connect();

        // set канал и чейнкод
        Network network = gateway.getNetwork(CHANNEL_NAME);
        return network.getContract(CHAINCODE_NAME);
    }

    private void putAdminIdentity(Wallet wallet) throws IOException, CertificateException, InvalidKeyException {
        Path certPath = ORG1_MSP_BASE.resolve("signcerts").resolve("cert.pem");
        Path keyDir = ORG1_MSP_BASE.resolve("keystore");

        Path keyPath;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(keyDir)) {
            keyPath = stream.iterator().next();
        }

        X509Certificate certificate;
        PrivateKey privateKey;

        try (Reader certReader = Files.newBufferedReader(certPath, StandardCharsets.UTF_8);
             Reader keyReader = Files.newBufferedReader(keyPath, StandardCharsets.UTF_8)) {
            certificate = Identities.readX509Certificate(certReader);
            privateKey = Identities.readPrivateKey(keyReader);
        }

        Identity identity = Identities.newX509Identity(MSP_ID, certificate, privateKey);
        wallet.put(IDENTITY_LABEL, identity);
    }

    public String createAccount(String id, String owner) throws ContractException, TimeoutException, InterruptedException {
        byte[] result = contract
                .createTransaction("CreditContract:createAccount")
                .submit(id, owner);
        return new String(result, StandardCharsets.UTF_8);
    }

    public String readAccount(String id) throws ContractException {
        byte[] result = contract.evaluateTransaction("CreditContract:readAccount", id);
        return new String(result, StandardCharsets.UTF_8);
    }

    public String deposit(String accountId, int amount) throws ContractException, TimeoutException, InterruptedException {
        byte[] result = contract
                .createTransaction("CreditContract:deposit")
                .submit(accountId, String.valueOf(amount));
        return new String(result, StandardCharsets.UTF_8);
    }

    public String issueLoan(String loanId, String accountId, int principal) throws ContractException, TimeoutException, InterruptedException {
        byte[] result = contract
                .createTransaction("CreditContract:issueLoan")
                .submit(loanId, accountId, String.valueOf(principal));
        return new String(result, StandardCharsets.UTF_8);
    }

    public String readLoan(String loanId) throws ContractException {
        byte[] result = contract.evaluateTransaction("CreditContract:readLoan", loanId);
        return new String(result, StandardCharsets.UTF_8);
    }

    public String repayLoan(String loanId, int amount) throws ContractException, TimeoutException, InterruptedException {
        byte[] result = contract
                .createTransaction("CreditContract:repayLoan")
                .submit(loanId, String.valueOf(amount));
        return new String(result, StandardCharsets.UTF_8);
    }

    public String transfer(String fromId, String toId, int amount) throws ContractException, TimeoutException, InterruptedException {
        byte[] result = contract
                .createTransaction("CreditContract:transfer")
                .submit(fromId, toId, String.valueOf(amount));
        return new String(result, StandardCharsets.UTF_8);
    }
}
