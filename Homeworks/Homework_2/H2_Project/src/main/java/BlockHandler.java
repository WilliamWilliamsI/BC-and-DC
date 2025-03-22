/**
 * @Time : 2025/3/22 18:412
 * @Author : http://bitcoinbook.cs.princeton.edu/
 * @Comment: Uses BlockChain.java to process a newly received block, create
 * a new block, or process a newly received transaction. It presents the
 * interface functionality of the blockChain that we need to implement.
 **/

package main.java;

import java.security.PublicKey;

public class BlockHandler {
    // define the blockChain to be handled
    private BlockChain blockChain;

    /**
     * Assume blockChain has the genesis block.
     */
    public BlockHandler(BlockChain blockChain) {
        this.blockChain = blockChain;
    }

    /**
     * Add {@code block} to the block chain if it is valid.
     *
     * @return true if the block is valid and has been added, false otherwise
     */
    public boolean processBlock(Block block) {
        if (block == null)
            return false;
        return blockChain.addBlock(block);
    }

    /**
     * Create a new {@code block} over the max height {@code block}.
     */
    public Block createBlock(PublicKey myAddress) {
        // get the parent block and other key content
        Block parent = blockChain.getMaxHeightBlock();
        UTXOPool uPool = blockChain.getMaxHeightUTXOPool();
        TransactionPool txPool = blockChain.getTransactionPool();
        // handle the transactions in blockChain to get reliable Txs
        TxHandler handler = new TxHandler(uPool);
        Transaction[] txs = txPool.getTransactions().toArray(new Transaction[0]);
        Transaction[] rTxs = handler.handleTxs(txs);
        // initialize the current block
        byte[] parentHash = parent.getHash();
        Block current = new Block(parentHash, myAddress);
        // add the reliable Txs to current  block
        for (int i = 0; i < rTxs.length; i++)
            current.addTransaction(rTxs[i]);
        // final the block (compute the hash)
        current.finalize();
        // add current block to the blockChain
        if (blockChain.addBlock(current))
            return current;
        else
            return null;
    }

    /**
     * Process a {@code Transaction}.
     */
    public void processTx(Transaction tx) {
        blockChain.addTransaction(tx);
    }
}
