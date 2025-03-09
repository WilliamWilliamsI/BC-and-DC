/**
 * @Time : 2025/3/8 17:29
 * @Author : Karry Ren
 * @Comment:
 **/

package test.java;

import main.java.Transaction;
import main.java.TxHandler;
import main.java.UTXOPool;
import org.junit.Test;

import java.security.*;


public class TxHandlerTest {
    public static void main(String[] args) {
        TxHandler th = new TxHandler(new UTXOPool());
        System.out.println(th.isValidTx(new Transaction()));
    }
}
