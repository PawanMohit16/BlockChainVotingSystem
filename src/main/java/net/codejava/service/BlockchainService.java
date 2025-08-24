package net.codejava.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.reactivex.disposables.Disposable;
import java.math.BigInteger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.Bool;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

@Service
public class BlockchainService {
    private final Web3j web3j;
    private final String contractAddress;
    private final Credentials credentials;
    private final TransactionManager transactionManager;
    private final ObjectMapper objectMapper;
    private final DefaultGasProvider gasProvider;

    public BlockchainService(
            @Value("${AVALANCHE_RPC_URL}") String rpcUrl,
            @Value("${TEST_PRIVATE_KEY}") String privateKey) {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
        this.credentials = Credentials.create(privateKey);
        // Use chainId 43113 for Avalanche Fuji testnet with EIP-155
        this.transactionManager = new RawTransactionManager(
            web3j, 
            credentials,
            43113, // Avalanche Fuji testnet chainId
            5,     // Attempt to retry failed transactions 5 times
            3000   // 3 second delay between retries
        );
        this.objectMapper = new ObjectMapper();
        this.gasProvider = new DefaultGasProvider();

        // Read contract address and ABI from fuji.json
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get("deployments/fuji.json")));
            JsonNode root = objectMapper.readTree(jsonContent);
            this.contractAddress = root.get("contract").get("address").asText();
        } catch (Exception e) {
            throw new RuntimeException("Error loading contract details", e);
        }
    }

    public TransactionReceipt addCandidate(String name, String party, String imageHash) {
        try {
            Function function = new Function(
                "addCandidate", 
                Arrays.asList(
                    new Utf8String(name),
                    new Utf8String(party),
                    new Utf8String(imageHash)
                ),
                Collections.emptyList()
            );

            String encodedFunction = FunctionEncoder.encode(function);
            org.web3j.protocol.core.methods.response.EthSendTransaction transaction = transactionManager.sendTransaction(
                gasProvider.getGasPrice(),
                gasProvider.getGasLimit(),
                contractAddress,
                encodedFunction,
                BigInteger.ZERO
            );
            String txHash = transaction.getTransactionHash();
            return web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to add candidate to blockchain", e);
        }
    }

    public TransactionReceipt vote(BigInteger candidateId) {
        try {
            Function function = new Function(
                "vote",
                Arrays.asList(new Uint256(candidateId)),
                Collections.emptyList()
            );

            String encodedFunction = FunctionEncoder.encode(function);
            org.web3j.protocol.core.methods.response.EthSendTransaction transaction = transactionManager.sendTransaction(
                gasProvider.getGasPrice(),
                gasProvider.getGasLimit(),
                contractAddress,
                encodedFunction,
                BigInteger.ZERO
            );
            String txHash = transaction.getTransactionHash();
            return web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to record vote on blockchain", e);
        }
    }

    public boolean hasVoted(String address) {
        try {
            Function function = new Function(
                "voters",
                Arrays.asList(new org.web3j.abi.datatypes.Address(address)),
                Arrays.asList(new TypeReference<Bool>() {})
            );

            String encodedFunction = FunctionEncoder.encode(function);
            EthCall ethCall = web3j.ethCall(
                Transaction.createEthCallTransaction(
                    credentials.getAddress(),
                    contractAddress,
                    encodedFunction
                ),
                DefaultBlockParameterName.LATEST
            ).send();

            return Boolean.parseBoolean(ethCall.getValue());
        } catch (Exception e) {
            throw new RuntimeException("Failed to check voter status on blockchain", e);
        }
    }

    public BigInteger getVoteCount(BigInteger candidateId) {
        try {
            Function function = new Function(
                "getVotes",
                Arrays.asList(new Uint256(candidateId)),
                Arrays.asList(new TypeReference<Uint256>() {})
            );

            String encodedFunction = FunctionEncoder.encode(function);
            EthCall ethCall = web3j.ethCall(
                Transaction.createEthCallTransaction(
                    credentials.getAddress(),
                    contractAddress,
                    encodedFunction
                ),
                DefaultBlockParameterName.LATEST
            ).send();

            return new BigInteger(ethCall.getValue().substring(2), 16);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get vote count from blockchain", e);
        }
    }
}
