/**
 * @Time : 2025/3/8 17:03
 * @Author : Karry Ren
 * @Comment: The given handler for transactions.
 * The `handleTxs()` function has been simplified.
 * The `getUTXOPool()` function is in the end.
 **/

package main.java;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TxHandler {
    // define the private utxoPool
    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent
     * transaction outputs) is {@code utxoPool}. This should make a copy of utxoPool
     * by using the UTXOPool(UTXOPool uPool) constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative
     * (5) the sum of {@code tx}s input values is >= to the sum of its output values;
     * and false otherwise.
     * <p>
     * However, should the input value and output value be equal ?
     * No! otherwise, the ledger will become unbalanced !
     */
    public boolean isValidTx(Transaction tx) {
        // already claimedUTXO
        Set<UTXO> claimedUTXO = new HashSet<UTXO>();

        // // define the sum of tx's input values and the sum of its output values
        double inputSum = 0;
        double outputSum = 0;

        // go through all tx's inputs to check (1), (2) and (3) while preparing (5)
        List<Transaction.Input> inputs = tx.getInputs();
        for (int i = 0; i < inputs.size(); i++) {
            // get the target input
            Transaction.Input input = inputs.get(i);
            // (1) verify if all outputs claimed by tx are in the current UTXO pool
            if (!isConsumedCoinAvailable(input)) {
                return false;
            }
            // (2) verify signature on each input of tx are valid
            if (!verifySignatureOfConsumeCoin(tx, i, input)) {
                return false;
            }
            // (3) ensure no UTXO is claimed multiple times by tx
            if (isCoinConsumedMultipleTimes(claimedUTXO, input)) {
                return false;
            }
            // used for (5), record the total values of input by the preOutput
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            Transaction.Output correspondingOutput = utxoPool.getTxOutput(utxo);
            inputSum += correspondingOutput.value;
        }

        // go through all tx's outputs to check (4), while preparing for (5)
        List<Transaction.Output> outputs = tx.getOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            // get the current output, utxo and the corresponding previous output
            Transaction.Output output = outputs.get(i);
            // (4) ensure all of tx's output values are non-negative
            if (output.value <= 0) {
                return false;
            }
            // used for (5), record the total values of output
            outputSum += output.value;
        }

        // Should the input value and output value be equal?
        // No, otherwise, the ledger will become unbalanced ！
        // The difference between inputSum and outputSum is the transaction fee
        if (outputSum > inputSum) {
            return false;
        }

        // all 5 conditions of verifying are passed !
        return true;
    }

    private boolean isConsumedCoinAvailable(Transaction.Input input) {
        UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
        return utxoPool.contains(utxo);
    }

    private boolean verifySignatureOfConsumeCoin(Transaction tx, int index, Transaction.Input input) {
        UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
        Transaction.Output correspondingOutput = utxoPool.getTxOutput(utxo);
        PublicKey pk = correspondingOutput.address;
        return Crypto.verifySignature(pk, tx.getRawDataToSign(index), input.signature);
    }

    private boolean isCoinConsumedMultipleTimes(Set<UTXO> claimedUTXO, Transaction.Input input) {
        UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
        return !claimedUTXO.add(utxo);
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions,
     * checking each transaction for correctness, returning a mutually valid array
     * of accepted transactions, and updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // define the accepted transactions array
        List<Transaction> acceptedTx = new ArrayList<Transaction>();

        // for loop to check remove and add
        for (int i = 0; i < possibleTxs.length; i++) {
            Transaction tx = possibleTxs[i];
            if (isValidTx(tx)) {
                acceptedTx.add(tx);
                removeConsumedCoinsFromPool(tx);
                addCreatedCoinsToPool(tx);
            }
        }

        // transfer the ArrayList<Transaction> to Transaction[]
        Transaction[] result = new Transaction[acceptedTx.size()];
        acceptedTx.toArray(result);
        return result;
    }

    private void addCreatedCoinsToPool(Transaction tx) {
        List<Transaction.Output> outputs = tx.getOutputs();
        for (int j = 0; j < outputs.size(); j++) {
            Transaction.Output output = outputs.get(j);
            UTXO utxo = new UTXO(tx.getHash(), j);
            utxoPool.addUTXO(utxo, output);
        }
    }

    private void removeConsumedCoinsFromPool(Transaction tx) {
        List<Transaction.Input> inputs = tx.getInputs();
        for (int j = 0; j < inputs.size(); j++) {
            Transaction.Input input = inputs.get(j);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            utxoPool.removeUTXO(utxo);
        }
    }

    public UTXOPool getUTXOPool() {
        return utxoPool;
    }
}
