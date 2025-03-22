/**
 * @Time : 2025/3/8 12:27
 * @Author : http://bitcoinbook.cs.princeton.edu/
 * @Comment: The UTXO class that represents an unspent transaction output.
 **/

package main.java;

import java.util.Arrays;

public class UTXO implements Comparable<UTXO> {
    // hash of the transaction from which this UTXO originates
    private byte[] txHash;
    // index of the corresponding output in said transaction
    private int index;

    /**
     * Creates a new UTXO corresponding to the output with index in the transaction whose
     * hash is {@code txHash}.
     */
    public UTXO(byte[] txHash, int index) {
        this.txHash = Arrays.copyOf(txHash, txHash.length);
        this.index = index;
    }

    /**
     * @return the transaction hash of this UTXO
     */
    public byte[] getTxHash() {
        return txHash;
    }

    /**
     * @return the index of this UTXO
     */
    public int getIndex() {
        return index;
    }

    /**
     * Compares this UTXO to the one specified by {@code other}, considering them equal if they have
     * {@code txHash} arrays with equal contents and equal {@code index} values.
     */
    public boolean equals(Object other) {
        // check other is null or not
        if (other == null) {
            return false;
        }
        // check class is equal or not
        if (getClass() != other.getClass()) {
            return false;
        }
        // check txHash and index is equal or not
        UTXO utxo = (UTXO) other;
        byte[] hash = utxo.txHash;
        int in = utxo.index;
        if (hash.length != txHash.length || index != in)
            return false;
        for (int i = 0; i < hash.length; i++) {
            if (hash[i] != txHash[i])
                return false;
        }
        return true;
    }

    /**
     * Simple implementation of a UTXO hashCode that respects equality of UTXOs.
     * (i.e. utxo1.equals(utxo2) => utxo1.hashCode() == utxo2.hashCode())
     */
    public int hashCode() {
        int hash = 1;
        hash = hash * 17 + index;
        hash = hash * 31 + Arrays.hashCode(txHash);
        return hash;
    }

    /**
     * Compares this UTXO to the one specified by {@code utxo}.
     * The logic is easy to understand: index first (bigger index, bigger UTXO),
     * index equal then compare txHash (longer txHash, bigger UTXO) (length equal, bigger txHash, bigger UTXO).
     */
    public int compareTo(UTXO utxo) {
        byte[] hash = utxo.txHash;
        int in = utxo.index;
        if (in > index)
            return -1;
        else if (in < index)
            return 1;
        else {
            int len1 = txHash.length;
            int len2 = hash.length;
            if (len2 > len1)
                return -1;
            else if (len2 < len1)
                return 1;
            else {
                for (int i = 0; i < len1; i++) {
                    if (hash[i] > txHash[i])
                        return -1;
                    else if (hash[i] < txHash[i])
                        return 1;
                }
                return 0;
            }
        }
    }
}
