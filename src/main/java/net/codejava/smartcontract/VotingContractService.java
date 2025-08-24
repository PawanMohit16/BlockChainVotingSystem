package net.codejava.smartcontract;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.crypto.Credentials;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import java.math.BigInteger;
import java.util.List;

@Service
public class VotingContractService {
    private final Web3j web3j;
    private final Credentials credentials;
    private final Voting votingContract;
    private final String contractAddress;
    private final DefaultGasProvider gasProvider;

    public VotingContractService(
            @Value("${AVALANCHE_RPC_URL}") String rpcUrl,
            @Value("${TEST_PRIVATE_KEY}") String privateKey,
            @Value("${CONTRACT_ADDRESS}") String contractAddress) {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
        this.credentials = Credentials.create(privateKey);
        this.contractAddress = contractAddress;
        this.gasProvider = new DefaultGasProvider();
        // Use chainId 43113 for Avalanche Fuji testnet with EIP-155
        TransactionManager txManager = new RawTransactionManager(
            web3j, 
            credentials,
            43113, // Avalanche Fuji testnet chainId
            5,     // Attempt to retry failed transactions 5 times
            3000   // 3 second delay between retries
        );
        this.votingContract = Voting.load(contractAddress, web3j, txManager, gasProvider);
    }

    public TransactionReceipt vote(BigInteger candidateId) throws Exception {
        return votingContract.vote(candidateId).send();
    }

    public List getActiveCandidateIds() throws Exception {
        return votingContract.getActiveCandidateIds().send();
    }

    public Boolean hasVoted(String address) throws Exception {
        return votingContract.voters(address).send();
    }
}
