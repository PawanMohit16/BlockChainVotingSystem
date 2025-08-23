package net.codejava.model;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Block {
    private String[] data;
    private String previousBlockHash;
    private String blockHash;
    private long timestamp;
    private int nonce;

    public Block(String[] data, String previousBlockHash) {
        this.data = data;
        this.previousBlockHash = previousBlockHash;
        this.timestamp = System.currentTimeMillis();
        this.nonce = 0;
        this.blockHash = calculateHash();
    }

    public String calculateHash() {
        try {
            String dataString = Arrays.toString(data) + previousBlockHash + timestamp + nonce;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(dataString.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!blockHash.substring(0, difficulty).equals(target)) {
            nonce++;
            blockHash = calculateHash();
        }
        System.out.println("Block mined: " + blockHash);
    }

    // Getters and Setters
    public String[] getData() {
        return data;
    }

    public void setData(String[] data) {
        this.data = data;
    }

    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    public void setPreviousBlockHash(String previousBlockHash) {
        this.previousBlockHash = previousBlockHash;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        return "Block{" +
                "data=" + Arrays.toString(data) +
                ", previousBlockHash='" + previousBlockHash + '\'' +
                ", blockHash='" + blockHash + '\'' +
                ", timestamp=" + timestamp +
                ", nonce=" + nonce +
                '}';
    }
}
