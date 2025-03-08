import java.security.*;
import java.util.List;

public class TransactionBuilder {
    private KeyPairGenerator keyPairGenerator;

    public TransactionBuilder() throws NoSuchAlgorithmException {
        keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    }

    public Transaction createInitialTransaction(int amount, PublicKey recipientPublicKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        KeyPair senderKeyPair = keyPairGenerator.generateKeyPair();

        Transaction tx = new Transaction();
        tx.addOutput(amount, recipientPublicKey);

        byte[] initHash = null;
        tx.addInput(initHash, 0);
        tx.signTx(senderKeyPair.getPrivate(), 0);

        return tx;
    }

    public Transaction createSubdividedTransaction(Transaction prevTx,PrivateKey senderPrivateKey ,List<Integer> amounts, List<PublicKey> recipientPublicKeys) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Transaction tx = new Transaction();
        tx.addInput(prevTx.getHash(), 0);

        for (int i = 0; i < amounts.size(); i++) {
            tx.addOutput(amounts.get(i), recipientPublicKeys.get(i));
        }

        tx.signTx(senderPrivateKey, 0);

        return tx;
    }
    public Transaction one_to_onePaymentTransaction(Transaction prevTx, PrivateKey senderPrivateKey, int amounts, PublicKey recipientPublicKeys) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Transaction tx = new Transaction();
        tx.addInput(prevTx.getHash(), 0);

        //first add output
        tx.addOutput(amounts, recipientPublicKeys);

        //then make the sign
        tx.signTx(senderPrivateKey, 0);
        return tx;
    }

    public Transaction one_to_morePaymentTransaction(Transaction prevTx, PrivateKey senderPrivateKey, List<Integer> amounts, List<PublicKey> recipientPublicKeys) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Transaction tx = new Transaction();
        for (int i = 0; i < amounts.size(); i++) {
            tx.addInput(prevTx.getHash(), i);
        }

        //first add output
        for (int i = 0; i < amounts.size(); i++) {
            tx.addOutput(amounts.get(i), recipientPublicKeys.get(i));
        }
        //then make the sign
        for (int i = 0; i < amounts.size(); i++) {
            tx.signTx(senderPrivateKey, i);
        }

        return tx;
    }

    public Transaction more_to_onePaymentTransaction(List<Transaction> prevTx,List<Integer> index, PrivateKey senderPrivateKey,int amounts,PublicKey recipientPublicKeys) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Transaction tx = new Transaction();
        for (int i = 0; i < index.size(); i++) {
            tx.addInput(prevTx.get(i).getHash(), index.get(i));
        }

        //first add output
            tx.addOutput(amounts, recipientPublicKeys);
        //then make the sign
        for (int i = 0; i < index.size(); i++) {
            tx.signTx(senderPrivateKey, i);
        }

        return tx;
    }
}
