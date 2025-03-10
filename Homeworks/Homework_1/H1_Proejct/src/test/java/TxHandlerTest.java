/**
 * @Time : 2025/3/8 17:29
 * @Author : Karry Ren
 * @Comment: The test suite to verify the implementation of TxHandler.
 **/

package test.java;

import main.java.Transaction;
import main.java.TxHandler;
import main.java.UTXOPool;
import main.java.UTXO;
import org.junit.Before;
import org.junit.Test;

import java.security.*;

import static org.junit.Assert.*;


public class TxHandlerTest {
    // some needed private attributes
    private KeyPair kScrooge, kAlice, kBob;
    private TxHandlerTest.TX tx0, tx1, tx2, tx3, tx4, tx5, tx6, tx7, tx8, tx9;
    private TxHandler txHandler;

    /**
     * Preparation 1. Extend the transaction class to TX and add the new function signTX().
     * Make the signature of the TX's {@code index} input using private key {@code sk}.
     * This algorithm is inferred from {@code verifySignature()}.
     */
    public static class TX extends Transaction {
        public void signTX(PrivateKey sk, int index)
                throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
            // generate the signature using the private key and the data
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(sk);
            signature.update(this.getRawDataToSign(index));
            // add the signature to the transaction's index input
            this.addSignature(signature.sign(), index);
            // finalize to create the
            this.finalize();
        }
    }

    /**
     * Create the coinbase transaction {@code tx0} and initialize the {@code UTXOPool}.
     * Also create other 9 translations (from {@code tx1}` to {@code tx9})
     * between Scrooge, Alice and Bob to simulate a real transaction.
     * Some of them are correct while others are incorrect.
     * The specific input-output relationships are shown in Figure 2 in README.md.
     */
    @Before
    public void before() throws Exception {
        // initialize the coinbase transaction (really important)
        tx0 = new TX();
        // - step 1. generate Scrooge's public key `pk` & private key `sk`
        kScrooge = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        // - step 2. update the input and output
        tx0.addOutput(10, kScrooge.getPublic());
        byte[] prevHash = new byte[0];
        tx0.addInput(prevHash, 0);
        tx0.signTX(kScrooge.getPrivate(), 0);
        // initialize the UTXOPool
        UTXOPool utxopool = new UTXOPool();
        UTXO utxo = new UTXO(tx0.getHash(), 0);
        utxopool.addUTXO(utxo, tx0.getOutput(0));
        // initialize the txHandler
        txHandler = new TxHandler(utxopool);

        // Pay coins transactions 1: [ Scrooge -> Alice<values=9>, Scrooge -> Scrooge<values=1> ],
        // which is RIGHT.
        tx1 = new TxHandlerTest.TX();
        kAlice = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        tx1.addInput(tx0.getHash(), 0);
        tx1.addOutput(9, kAlice.getPublic());
        tx1.addOutput(1, kScrooge.getPublic());
        tx1.signTX(kScrooge.getPrivate(), 0);

        // Pay coins transactions 2: [ Alice -> Bob<values=5>, Alice -> Alice<values=4> ]
        // which is RIGHT after tx1.
        tx2 = new TxHandlerTest.TX();
        kBob = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        tx2.addInput(tx1.getHash(), 0);
        tx2.addOutput(5, kBob.getPublic());
        tx2.addOutput(4, kAlice.getPublic());
        tx2.signTX(kAlice.getPrivate(), 0);

        // Pay coins transactions 3: [ Alice -> Scrooge<values=9> ],
        // but with the WRONG input index.
        tx3 = new TxHandlerTest.TX();
        tx3.addInput(tx1.getHash(), 2); // should be 0 here, if 1 signature is error
        tx3.addOutput(9, kScrooge.getPublic());
        tx3.signTX(kAlice.getPrivate(), 0);

        // Pay coins transactions 4: [ Alice -> Bob amount<values=3>, Alice -> Alice<value=6> ]
        // but with wrong signature.
        tx4 = new TxHandlerTest.TX();
        tx4.addInput(tx1.getHash(), 0);
        tx4.addOutput(3, kBob.getPublic());
        tx4.addOutput(6, kAlice.getPublic());
        tx4.signTX(kScrooge.getPrivate(), 0); //

        // Pay coins transactions 3: [Scrooge -> Bob<values=8>,  Scrooge -> Alexa<values=3>]
//        tx3 = new TxHandlerTest.TX();
//        pkAlexa = KeyPairGenerator.getInstance("RSA").generateKeyPair();
//        tx3.addInput(tx0.getHash(), 0);
//        tx3.addOutput(8, pkBob.getPublic());
//        tx3.addOutput(3, pkAlexa.getPublic());
//        tx3.signTX(pkScrooge.getPrivate(), 0);
    }

    /**
     * -------------------------------------- isValidTx() Function Test -------------------------------------- *
     * 1. Function Introduction:
     * The function {@code isValidTx()} is used to verify the validity of each transaction.
     * It returns Ture if
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}'s output values are non-negative, and
     * (5) the sum of {@code tx}'s input values is greater than or equal to the sum of
     * 2. Test Strategy:
     * Violate each rule to each function and test the results one by one.
     * ------------------------------------------------------------------------------------------------------- *
     */

    @Test
    public void test_tx_is_valid() {
        // tx1 should be valid
        assertTrue(txHandler.isValidTx(tx1));
    }

    @Test
    public void test_tx_input_not_in_utxoPool1() {
        // tx2 should `be not in the current UTXO pool` because tx1 hasn't been handled.
        assertFalse(txHandler.isValidTx(tx2));
        // after handling the tx1, tx2 should be valid
        txHandler.handleTxs(new Transaction[]{tx1});
        assertTrue(txHandler.isValidTx(tx2));
    }

    @Test
    public void test_tx_input_not_in_utxoPool2() {
        // tx3 should `be not in the current UTXO pool` because tx1 hasn't been handled.
        assertFalse(txHandler.isValidTx(tx2));
        // handle the pre txs, tx3 should also `be not in the current UTXO pool` because of the wrong index
        txHandler.handleTxs(new Transaction[]{tx1});
        assertFalse(txHandler.isValidTx(tx3));
    }

    @Test
    public void test_tx_input_with_wrong_signature() {
        // handle the pre txs, tx4 should be `signature is invalid` because of the wrong signature
        txHandler.handleTxs(new Transaction[]{tx1});
        assertFalse(txHandler.isValidTx(tx4));
    }
}
