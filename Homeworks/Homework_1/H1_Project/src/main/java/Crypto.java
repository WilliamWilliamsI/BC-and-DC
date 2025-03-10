/**
 * @Time : 2025/3/8 16:46
 * @Author : http://bitcoinbook.cs.princeton.edu/.
 * @Comment: The Crypto class for verifying the Signature.
 **/

package main.java;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class Crypto {
    /**
     * @return true is {@code signature} is a valid digital signature of {@code message} under the key {@code pubKey}
     * Internally, this uses RSA signature, but the student does not have to deal with any of the implementation
     * details of the specific signature algorithm.
     */
    public static boolean verifySignature(PublicKey pubKey, byte[] message, byte[] signature) {
        Signature sig = null;
        // define the signature algorithm
        try {
            sig = Signature.getInstance("SHA256withRSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // init the public key
        try {
            sig.initVerify(pubKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        // build up the signature with message and verify
        try {
            sig.update(message);
            return sig.verify(signature);
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        // if any error then false
        return false;
    }
}
