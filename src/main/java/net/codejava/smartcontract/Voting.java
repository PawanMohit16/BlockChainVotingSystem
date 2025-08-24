package net.codejava.smartcontract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

public class Voting extends Contract {
    protected Voting(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super("", contractAddress, web3j, credentials, contractGasProvider);
    }
    protected Voting(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super("", contractAddress, web3j, transactionManager, contractGasProvider);
    }
    public static Voting load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Voting(contractAddress, web3j, credentials, contractGasProvider);
    }
    public static Voting load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Voting(contractAddress, web3j, transactionManager, contractGasProvider);
    }
    // Add contract methods here
    public RemoteCall<org.web3j.protocol.core.methods.response.TransactionReceipt> vote(BigInteger candidateId) {
        // Call the vote function on the smart contract
        Function function = new Function(
            "vote",
            Arrays.asList(new Uint256(candidateId)),
            Collections.emptyList()
        );

        return executeRemoteCallTransaction(function);
    }
    
    @SuppressWarnings("unchecked")
    public RemoteCall<List<BigInteger>> getActiveCandidateIds() {
        // Call getActiveCandidateIds function on the smart contract
        Function function = new Function(
            "getActiveCandidateIds",
            Collections.emptyList(), 
            Arrays.asList(new TypeReference<DynamicArray<Uint256>>() {})
        );

        return new RemoteCall<>(() -> {
            DynamicArray<Uint256> result = (DynamicArray<Uint256>) executeCallSingleValueReturn(function, DynamicArray.class);
            List<Uint256> uint256List = result.getValue();
            return uint256List.stream()
                .map(Uint256::getValue)
                .collect(java.util.stream.Collectors.toList());
        });
    }
    
    public RemoteCall<Boolean> voters(String address) {
        // Call voters mapping on the smart contract
        Function function = new Function(
            "voters",
            Arrays.asList(new Address(address)),
            Arrays.asList(new TypeReference<Bool>() {})
        );

        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }
}
