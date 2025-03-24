/**
 * @Time : 2025/3/22 11:33
 * @Author : Karry Ren
 * @Comment:
 **/

package test.java;

import main.java.*;
import org.junit.Before;
import org.junit.Test;

import java.security.*;
import java.util.Arrays;

import static org.junit.Assert.*;


public class BlockChainTest {
    // some needed private attributes
    private final double COINBASE = 25.0;
    private BlockChain blockChain;
    private KeyPair genesisKeyPair;
    private Block genesisBlock;
    private TransactionPool txPool = new TransactionPool();

    /**
     * Generate the secure key pair with 2048 length and SecureRandom().
     */
    private KeyPair secureKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        return generator.generateKeyPair();
    }

    /**
     * Create the transaction from {@code preTxHash}, {@code outputIndex} of {@code sender},
     * to {@code receivers[]} with {@code amount[]} (make it more quick).
     */
    private Transaction createTransaction(byte[] prevTxHash, int outputIndex, KeyPair sender,
                                          KeyPair[] receivers, double[] amount)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        // initial transaction of the input and outputs (one by one)
        Transaction tx = new Transaction();
        tx.addInput(prevTxHash, outputIndex);
        for (int i = 0; i < receivers.length; i++) {
            tx.addOutput(amount[i], receivers[i].getPublic());
        }
        // sign the input
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(sender.getPrivate());
        signature.update(tx.getRawDataToSign(0));
        tx.addSignature(signature.sign(), 0);
        // hash the tx and return
        tx.finalize();
        return tx;
    }

    /*
     * Initializing the block chain with only one genesis block.
     */
    @Before
    public void before() throws NoSuchAlgorithmException {
        // generate the secure key pair of genesis block
        genesisKeyPair = secureKeyPair();
        // initialize genesis block
        genesisBlock = new Block(null, genesisKeyPair.getPublic());
        genesisBlock.finalize();
        // initialize the block chain
        blockChain = new BlockChain(genesisBlock);
    }


    /**
     * --------------------------------------  Branch Scenario -------------------------------------- *
     * Test Strategy: Using the constructor to create a new empty block chain and then add some
     * blocks to see if it can add the block to the blockchain correctly.
     * 1. when there is only genesis block,
     * 2. when there is a valid block with only one valid transaction,
     * 3. when there is a valid block with several valid transactions,
     * 4. when there is an invalid transaction in the transaction pool,
     * 5. when the block's previous is null,
     * 6. when the block's preHash isn't in the blockchain,
     * 7. when the block contains invalid transactions,
     * 8. when the blockchain's height doesn't satisfy the storage condition.
     * ---------------------------------------------------------------------------------------------- *
     */

    @Test
    public void test_only_genesis_block() {
        // build up another coinbaseTx to compare
        Transaction coinbaseTx = new Transaction(COINBASE, genesisKeyPair.getPublic());
        // get the latestBlock
        Block latestBlock = blockChain.getMaxHeightBlock();
        // tests for only genesis block situation
        assertEquals(latestBlock.getHash(), genesisBlock.getHash());
        assertEquals(latestBlock.getCoinbase(), coinbaseTx);
        assertEquals(blockChain.getMaxHeightUTXOPool().getAllUTXO().get(0), new UTXO(coinbaseTx.getHash(), 0));
        assertEquals(blockChain.getTransactionPool().getTransactions(), txPool.getTransactions());
        assertTrue(blockChain.getTransactionPool().getTransactions().isEmpty());
    }

    @Test
    public void test_block_with_one_coinbase_tx() throws
            NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        // build up the keypair
        KeyPair bobKeyPair = secureKeyPair(); // bob is a receiver
        KeyPair aliceKeyPair = secureKeyPair(); // alice is a miner
        double[] outputValues = {25.0};
        // create the coinbase transaction
        KeyPair[] receiversKeypairArray = {bobKeyPair};
        Transaction tx = createTransaction(genesisBlock.getCoinbase().getHash(), 0,
                genesisKeyPair, receiversKeypairArray, outputValues);
        // initial the block handler and create the block with coinbase tx (mining)
        BlockHandler blockHandler = new BlockHandler(blockChain);
        blockHandler.processTx(tx);
        Block validBlock = blockHandler.createBlock(aliceKeyPair.getPublic());
        // tests for a valid block with one coinbase transaction
        assertTrue(blockHandler.processBlock(validBlock));
        assertEquals(blockChain.getMaxHeightBlock().getHash(), validBlock.getHash());
        assertEquals(blockChain.getMaxHeightBlock().getCoinbase(), new Transaction(25, aliceKeyPair.getPublic()));
        assertEquals(2, blockChain.getMaxHeightUTXOPool().getAllUTXO().size());
        assertTrue(blockChain.getTransactionPool().getTransactions().isEmpty());
    }

    @Test
    public void test_block_with_several_valid_txs() throws
            NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        // build up the key pair (7 players and the first one is miner)
        int numOfPlayers = 7;
        KeyPair[] keyPair = new KeyPair[numOfPlayers];
        for (int i = 0; i < numOfPlayers; i++) {
            keyPair[i] = secureKeyPair();
        }
        // create new transactions
        int numOfTransactions = 3;
        Transaction[] txs = new Transaction[numOfTransactions];
        txs[0] = createTransaction(genesisBlock.getCoinbase().getHash(), 0, genesisKeyPair,
                new KeyPair[]{keyPair[1], keyPair[2]}, new double[]{10, 15});
        txs[1] = createTransaction(txs[0].getHash(), 0, keyPair[1],
                new KeyPair[]{keyPair[3], keyPair[4]}, new double[]{8, 2});
        txs[2] = createTransaction(txs[0].getHash(), 1, keyPair[2],
                new KeyPair[]{keyPair[5], keyPair[6]}, new double[]{7, 8});
        // initial the block handler and create the block with related txs (mining)
        BlockHandler blockHandler = new BlockHandler(blockChain);
        for (int i = 0; i < numOfTransactions; i++) {
            blockHandler.processTx(txs[i]);
        }
        Block validBlock = blockHandler.createBlock(keyPair[0].getPublic());
        // tests for a valid block with several valid transactions
        assertTrue(blockHandler.processBlock(validBlock));
        assertEquals(blockChain.getMaxHeightBlock().getHash(), validBlock.getHash());
        assertEquals(blockChain.getMaxHeightBlock().getCoinbase(), new Transaction(25, keyPair[0].getPublic()));
        assertEquals(5, blockChain.getMaxHeightUTXOPool().getAllUTXO().size());
        assertEquals(0, blockChain.getTransactionPool().getTransactions().size());
    }

    @Test
    public void test_block_with_invalid_txs_in_txPool() throws
            NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        // build up the key pair (5 players and the first one is miner)
        int numOfPlayers = 5;
        KeyPair[] keyPair = new KeyPair[numOfPlayers];
        for (int i = 0; i < numOfPlayers; i++) {
            keyPair[i] = secureKeyPair();
        }
        // create new transactions
        int numOfTransactions = 3;
        Transaction[] txs = new Transaction[numOfTransactions];
        txs[0] = createTransaction(genesisBlock.getCoinbase().getHash(), 0, genesisKeyPair,
                new KeyPair[]{keyPair[1], keyPair[2]}, new double[]{10, 15});
        txs[1] = createTransaction(txs[0].getHash(), 0, keyPair[1],
                new KeyPair[]{keyPair[3]}, new double[]{10});
        txs[2] = createTransaction(txs[0].getHash(), 0, keyPair[1],
                new KeyPair[]{keyPair[4]}, new double[]{10});
        // initial the block handler and create the block with related txs (mining)
        BlockHandler blockHandler = new BlockHandler(blockChain);
        for (int i = 0; i < numOfTransactions; i++) {
            blockHandler.processTx(txs[i]);
        }
        Block validBlock = blockHandler.createBlock(keyPair[0].getPublic());
        // tests for a valid block with several valid and one invalid transactions
        assertTrue(blockHandler.processBlock(validBlock));
        assertEquals(blockChain.getMaxHeightBlock().getHash(), validBlock.getHash());
        assertEquals(blockChain.getMaxHeightBlock().getCoinbase(), new Transaction(25, keyPair[0].getPublic()));
        assertEquals(3, blockChain.getMaxHeightUTXOPool().getAllUTXO().size());
        assertEquals(1, blockChain.getTransactionPool().getTransactions().size());
    }

    @Test
    public void test_block_with_null_preHash() throws
            NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        // build up the key pair
        KeyPair[] bobKeypair = {secureKeyPair()};
        KeyPair aliceKeyPair = secureKeyPair();
        double[] amount = {25.0};
        // create new transactions
        Transaction tx = createTransaction(genesisBlock.getCoinbase().getHash(), 0,
                genesisKeyPair, bobKeypair, amount);
        // initial the block handler and create an invalid block (faked)
        BlockHandler blockHandler = new BlockHandler(blockChain);
        blockHandler.processTx(tx);
        Block invalidBlock = new Block(null, aliceKeyPair.getPublic());
        UTXOPool uPool = blockChain.getMaxHeightUTXOPool();
        TransactionPool txPool = blockChain.getTransactionPool();
        TxHandler handler = new TxHandler(uPool);
        Transaction[] txs = txPool.getTransactions().toArray(new Transaction[0]);
        Transaction[] rTxs = handler.handleTxs(txs);
        for (int i = 0; i < rTxs.length; i++)
            invalidBlock.addTransaction(rTxs[i]);
        invalidBlock.finalize();
        // tests for an invalid block with null pre hash
        assertFalse(blockHandler.processBlock(invalidBlock));
        assertEquals(blockChain.getMaxHeightBlock().getHash(), genesisBlock.getHash());
        assertEquals(1, blockChain.getMaxHeightUTXOPool().getAllUTXO().size());
        assertEquals(1, blockChain.getTransactionPool().getTransactions().size());
    }

    @Test
    public void test_block_with_wrong_preHash() throws
            NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        // players
        int numOfPlayers = 8;
        KeyPair[] keyPair = new KeyPair[numOfPlayers];
        for (int i = 0; i < numOfPlayers; i++) {
            keyPair[i] = secureKeyPair();
        }
        // first block
        int numOfTransactions = 3;
        Transaction[] txs = new Transaction[numOfTransactions];
        txs[0] = createTransaction(genesisBlock.getCoinbase().getHash(), 0, genesisKeyPair,
                new KeyPair[]{keyPair[1], keyPair[2]}, new double[]{10, 15});
        txs[1] = createTransaction(txs[0].getHash(), 0, keyPair[1],
                new KeyPair[]{keyPair[3], keyPair[4]}, new double[]{8, 2});
        BlockHandler blockHandler1 = new BlockHandler(blockChain);
        for (int i = 0; i < numOfTransactions - 1; i++) {
            blockHandler1.processTx(txs[i]);
        }
        Block theFirstBlock = blockHandler1.createBlock(keyPair[0].getPublic());
        // second block
        txs[2] = createTransaction(txs[0].getHash(), 1, keyPair[2],
                new KeyPair[]{keyPair[5], keyPair[6]}, new double[]{7, 8});
        BlockHandler blockHandler2 = new BlockHandler(blockChain);
        blockHandler2.processTx(txs[2]);
        // - wrong preHash
        Block invalidBlock = new Block(genesisBlock.getCoinbase().getHash(), keyPair[7].getPublic());
        UTXOPool uPool = blockChain.getMaxHeightUTXOPool();
        TransactionPool txPool = blockChain.getTransactionPool();
        TxHandler handler = new TxHandler(uPool);
        Transaction[] TX = txPool.getTransactions().toArray(new Transaction[0]);
        Transaction[] rTxs = handler.handleTxs(TX);
        for (int i = 0; i < rTxs.length; i++)
            invalidBlock.addTransaction(rTxs[i]);
        invalidBlock.finalize();
        // test
        assertTrue(blockHandler1.processBlock(theFirstBlock));
        assertFalse(blockHandler2.processBlock(invalidBlock));
        assertEquals(blockChain.getMaxHeightBlock().getHash(), theFirstBlock.getHash());
        assertEquals(blockChain.getMaxHeightBlock().getCoinbase(), new Transaction(25, keyPair[0].getPublic()));
        assertEquals(4, blockChain.getMaxHeightUTXOPool().getAllUTXO().size());
        assertEquals(1, blockChain.getTransactionPool().getTransactions().size());
    }

    @Test
    public void test_block_with_invalid_txs() throws
            NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        // players
        int numOfPlayers = 5;
        KeyPair[] keyPair = new KeyPair[numOfPlayers];
        for (int i = 0; i < numOfPlayers; i++) {
            keyPair[i] = secureKeyPair();
        }
        // transactions
        int numOfTransactions = 3;
        Transaction[] txs = new Transaction[numOfTransactions];
        txs[0] = createTransaction(genesisBlock.getCoinbase().getHash(), 0, genesisKeyPair,
                new KeyPair[]{keyPair[1], keyPair[2]}, new double[]{10, 15});
        txs[1] = createTransaction(txs[0].getHash(), 0, keyPair[1],
                new KeyPair[]{keyPair[3]}, new double[]{10});
        txs[2] = createTransaction(txs[0].getHash(), 0, keyPair[1],
                new KeyPair[]{keyPair[4]}, new double[]{10});
        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block invalidBlock = new Block(genesisBlock.getHash(), keyPair[0].getPublic());
        for (int i = 0; i < numOfTransactions; i++)
            invalidBlock.addTransaction(txs[i]);
        invalidBlock.finalize();
        // test
        assertFalse(blockHandler.processBlock(invalidBlock));
        assertEquals(blockChain.getMaxHeightBlock().getHash(), genesisBlock.getHash());
        assertEquals(blockChain.getMaxHeightBlock().getCoinbase(), new Transaction(25, genesisKeyPair.getPublic()));
        assertEquals(1, blockChain.getMaxHeightUTXOPool().getAllUTXO().size());
        assertEquals(0, blockChain.getTransactionPool().getTransactions().size());
    }

    @Test
    public void test_storage_condition() throws NoSuchAlgorithmException {
        // players & blocks
        int numOfPlayersBlocks = 23;
        KeyPair[] keyPair = new KeyPair[numOfPlayersBlocks];
        for (int i = 0; i < numOfPlayersBlocks; i++) {
            keyPair[i] = secureKeyPair();
        }
        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block[] block = new Block[numOfPlayersBlocks];
        // build up blocks
        block[0] = new Block(genesisBlock.getHash(), keyPair[0].getPublic());
        block[0].finalize();
        for (int i = 1; i < numOfPlayersBlocks; i++) {
            block[i] = new Block(block[i - 1].getHash(), keyPair[i].getPublic());
            block[i].finalize();
        }
        for (int i = 0; i < numOfPlayersBlocks; i++) {
            assertTrue(blockHandler.processBlock(block[i]));
        }
        // test
        assertEquals(20, blockChain.getBlockChain().size());
        assertEquals(24, blockChain.getMaxHeightUTXOPool().getAllUTXO().size());
        assertEquals(blockChain.getMaxHeightBlock().getHash(), block[numOfPlayersBlocks - 1].getHash());
        assertEquals(blockChain.getMaxHeightBlock().getCoinbase(),
                new Transaction(25, keyPair[numOfPlayersBlocks - 1].getPublic()));
        assertEquals(0, blockChain.getTransactionPool().getTransactions().size());
    }

    /**
     * --------------------------------------Forking Scenario -------------------------------------- *
     * Using the constructor to create a new empty block chain, adding some blocks and then make some forks
     * to see if it can add the blocks in the blockchain correctly.
     * 1. The fork should satisfy CUT_OFF_AGE_Condition,
     * 2. If there are multiple blocks at the same height, it should consider the oldest block
     * in the longest valid branch,
     * 3. If there are two branches with the same height, whichever has the next block first will become to the new
     * longest valid branch.
     * ---------------------------------------------------------------------------------------------- *
     */

    @Test
    public void test_CUT_OFF_AGE_condition() throws NoSuchAlgorithmException {
        int numOfPlayers = 13;
        KeyPair[] keyPair = new KeyPair[numOfPlayers];
        for (int i = 0; i < numOfPlayers; i++) {
            keyPair[i] = secureKeyPair();
        }
        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block[] block = new Block[numOfPlayers];
        block[1] = new Block(genesisBlock.getHash(), keyPair[1].getPublic());
        block[1].finalize();
        for (int i = 2; i < 12; i++) {
            block[i] = new Block(block[i - 1].getHash(), keyPair[i].getPublic());
            block[i].finalize();
        }
        block[0] = new Block(genesisBlock.getHash(), keyPair[0].getPublic());
        block[0].finalize();
        block[12] = new Block(block[1].getHash(), keyPair[12].getPublic());
        block[12].finalize();
        for (int i = 1; i < 12; i++) {
            assertTrue(blockHandler.processBlock(block[i]));
        }
        assertFalse(blockHandler.processBlock(block[0]));
        assertTrue(blockHandler.processBlock(block[12]));
        assertEquals(12, blockChain.getMaxHeightUTXOPool().getAllUTXO().size());
        assertEquals(blockChain.getMaxHeightBlock().getHash(), block[11].getHash());
        assertEquals(blockChain.getMaxHeightBlock().getCoinbase(), new Transaction(25, keyPair[11].getPublic()));
        assertEquals(0, blockChain.getTransactionPool().getTransactions().size());
    }

    @Test
    public void test_mul_blocks_same_height() throws
            NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        int numOfPlayers = 9;
        int numOfTransactions = 3;
        KeyPair[] keyPair = new KeyPair[numOfPlayers];
        for (int i = 0; i < numOfPlayers; i++) {
            keyPair[i] = secureKeyPair();
        }
        Transaction[] txs = new Transaction[numOfTransactions];
        txs[0] = createTransaction(genesisBlock.getCoinbase().getHash(), 0, genesisKeyPair,
                new KeyPair[]{keyPair[1], keyPair[2]}, new double[]{10, 15});
        txs[1] = createTransaction(txs[0].getHash(), 0, keyPair[1],
                new KeyPair[]{keyPair[3], keyPair[4]}, new double[]{8, 2});
        txs[2] = createTransaction(txs[0].getHash(), 1, keyPair[2],
                new KeyPair[]{keyPair[5], keyPair[6]}, new double[]{7, 8});
        BlockHandler blockHandler = new BlockHandler(blockChain);
        blockHandler.processTx(txs[0]);
        Block block1 = new Block(genesisBlock.getHash(), keyPair[0].getPublic());
        block1.addTransaction(txs[0]);
        block1.finalize();
        blockHandler.processTx(txs[1]);
        Block block2 = new Block(block1.getHash(), keyPair[7].getPublic());
        block2.addTransaction(txs[1]);
        block2.finalize();
        blockHandler.processTx(txs[2]);
        Block block3 = new Block(block1.getHash(), keyPair[8].getPublic());
        block3.addTransaction(txs[2]);
        block3.finalize();
        assertTrue(blockHandler.processBlock(block1));
        assertTrue(blockHandler.processBlock(block2));
        assertTrue(blockHandler.processBlock(block3));
        assertEquals(5, blockChain.getMaxHeightUTXOPool().getAllUTXO().size());
        assertEquals(blockChain.getMaxHeightBlock().getHash(), block2.getHash());
        assertEquals(blockChain.getMaxHeightBlock().getCoinbase(), new Transaction(25, keyPair[7].getPublic()));
        assertEquals(0, blockChain.getTransactionPool().getTransactions().size());
    }

    @Test
    public void test_forking_attack() throws NoSuchAlgorithmException {
        int numOfPlayers = 3;
        KeyPair[] keyPair = new KeyPair[numOfPlayers];
        for (int i = 0; i < numOfPlayers; i++) {
            keyPair[i] = secureKeyPair();
        }
        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block[] block = new Block[numOfPlayers];
        block[1] = new Block(genesisBlock.getHash(), keyPair[1].getPublic());
        block[1].finalize();
        block[0] = new Block(genesisBlock.getHash(), keyPair[0].getPublic());
        block[0].finalize();
        block[2] = new Block(block[0].getHash(), keyPair[2].getPublic());
        block[2].finalize();
        for (int i = 0; i < 3; i++) {
            assertTrue(blockHandler.processBlock(block[i]));
        }
        assertEquals(3, blockChain.getMaxHeightUTXOPool().getAllUTXO().size());
        assertEquals(blockChain.getMaxHeightBlock().getHash(), block[2].getHash());
        assertEquals(blockChain.getMaxHeightBlock().getCoinbase(), new Transaction(25, keyPair[2].getPublic()));
        assertEquals(0, blockChain.getTransactionPool().getTransactions().size());
    }
}

