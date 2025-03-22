/**
 * @Time : 2025/3/22 17:43
 * @Author : http://bitcoinbook.cs.princeton.edu/
 * @Comment: A class stores the block data structure.
 **/

package main.java;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;

public class Block {
    // reward from mining one block
    public static final double COINBASE = 25;
    // hash value of this block
    private byte[] hash;
    // hash value of the previous block
    private byte[] prevBlockHash;
    // coinbase transaction
    private Transaction coinbase;
    // array of all collected transactions in this block
    private ArrayList<Transaction> txs;

    /**
     * {@code address} is the address to which the coinbase transaction would go.
     * Create the coinbase transaction, which has only one Output and no Input.
     */
    public Block(byte[] prevHash, PublicKey address) {
        prevBlockHash = prevHash;
        coinbase = new Transaction(COINBASE, address);
        txs = new ArrayList<Transaction>();
    }

    public Transaction getCoinbase() {
        return coinbase;
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] getPrevBlockHash() {
        return prevBlockHash;
    }

    public ArrayList<Transaction> getTransactions() {
        return txs;
    }

    public Transaction getTransaction(int index) {
        return txs.get(index);
    }

    public void addTransaction(Transaction tx) {
        txs.add(tx);
    }

    /**
     * Get the raw data of feature in block
     * (prevBlockHash & raw feature in all transactions).
     */
    public byte[] getRawBlock() {
        ArrayList<Byte> rawBlock = new ArrayList<Byte>();
        if (prevBlockHash != null)
            for (int i = 0; i < prevBlockHash.length; i++)
                rawBlock.add(prevBlockHash[i]);
        for (int i = 0; i < txs.size(); i++) {
            byte[] rawTx = txs.get(i).getRawTx();
            for (int j = 0; j < rawTx.length; j++) {
                rawBlock.add(rawTx[j]);
            }
        }
        byte[] raw = new byte[rawBlock.size()];
        for (int i = 0; i < raw.length; i++)
            raw[i] = rawBlock.get(i);
        return raw;
    }

    /**
     * Get the raw data of feature in block to compute hash.
     */
    public void finalize() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(getRawBlock());
            hash = md.digest();
        } catch (NoSuchAlgorithmException x) {
            x.printStackTrace(System.err);
        }
    }
}
