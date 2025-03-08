import java.security.*;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

public class TestMain {

    public TestMain() throws NoSuchAlgorithmException {
    }
    // generate key pairs, simulate initial tx from Jason to Alice to Bob
    KeyPair Jason_KeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    KeyPair Alice_KeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    KeyPair Bob_KeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    TransactionBuilder transactionBuilder = new TransactionBuilder();// make TransactionBuilder instance

    @Test
    //test situation 1: valid transactions, show basic functions
    //tx0: Jason    --> Jason[0] 12coins [Create Coins]
    //tx1: Jason[0] --> Jason[0] 4coins  [Divide Coins]
    //              --> Jason[1] 8coins
    //tx2: Jason[0] --> Alice[0] 4coins  [Pay separately]
    //     Jason[1] --> Alice[1] 8coins
    //tx3: Alice[0] --> Alice[2] 3coins  [Divide Coins]
    //              --> Alice[3] 1coins
    //tx4: Alice[1],Alice[2]  --> Bob[0]   8+3coins  [Pay jointly]
    public void testValidTransaction() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        //tx0: Jason    --> Jason[0] 12coins [Create Coins]
        Transaction tx0 = transactionBuilder.createInitialTransaction(12,Jason_KeyPair.getPublic());

        UTXOPool utxoPool = new UTXOPool();
        UTXO utxo = new UTXO(tx0.getHash(), 0);
        utxoPool.addUTXO(utxo, tx0.getOutput(0));

        //tx1: Jason[0] --> Jason[0] 4coins  [Divide Coins]
        //    	        --> Jason[1] 8coins
        List<Integer> tx1AmountList;
        tx1AmountList = new ArrayList<>();
        tx1AmountList.add(4);
        tx1AmountList.add(8);

        List<PublicKey> tx1PublicKeyList;
        tx1PublicKeyList = new ArrayList<>();
        for (int i = 0; i < 2; i++){tx1PublicKeyList.add(Jason_KeyPair.getPublic());}
        Transaction tx1 = transactionBuilder.createSubdividedTransaction(tx0,Jason_KeyPair.getPrivate(),tx1AmountList,tx1PublicKeyList);

        TxHandler txHandler = new TxHandler(utxoPool);
        System.out.println("txHandler.isValidTx(tx1) returns: " + txHandler.isValidTx(tx1));

        assertEquals("tx1:One valid transaction", 1, txHandler.handleTxs(new Transaction[]{tx1}).length);
        // update utxo pool
        utxoPool = txHandler.getHandledUtxoPool();
        assertEquals("tx1:Two UTXOs are created", 2, utxoPool.getAllUTXO().size());

        //tx2: Jason[0] --> Alice[0] 4coins  [Pay separately]
        //     Jason[1] --> Alice[1] 8coins

        List<Integer> tx2AmountList;
        tx2AmountList = new ArrayList<>();
        tx2AmountList.add(4);
        tx2AmountList.add(8);

        List<PublicKey> tx2PublicKeyList;
        tx2PublicKeyList = new ArrayList<>();
        for (int i = 0; i < 2; i++){tx2PublicKeyList.add(Alice_KeyPair.getPublic());}

        Transaction tx2  = transactionBuilder.one_to_morePaymentTransaction(tx1,Jason_KeyPair.getPrivate(),tx2AmountList,tx2PublicKeyList);

        txHandler = new TxHandler(utxoPool);
        System.out.println("txHandler.isValidTx(tx2) returns: " + txHandler.isValidTx(tx2));
        assertEquals("tx2:One valid transaction", 1, txHandler.handleTxs(new Transaction[]{tx2}).length);
        // update utxo pool
        utxoPool = txHandler.getHandledUtxoPool();
        assertEquals("tx2:Two UTXOs are used and two UTXOs are created", 2, utxoPool.getAllUTXO().size());

        //tx3: Alice[0] --> Alice[2] 3coins  [Divide Coins]
        //              --> Alice[3] 1coins
        List<Integer> tx3AmountList;
        tx3AmountList = new ArrayList<>();
        tx3AmountList.add(3);
        tx3AmountList.add(1);

        List<PublicKey> tx3PublicKeyList;
        tx3PublicKeyList = new ArrayList<>();
        for (int i = 0; i < 2; i++){tx3PublicKeyList.add(Alice_KeyPair.getPublic());}
        Transaction tx3 = transactionBuilder.createSubdividedTransaction(tx2,Alice_KeyPair.getPrivate(),tx3AmountList,tx3PublicKeyList);

        txHandler = new TxHandler(utxoPool);
        System.out.println("txHandler.isValidTx(tx3) returns: " + txHandler.isValidTx(tx3));
        assertEquals("tx3:One valid transaction", 1, txHandler.handleTxs(new Transaction[]{tx3}).length);
        // update utxo pool
        utxoPool = txHandler.getHandledUtxoPool();
        assertEquals("tx3:One UTXOs is used and Two UTXOs are created", 3, utxoPool.getAllUTXO().size());

        //tx4: Alice[1],Alice[2]  --> Bob[0]   8+3coins  [Pay jointly]
        List<Transaction> tx4PrevTx;
        tx4PrevTx = new ArrayList<>();
        tx4PrevTx.add(tx2);
        tx4PrevTx.add(tx3);

        List<Integer> index;
        index = new ArrayList<>();
        index.add(1);
        index.add(0);
        Transaction tx4 = transactionBuilder.more_to_onePaymentTransaction(tx4PrevTx,index,Alice_KeyPair.getPrivate(),11,Bob_KeyPair.getPublic());

        txHandler = new TxHandler(utxoPool);
        System.out.println("txHandler.isValidTx(tx4) returns: " + txHandler.isValidTx(tx4));
        assertEquals("tx4:One valid transaction", 1, txHandler.handleTxs(new Transaction[]{tx4}).length);
        // update utxo pool
        utxoPool = txHandler.getHandledUtxoPool();
        assertEquals("tx4:Two UTXOs are used and one UTXOs is created", 2, utxoPool.getAllUTXO().size());
    }

    @Test
    //use the txs example above
    public void TestAllTx_OneTime() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException{
        //tx0: Jason    --> Jason[0] 12coins [Create Coins]
        Transaction tx0 = transactionBuilder.createInitialTransaction(12,Jason_KeyPair.getPublic());

        UTXOPool utxoPool = new UTXOPool();
        UTXO utxo = new UTXO(tx0.getHash(), 0);
        utxoPool.addUTXO(utxo, tx0.getOutput(0));

        //tx1: Jason[0] --> Jason[0] 4coins  [Divide Coins]
        //    	        --> Jason[1] 8coins
        List<Integer> tx1AmountList;
        tx1AmountList = new ArrayList<>();
        tx1AmountList.add(4);
        tx1AmountList.add(8);

        List<PublicKey> tx1PublicKeyList;
        tx1PublicKeyList = new ArrayList<>();
        for (int i = 0; i < 2; i++){tx1PublicKeyList.add(Jason_KeyPair.getPublic());}
        Transaction tx1 = transactionBuilder.createSubdividedTransaction(tx0,Jason_KeyPair.getPrivate(),tx1AmountList,tx1PublicKeyList);


        //tx2: Jason[0] --> Alice[0] 4coins  [Pay separately]
        //     Jason[1] --> Alice[1] 8coins

        List<Integer> tx2AmountList;
        tx2AmountList = new ArrayList<>();
        tx2AmountList.add(4);
        tx2AmountList.add(8);

        List<PublicKey> tx2PublicKeyList;
        tx2PublicKeyList = new ArrayList<>();
        for (int i = 0; i < 2; i++){tx2PublicKeyList.add(Alice_KeyPair.getPublic());}

        Transaction tx2  = transactionBuilder.one_to_morePaymentTransaction(tx1,Jason_KeyPair.getPrivate(),tx2AmountList,tx2PublicKeyList);


        //tx3: Alice[0] --> Alice[2] 3coins  [Divide Coins]
        //              --> Alice[3] 1coins
        List<Integer> tx3AmountList;
        tx3AmountList = new ArrayList<>();
        tx3AmountList.add(3);
        tx3AmountList.add(1);

        List<PublicKey> tx3PublicKeyList;
        tx3PublicKeyList = new ArrayList<>();
        for (int i = 0; i < 2; i++){tx3PublicKeyList.add(Alice_KeyPair.getPublic());}
        Transaction tx3 = transactionBuilder.createSubdividedTransaction(tx2,Alice_KeyPair.getPrivate(),tx3AmountList,tx3PublicKeyList);


        //tx4: Alice[1],Alice[2]  --> Bob[0]   8+3coins  [Pay jointly]
        List<Transaction> tx4PrevTx;
        tx4PrevTx = new ArrayList<>();
        tx4PrevTx.add(tx2);
        tx4PrevTx.add(tx3);

        List<Integer> index;
        index = new ArrayList<>();
        index.add(1);
        index.add(0);
        Transaction tx4 = transactionBuilder.more_to_onePaymentTransaction(tx4PrevTx,index,Alice_KeyPair.getPrivate(),11,Bob_KeyPair.getPublic());


        TxHandler txHandler = new TxHandler(utxoPool);

        assertEquals("tx1,2,3,4: four valid transaction", 4, txHandler.handleTxs(new Transaction[]{tx1, tx2, tx3, tx4}).length);
        // update utxo pool
        utxoPool = txHandler.getHandledUtxoPool();
        assertEquals("tx1,2,3,4:Two UTXOs are left", 2, utxoPool.getAllUTXO().size());
    }

    @Test
    // test situation 2: include the following invalid cases
    //tx0: Jason --> Jason[0] 12coins [Create Coins]
    //tx1: Jason[0] --> Alice[0] 12coins [Pay Coins]
    //tx2: Alice[0] --> Bob[0]   -1coins [*negative output number*]
    //tx3: Alice[0] --> Bob[0]   13coins [*output number exceed input number*]
    //tx4: Alice[0] --> Bob[0]   12coins [*Signed by Bob*]
    //tx5: Alice[0] --> Bob[0]   12coins [Pay Coins *NOT added to UTXO pool* by removing]
    //tx6: Bob[0] --> Jason[0]   12coins [Pay Coins *Previous Tx NOT in UTXO pool*]
    public void testInvalidTransaction() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        //tx0: Jason    --> Jason[0] 12coins [Create Coins]
        TransactionBuilder transactionBuilder = new TransactionBuilder();// make TransactionBuilder instance
        Transaction tx0 = transactionBuilder.createInitialTransaction(12,Jason_KeyPair.getPublic());

        UTXOPool utxoPool = new UTXOPool();
        UTXO utxo = new UTXO(tx0.getHash(), 0);
        utxoPool.addUTXO(utxo, tx0.getOutput(0));

        //tx1: Jason[0] --> Alice[0] 12coins [Pay Coins]
        Transaction tx1 = transactionBuilder.one_to_onePaymentTransaction(tx0,Jason_KeyPair.getPrivate(),12,Alice_KeyPair.getPublic());

        TxHandler txHandler = new TxHandler(utxoPool);
        System.out.println("txHandler.isValidTx(tx1) returns: " + txHandler.isValidTx(tx1));
        assertEquals("tx1:add one valid transaction", 1, txHandler.handleTxs(new Transaction[]{tx1}).length);
        // update utxo pool
        utxoPool = txHandler.getHandledUtxoPool();
        assertEquals("tx1:one UTXO's created.", 1, utxoPool.getAllUTXO().size());

        //tx2: Alice[0] --> Bob[0]   -1coins [*negative output number*]
        Transaction tx2 = transactionBuilder.one_to_onePaymentTransaction(tx1,Alice_KeyPair.getPrivate(),-1,Bob_KeyPair.getPublic());

        txHandler = new TxHandler(utxoPool);
        System.out.println("txHandler.isValidTx(tx2) returns: " + txHandler.isValidTx(tx2) +". The output number is negative -1!");
        assertEquals("tx2:add no invalid transaction", 0, txHandler.handleTxs(new Transaction[]{tx2}).length);
        // update utxo pool
        utxoPool = txHandler.getHandledUtxoPool();
        assertEquals("tx2:one UTXO's remained", 1, utxoPool.getAllUTXO().size());

        //tx3: Alice[0] --> Bob[0]   13coins [*output number exceed input number*]
        Transaction tx3 = transactionBuilder.one_to_onePaymentTransaction(tx1,Alice_KeyPair.getPrivate(),13,Bob_KeyPair.getPublic());

        txHandler = new TxHandler(utxoPool);
        System.out.println("txHandler.isValidTx(tx3) returns: " + txHandler.isValidTx(tx3) + ". The output 13 exceed input 12");
        assertEquals("tx3:add no invalid transaction", 0, txHandler.handleTxs(new Transaction[]{tx3}).length);
        // update utxo pool
        utxoPool = txHandler.getHandledUtxoPool();
        assertEquals("tx3:one UTXO's remained", 1, utxoPool.getAllUTXO().size());

        //tx4: Alice[0] --> Bob[0]   12coins [*Signed by Bob*]
        Transaction tx4 = transactionBuilder.one_to_onePaymentTransaction(tx1,Bob_KeyPair.getPrivate(),12,Bob_KeyPair.getPublic());

        txHandler = new TxHandler(utxoPool);
        System.out.println("txHandler.isValidTx(tx4) returns: " + txHandler.isValidTx(tx4) + ". Wrongly signed by Bob!");
        assertEquals("tx4:add no invalid transaction", 0, txHandler.handleTxs(new Transaction[]{tx4}).length);
        // update utxo pool
        utxoPool = txHandler.getHandledUtxoPool();
        assertEquals("tx4:one UTXO's remained", 1, utxoPool.getAllUTXO().size());

        //tx5: Alice[0] --> Bob[0]   12coins [Pay Coins *NOT added to UTXO pool* by removing]
        Transaction tx5 = transactionBuilder.one_to_onePaymentTransaction(tx1,Alice_KeyPair.getPrivate(),12,Bob_KeyPair.getPublic());

        utxo = new UTXO(tx5.getHash(),0);
        txHandler = new TxHandler(utxoPool);
        System.out.println("txHandler.isValidTx(tx5) returns: " + txHandler.isValidTx(tx5));
        assertEquals("tx5:add one valid transaction", 1, txHandler.handleTxs(new Transaction[]{tx5}).length);
        // update utxo pool
        utxoPool = txHandler.getHandledUtxoPool();
        utxoPool.removeUTXO(utxo);
        assertEquals("tx5: UTXO has been removed.", 0, utxoPool.getAllUTXO().size());

        //tx6: Bob[0] --> Jason[0]   12coins [Pay Coins *Previous Tx NOT in UTXO pool*]
        Transaction tx6 = transactionBuilder.one_to_onePaymentTransaction(tx5,Bob_KeyPair.getPrivate(),12,Jason_KeyPair.getPublic());
        System.out.println("txHandler.isValidTx(tx6) returns: " + txHandler.isValidTx(tx6) + ". Previous Tx NOT in UTXO pool!");
        assertEquals("tx2:no valid transaction", 0, txHandler.handleTxs(new Transaction[]{tx6}).length);
        // update utxo pool
        utxoPool = txHandler.getHandledUtxoPool();
        assertEquals("tx2:no UTXOs created.", 0, utxoPool.getAllUTXO().size());
    }

    @Test
    //test situation 3: double spending
    //tx0: Jason    --> Jason[0] 12coins [Create Coins]
    //tx1: Jason[0] --> Alice[0] 12coins [Pay Coins]
    //tx2: Jason[0] --> Bob[0]   12coins [*Double-spending*]
    public void testDoubleSpent() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        //tx0: Jason    --> Jason[0] 12coins [Create Coins]
        TransactionBuilder transactionBuilder = new TransactionBuilder();// make TransactionBuilder instance
        Transaction tx0 = transactionBuilder.createInitialTransaction(12,Jason_KeyPair.getPublic());

        UTXOPool utxoPool = new UTXOPool();
        UTXO utxo = new UTXO(tx0.getHash(), 0);
        utxoPool.addUTXO(utxo, tx0.getOutput(0));

        //tx1: Jason[0] --> Alice[0] 12coins [Pay Coins]
        Transaction tx1 = transactionBuilder.one_to_onePaymentTransaction(tx0,Jason_KeyPair.getPrivate(),12,Alice_KeyPair.getPublic());

        TxHandler txHandler = new TxHandler(utxoPool);
        System.out.println("txHandler.isValidTx(tx1) returns: " + txHandler.isValidTx(tx1));
        assertEquals("tx1:add one valid transaction", 1, txHandler.handleTxs(new Transaction[]{tx1}).length);
        // update utxo pool
        utxoPool = txHandler.getHandledUtxoPool();
        assertEquals("tx1:one UTXO's created.", 1, utxoPool.getAllUTXO().size());

        //tx2: Alice[0] --> Bob[0]   12coins [*negative output number*]
        Transaction tx2 = transactionBuilder.one_to_onePaymentTransaction(tx0,Alice_KeyPair.getPrivate(),12,Bob_KeyPair.getPublic());

        txHandler = new TxHandler(utxoPool);
        System.out.println("txHandler.isValidTx(tx2) returns: " + txHandler.isValidTx(tx2) +". Double spending!");
        assertEquals("tx2:add no invalid transaction", 0, txHandler.handleTxs(new Transaction[]{tx2}).length);

        // update utxo pool
        utxoPool = txHandler.getHandledUtxoPool();
        assertEquals("tx2:no new UTXOs created.", 1, utxoPool.getAllUTXO().size());

    }
}
