/**
 * @Time : 2025/3/22 18:59
 * @Author : KarryRen
 * @Comment: The blockChain class.
 * @Warning: Block Chain should maintain only limited block nodes to satisfy the functions
 * You should not have all the blocks added to the blockchain in memory
 * as it would cause a memory overflow.
 **/

package main.java;

import java.util.*;

public class BlockChain {
    // the related final variable
    public static final int CUT_OFF_AGE = 10;
    public static final int STORAGE = 20;
    // the block Chain
    private Map<byte[], BlockState> blockChain;
    // the global pool of transactions
    private TransactionPool transactionPool;
    // the state of the latest block
    private BlockState latestBlockState;

    /**
     * Create an empty blockchain with just a genesis block with 4 steps:
     * Step 1. Initialize the genesisBlockState, including the block, its height, and the current utxoPool.
     * Step 2. Add the {@code genesisBlockState} to an empty block chain.
     * Step 3. Initialize the {@code latestBlockState} to record the latest block's state in the longest valid branch.
     * Step 4. Initialize the global Transaction Pool {@code transactionPool}.
     * <p>
     * Assume {@code genesisBlock} is a valid block.
     */
    public BlockChain(Block genesisBlock) {
        // Step 1. Initialize the genesisBlockState, including the block, its height, and the current utxoPool.
        Transaction coinbaseTx = genesisBlock.getCoinbase();
        UTXOPool genesisUtxoPool = new UTXOPool();
        UTXO utxo = new UTXO(coinbaseTx.getHash(), 0);
        genesisUtxoPool.addUTXO(utxo, coinbaseTx.getOutput(0));
        BlockState genesisBlockChainState = new BlockState(genesisBlock, 1, genesisUtxoPool);

        // Step 2. Add the genesisBlockState to an empty block chain.
        // In order to save the structure of the tree, I use HashMap to store the data.
        blockChain = new HashMap<byte[], BlockState>();
        blockChain.put(genesisBlock.getHash(), genesisBlockChainState);

        // Step 3. Initialize the latestBlockState to record the latest block's state in the longest valid branch.
        latestBlockState = genesisBlockChainState;

        // Step 4. initialize the global Transaction Pool.
        transactionPool = new TransactionPool();
    }

    /**
     * Get the maximum height block.
     */
    public Block getMaxHeightBlock() {
        return latestBlockState.getBlock();
    }

    /**
     * Get the UTXOPool for mining a new block on top of max height block.
     */
    public UTXOPool getMaxHeightUTXOPool() {
        return latestBlockState.getUtxoPool();
    }

    /**
     * Get the transaction pool to mine a new block.
     */
    public TransactionPool getTransactionPool() {
        return transactionPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)};
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * blockchain maxHeight is {@code <= CUT_OFF_AGE + 1}. As soon as {@code maxHeight > CUT_OFF_AGE + 1},
     * you cannot create a new block at height 2.
     *
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        if (block.getPrevBlockHash() == null || !blockChain.containsKey(block.getPrevBlockHash())) {
            // Condition 1. The block's parent node is valid.  If the block claims to be  a genesis block
            // (parent is a null hash) or its parent node isn't in the previous block chain, returns `false`.
            return false;
        } else if (!allTransactionsValid(block)) {
            // Condition 2. All the transactions in the block should be valid. This condition makes sure
            // that there is no invalid transactions in the block.
            return false;
        } else if (!CUT_OFF_AGE_Condition(block)) {
            // Condition 3. The CUT_OFF_AGE condition should be satisfied. For simplicity, the block
            // should also meet the criteria that the `height > (maxHeight - CUT_OFF_AGE)`.
            // If not, it returns false.
            return false;
        } else {
            // If all the conditions can be satisfied, we can add this block into the blockchain
            // following 3 steps:
            // - step 1. add state of this block into the blockchain
            BlockState parentBlockState = blockChain.get(block.getPrevBlockHash());
            // -- build the txHandler and handle the Txs in block
            TxHandler txHandler = new TxHandler(parentBlockState.getUtxoPool());
            txHandler.handleTxs(block.getTransactions().toArray(new Transaction[block.getTransactions().size()]));
            UTXOPool utxoPool = txHandler.getUTXOPool();
            // -- add the coinbase transaction to UTXOPool
            Transaction blockCoinbaseTx = block.getCoinbase();
            utxoPool.addUTXO(new UTXO(blockCoinbaseTx.getHash(), 0), blockCoinbaseTx.getOutput(0));
            // -- define state of current block and put it into block chain
            BlockState blockState = new BlockState(block, parentBlockState.getHeight() + 1, utxoPool);
            blockChain.put(block.getHash(), blockState);
            // -- update the latestBlockSate (if height equal, then the latestBlockState will be the oldest one)
            if (latestBlockState.getHeight() < blockState.getHeight())
                latestBlockState = blockState;
            // - step 2. update the TransactionPool
            List<Transaction> transactions = block.getTransactions();
            for (Transaction tx : transactions) {
                transactionPool.removeTransaction(tx.getHash());
            }
            // - step 3. delete the previous block to meet the storage condition.
            if (latestBlockState.getHeight() > STORAGE) {
                Iterator iter = blockChain.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    Object val = entry.getValue();
                    if (((BlockState) val).getHeight() <= latestBlockState.getHeight() - STORAGE)
                        iter.remove();
                }
            }
            // return true
            return true;
        }
    }

    /**
     * Add a transaction to the transaction pool.
     */
    public void addTransaction(Transaction tx) {
        transactionPool.addTransaction(tx);
    }

    /**
     * Verify if all the block's transactions are valid
     * Using block.getTransactions() to get the block's transactions and checking by
     * TxHandler().handleTxs(), if the number is not change, then all are valid!
     *
     * @return true if is the subsets.
     */
    private boolean allTransactionsValid(Block block) {
        // get the block's transactions
        Transaction[] blockTxs = block.getTransactions().toArray(new Transaction[block.getTransactions().size()]);
        // checking them by handler to get valid transactions
        BlockState parentBlockState = blockChain.get(block.getPrevBlockHash());
        Transaction[] validTxs = new TxHandler(parentBlockState.getUtxoPool()).handleTxs(blockTxs);
        // test whether number of Txs is change or not
        return blockTxs.length == validTxs.length;
    }

    /**
     * Verify if the block are at {@code height > (maxHeight - CUT_OFF_AGE)}
     * Get the height of the block's parent node, check if parentBlockState Height + 1 > maxHeight - CUT_OFF_AGE.
     *
     * @return true if the height > (maxHeight - CUT_OFF_AGE).
     */
    private boolean CUT_OFF_AGE_Condition(Block block) {
        BlockState parentBlockState = blockChain.get(block.getPrevBlockHash());
        return parentBlockState.getHeight() + 1 > latestBlockState.getHeight() - CUT_OFF_AGE;
    }

    /**
     * Help get blockChain.
     */
    public Map<byte[], BlockState> getBlockChain() {
        return blockChain;
    }
}