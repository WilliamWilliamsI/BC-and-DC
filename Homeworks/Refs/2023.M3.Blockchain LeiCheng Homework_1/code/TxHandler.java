import java.util.*;

public class TxHandler {

    /**
     * 创建一个公共分类帐，其当前的UTXOPool（未花费交易输出的集合）是
     * {@code utxoPool}。这应该通过使用UTXOPool(UTXOPool uPool)构造函数来复制utxoPool。
     */

    private UTXOPool utxoPool;
    public TxHandler(UTXOPool utxoPool) {
        // 实现这个
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return UTXO池:
     */
    public UTXOPool getHandledUtxoPool() {
        return this.utxoPool;
    }

    /**
     * @return 如果：
     * (1) {@code tx} 中声明的所有输出都在当前UTXO池中，
     * (2) {@code tx} 的每个输入的签名都是有效的，
     * (3) {@code tx} 没有多次声明同一个UTXO，
     * (4) {@code tx} 的所有输出值都是非负的，以及
     * (5) {@code tx} 输入值的总和大于或等于其输出值的总和；则返回true，否则返回false。
     */
    public boolean isValidTx(Transaction tx) {
        // 实现这个
        int index_k = 0; // 索引
        double inputTotal = 0; // 输入总金额
        double outputTotal = 0; // 输出总金额
        // spentUtxo 集合用于确保一个UTXO在交易中只能使用一次。
        Set<UTXO> spentUtxo = new HashSet<UTXO>(); // 已花费的交易输出，避免重复

        // 循环，遍历一个Tx中的所有输入
        for (Transaction.Input input : tx.getInputs()) {
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);

            // (1) 所有在{@code tx}中声明的输出都在当前UTXO池中
            if (!utxoPool.contains(utxo)) {
                return false;
            }

            //(2) {@code tx} 的每个输入的签名都是有效的
            if (!Crypto.verifySignature(utxoPool.getTxOutput(utxo).address, tx.getRawDataToSign(index_k), input.signature)) {
                return false;
            }

            // (3) {@code tx} 没有多次声明同一个UTXO
            if (spentUtxo.contains(utxo)) {
                return false;
            }
            // 将已花费的交易添加到 spentUtxo 集合中
            spentUtxo.add(utxo);

            // 计算总输入（inputTotal）
            inputTotal += utxoPool.getTxOutput(utxo).value;
            index_k++;
        }

        // (4) {@code tx} 的所有输出值都是非负的
        for (Transaction.Output output : tx.getOutputs()) {
            if (output.value < 0) {
                return false;
            } else {
                outputTotal += output.value;
            }
        }
        // (5) {@code tx} 输入值的总和大于或等于其输出值的总和
        if (outputTotal > inputTotal) {
            return false;
        }

        // 如果上述所有情况都不成立，则返回true
        return true;
    }

    /**
     * 通过接收一个无序的提议交易数组来处理每个时期，检查每个交易的正确性，
     * 返回一组相互有效的接受交易，并根据需要更新当前的UTXO池。
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // 实现这个
        Set<Transaction> acceptedTxs = new HashSet<Transaction>();
        Set<Transaction> invalidTx = new HashSet<Transaction>();
        Set<Transaction> newlyacceptedTxs = new HashSet<Transaction>();

        // 遍历可能的交易数组。
        for (Transaction tx : possibleTxs) {
            if (isValidTx(tx)) {
                HandleValidTx(acceptedTxs, newlyacceptedTxs, tx);
            } else {
                invalidTx.add(tx);
            }
        }
        while (!newlyacceptedTxs.isEmpty()) {
            newlyacceptedTxs.clear(); // 确保没有死循环。
            for (Transaction invalid_tx : invalidTx) {
                if (isValidTx(invalid_tx)) {
                    HandleValidTx(acceptedTxs, newlyacceptedTxs, invalid_tx);
                }
            }
            invalidTx.removeAll(newlyacceptedTxs);
        }

        // 修复交易的大小
        Transaction[] Arr_acceptedTx = acceptedTxs.toArray(new Transaction[acceptedTxs.size()]);

        return Arr_acceptedTx;
    }

    public void HandleValidTx(Set<Transaction> acceptedTxs, Set<Transaction> newlyacceptedTxs, Transaction tx) {
        for (Transaction.Input oneDeal : tx.getInputs()) {
            // 检查交易中的输入，将其从UTXOPool中移除
            UTXO possibleDeal = new UTXO(oneDeal.prevTxHash, oneDeal.outputIndex);
            if (utxoPool.contains(possibleDeal)) {
                utxoPool.removeUTXO(possibleDeal);
            }
        }

        // 输出是新的未来输入，添加到UTXOPool中
        List<Transaction.Output> outputs = tx.getOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            UTXO utxo = new UTXO(tx.getHash(), i);
            utxoPool.addUTXO(utxo, outputs.get(i));
        }
        newlyacceptedTxs.add(tx);
        acceptedTxs.add(tx);
    }
}
