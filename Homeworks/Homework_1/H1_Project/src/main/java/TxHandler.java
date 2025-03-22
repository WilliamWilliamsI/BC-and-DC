/**
 * @Time : 2025/3/8 17:03
 * @Author : Karry Ren
 * @Comment: The implementation of TxHandler class,
 * which contains 3 methods: TxHandler(), isValidTx(), and handleTxs().
 **/

package main.java;

import java.util.ArrayList;

public class TxHandler {
    // define the private utxoPool
    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent
     * transaction outputs) is {@code utxoPool}. This should make a defensive copy of
     * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}'s output values are non-negative,
     * (5) the sum of {@code tx}'s input values is >= the sum of its output values its output values;
     * and return false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // (0) check whether tx is null or not
        if (tx == null)
            return false;

        // define the sum of tx's input values and the sum of its output values
        double inputsTotalValues = 0.;
        double outputsTotalValues = 0.;

        // the spentUTXOs is used to record the UTXO has been used
        ArrayList<UTXO> spentUTXOs = new ArrayList<UTXO>();

        // go through all tx's inputs to check (1), (2) and (3) while preparing (5)
        for (int i = 0; i < tx.numInputs(); i++) {
            // get and define the current input, utxo and the corresponding previous output
            Transaction.Input curInput = tx.getInput(i);
            UTXO curUtxo = new UTXO(curInput.prevTxHash, curInput.outputIndex);
            Transaction.Output preOutput = utxoPool.getTxOutput(curUtxo);
            // (1) verify if all outputs claimed by tx are in the current UTXO pool
            if (!utxoPool.contains(curUtxo)) {
                // System.out.println("ERROR (1): The output is not in the current UTXO pool !!");
                return false;
            }
            // (2) verify signature on each input of tx are valid
            if (!Crypto.verifySignature(preOutput.address, tx.getRawDataToSign(i), curInput.signature)) {
                // System.out.println("ERROR (2): The input's signature is invalid !!");
                return false;
            }
            // (3) ensure no UTXO is claimed multiple times by tx
            if (spentUTXOs.contains(curUtxo)) {
                // System.out.println("ERROR (3): Multiple claim of one UTXO !!");
                return false;
            } else {
                spentUTXOs.add(curUtxo);
            }
            // used for (5), record the total values of input by the preOutput
            inputsTotalValues += preOutput.value;
        }

        // go through all tx's outputs to check (4), while preparing for (5)
        for (int o = 0; o < tx.numOutputs(); o++) {
            // get the current output, utxo and the corresponding previous output
            Transaction.Output curOutput = tx.getOutput(o);
            // (4) ensure all of tx's output values are non-negative
            if (tx.getOutput(o).value < 0) {
                // System.out.println("ERROR (4): The output's value is negative !!");
                return false;
            }
            // used for (5), record the total values of output
            outputsTotalValues += curOutput.value;
        }

        // (5) ensure the sum of tx's input values is greater than or equal to the sum of its output values
        if (inputsTotalValues < outputsTotalValues) {
            // System.out.println("ERROR (5): outputsTotalValues > inputsTotalValues !!");
            return false;
        }

        // all 5 conditions of verifying are passed !
        // System.out.println("This transaction has been verified, it's OK !!");
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions,
     * checking each transaction for correctness, returning a mutually valid array
     * of accepted transactions, and updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // define the accepted transactions array
        ArrayList<Transaction> acceptedTxs = new ArrayList<>();

        // go through over and over again, until can't find the valid transaction
        while (true) {
            // the flag to detect whether it finds valid transaction in going through or not
            boolean findValidTxFlag = false;
            // go through possibleTxs
            for (Transaction tx : possibleTxs) {
                if (acceptedTxs.contains(tx)) {
                    continue;
                } else {
                    if (isValidTx(tx)) {
                        // find the valid transaction add to the array and change the flag
                        acceptedTxs.add(tx);
                        findValidTxFlag = true;
                        // update utxoPool: add new valid output to utxoPool
                        for (int o = 0; o < tx.numOutputs(); o++) {
                            Transaction.Output output = tx.getOutput(o);
                            UTXO utxo = new UTXO(tx.getHash(), o);
                            utxoPool.addUTXO(utxo, output);
                        }
                        // update utxoPool: remove spent valid input from utxoPool
                        for (int i = 0; i < tx.numInputs(); i++) {
                            Transaction.Input input = tx.getInput(i);
                            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                            utxoPool.removeUTXO(utxo);
                        }
                    }
                }
            }
            // there is no valid transaction left, just break.
            if (!findValidTxFlag)
                break;
        }

        // transfer the ArrayList<Transaction> to Transaction[]
        return acceptedTxs.toArray(new Transaction[acceptedTxs.size()]);
    }
}
