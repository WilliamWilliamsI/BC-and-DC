package test.java;

import main.java.Transaction;

import org.junit.Test;

import java.security.*;

/**
 * @Time : 2025/3/8 17:29
 * @Author : Karry Ren
 * @Comment:
 **/

public class TxHandlerTest {
    // Extends the transaction class and add the new function: add signature.
    public static class TX extends Transaction {
        public void signTX(PrivateKey sk, int index) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
            // Generate the signature.
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(sk);
            signature.update(this.getRawDataToSign(index));
            // Add the signature to the transaction.
            this.addSignature(signature.sign(), index);
            this.finalize();
        }
    }

    public static void main(String[] args) {
        System.out.print("Testing TxHandler\n");
    }
}
