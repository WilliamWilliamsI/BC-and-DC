package main.java; /**
 * @Time : 2025/3/8 16:59
 * @Author : http://bitcoinbook.cs.princeton.edu/.
 * @Comment: The main.java.UTXOPool class that represents the current set of outstanding
 * UTXOs and contains a map from each main.java.UTXO to its corresponding transaction output.
 **/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class UTXOPool {

    // the current collection of UTXOs, with each one mapped to
    // its corresponding transaction output
    private HashMap<UTXO, Transaction.Output> H;

    /**
     * Creates a new empty main.java.UTXOPool
     */
    public UTXOPool() {
        H = new HashMap<UTXO, Transaction.Output>();
    }

    /**
     * Creates a new main.java.UTXOPool that is a copy of {@code uPool}
     */
    public UTXOPool(UTXOPool uPool) {
        H = new HashMap<UTXO, Transaction.Output>(uPool.H);
    }

    /**
     * Adds a mapping from main.java.UTXO {@code utxo} to transaction output @code{txOut} to the pool
     */
    public void addUTXO(UTXO utxo, Transaction.Output txOut) {
        H.put(utxo, txOut);
    }

    /**
     * Removes the main.java.UTXO {@code utxo} from the pool
     */
    public void removeUTXO(UTXO utxo) {
        H.remove(utxo);
    }

    /**
     * @return the transaction output corresponding to main.java.UTXO {@code utxo}, or null if {@code utxo} is
     * not in the pool.
     */
    public Transaction.Output getTxOutput(UTXO ut) {
        return H.get(ut);
    }

    /**
     * @return true if main.java.UTXO {@code utxo} is in the pool and false otherwise
     */
    public boolean contains(UTXO utxo) {
        return H.containsKey(utxo);
    }

    /**
     * Returns an {@code ArrayList} of all UTXOs in the pool
     */
    public ArrayList<UTXO> getAllUTXO() {
        Set<UTXO> setUTXO = H.keySet();
        ArrayList<UTXO> allUTXO = new ArrayList<UTXO>();
        for (UTXO ut : setUTXO) {
            allUTXO.add(ut);
        }
        return allUTXO;
    }
}
