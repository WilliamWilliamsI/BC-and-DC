/**
 * @Time : 2025/3/23 10:23
 * @Author : Karry Ren
 * @Comment: The added class for recording the 1) content of this block**, 2) the height of the block
 * and 3) the corresponding UTXOPool. This class is used to implement the blockChain using Map storage
 * structure rather than the tree.
 **/

package main.java;

public class BlockState {
    // 1) the content of this block
    private Block block;
    // 2) the height of the block in blockChain
    private int height;
    // 3) the corresponding UTXOPool
    private UTXOPool utxoPool;

    public BlockState(Block block, int height, UTXOPool utxoPool) {
        this.block = block;
        this.height = height;
        this.utxoPool = utxoPool;
    }

    public int getHeight() {
        return height;
    }

    public UTXOPool getUtxoPool() {
        return utxoPool;
    }
}
