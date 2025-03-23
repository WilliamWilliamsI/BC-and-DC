# H2 Project: Block Chain

```python
H2_Proejct/
├── docs/
    ├── Homework_2.pdf # The requirements of homework2.
    └── Images.pptx # The images of homework 2.
├── src/ # The source codes (code framework is following the maven framework).
    ├── main.java/ # The main classes.
    ├── test.java/ # The test classes.
├── H2_Project.iml # The config file for Intellij IDEA.
└── .gitignore # The gitignore file.
```



## 1. Overview & Analysis

:pencil: The detailed requirements of homework 2 are in the `docs/Homework_2.pdf`. 

- Overall, we will implement a **node** that’s part of a block-chain-based distributed consensus protocol. Our code will receive **incoming transactions and blocks** and maintain an updated block chain. 
- Specifically, we need to **implement** the `BlockChain` class, which is responsible for maintaining a block chain. Since the entire block chain could be huge in size, we should only keep around the most recent blocks.  We also need to **do the testing**.
- Attention: Since **there can be (multiple) forks**, blocks form **a tree rather than a list**. Our design should take this into account. We have to maintain a UTXO pool corresponding to **every block** on top of which a **new block might be created**.

Based on the handling of transactions already implemented in homework 1, we started to build the block chain in depth. The block chain needs to be maintained and changed according to the established rules.

:recycle: To complete this assignment, we must first analyze **the given class code**:

- **`UTXO.java`** and **`UTXOPool.java`** are same as the homework 1, which are used to manage the unspent transactions outputs. **` Crypto.java`** is also same as the one provided in homework 1.
- **`TxHandler.java`** has the same logic as my previous implementation. However, it simplifies the implementation of the `handleTxs()` function and does not take into account the unordered nature of the proposed transactions. It also **adds a new function `getUTXOPool()`** to it.
- **` Transaction.java`** is similar to `Transaction.java` as provided in homework 1 except for introducing functionality to create a `coinbase` transaction.
- **`ByteArrayWrapper.java`** is a utility file which creates a wrapper for byte arrays such that it could be used as a key in hash functions (this class is with `hashCode()` and `equals()` function implemented). 
- **`Block.java`** stores the block data structure. It contains at least one transaction (the one called `coinbase` that received `COINBASE`), and the rest of the transactions are in a separate array `txs[]`.
- **`BlockHandler.java`** uses `BlockChain.java` to process a newly received block, create a new block, or process a newly received transaction.

There are also some more detailed information: The coinbase value is kept constant at 25 bitcoins whereas in reality it halves roughly every 4 years and is currently 12.5 BTC. So, the `Block.COINBASE = 25`.

:page_facing_up: Based on these elements, we need to finish **implementing and testing** the `BlockChain.java`:

```java
// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory
// as it would cause a memory overflow.

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
  
    /**
     * Create an empty block chain with just a genesis block. 
     * Assume {@code genesisBlock} is a valid block.
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
    }
  
    /**
     * Get the maximum height block.
     */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
    }
  
    /**
     * Get the UTXOPool for mining a new block on top of max height block.
     */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
    }
  
    /**
     * Get the transaction pool to mine a new block.
     */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
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
        // IMPLEMENT THIS
    }
  
    /** 
     * Add a transaction to the transaction pool 
     */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
    }
}
```

:gear: More detailed requirements:

- A new genesis block won’t be mined. If you receive a block which claims to be a genesis block (parent is a null hash) in the `addBlock(Block b)` function, you can return `false`.
- If there are multiple blocks at the same height, return the **oldest block** in `getMaxHeightBlock()` function.
- Assume for simplicity that a coinbase transaction of a block **is available to be spent in the next block mined on top of it**. (This is contrary to the actual Bitcoin protocol when there is a “maturity” period of 100 confirmations before it can be spent).
- Maintain only **one global Transaction Pool** for the block chain and keep adding transactions to it on receiving transactions and remove transactions from it if a new block is received or created. It’s okay if some transactions get dropped during a block chain reorganization, i.e., when a side branch becomes the new longest branch. Specifically, transactions present in the original main branch (and thus removed from the transaction pool) but absent in the side branch **might get lost**.
- When checking for validity of a newly received block, just checking if the transactions form a valid set is enough. The set need not be a maximum possible set of transactions. Also, you needn’t do any proof-of-work checks.



## 2. Implementation & Test

The runnable result code is a and b.  In this section, I will elaborate the **implementation** and **test** code writing logic:

- The **implementation of BlockChain class**.
- The **test suite** to verify the related implementation.

### 2.1 Details of the implementation

**Part 0. Storage structure of block chain.**

Given there might be multiple forks, the data structure of the block chain **should be a tree rather than a list**. While, for the storage structure, there is no need to store the blocks in the tree because we can create a tree data structure using a list by storing the hash of this block and the state of this block in the block chain. The state of the block records the **1) content of this block**, **2) the height of the block** and **3) the corresponding UTXOPool**. Based on this, I build a `BlockState` class in `BlockState.java` to store these three elements and the ways to get them.

Considering that **the order of storage is meaningless**, it would be inconvenient if we use the list to store the block chain. Finally, I decided to **use Map as the storage structure**, as shown in Figure 1.  The attribute in `BlockChain` should be defined as:

```java
private Map<byte[], BlockState> blockChain;
```

The `hash` of the block is the key and the `state` of the blcok is the value in this map, and they can form a one-to-one mapping. At the same time, I maintain a state variable `latestBlockState` that indicates the status of the latest block in the longest valid branch. This not only allows for an **efficient implementation** of the data storage structure, but also **improves the efficiency** of block chain related operations, such as searching.

**Part 1. Construtor: `BlockChain(Block genesisBlock)`**

This constructor is used to create a block chain with just a genesis block. The implementation can be devided into **four steps**:

- Step 1. Initialize the state of genesisBlock `genesisBlockState`, including the block, its height, and the current utxoPool.

  ```java
  Transaction coinbaseTx = genesisBlock.getCoinbase();
  UTXOPool genesisUtxoPool = new UTXOPool();
  UTXO utxo = new UTXO(coinbaseTx.getHash(), 0);
  genesisUtxoPool.addUTXO(utxo, coinbaseTx.getOutput(0));
  BlockState genesisBlockChainState = new BlockState(genesisBlock, 1, genesisUtxoPool);
  ```

- Step 2. Add the `genesisBlockState` to an empty block chain.

  ```java
  blockChain = new HashMap<byte[], BlockState>();
  blockChain.put(genesisBlock.getHash(), genesisBlockChainState);
  ```

- Step 3: Initialize the `latestBlockState` to record the latest block's state in the longest valid branch.

  ```java
  latestBlockState = genesisBlockChainState;
  ```

- Step 4: initialize the global Transaction Pool `transactionPool`.

  ```java
  transactionPool = new TransactionPool();
  ```

**Part 2. Get Max Height Block State: `getMaxHeightBlock()` & `getMaxHeightUTXOPool()`**

These two methods are used to get the **maximum height block** and the **UTXOPool** for mining a new block on top of max height block respectively. Since I have recorded the latest block's state in the longest valid branch, as long as the  `latestBlockState` can be managed wisely, I can get them easily using the following interface:

```java
public Block getMaxHeightBlock() {
    return latestBlockState.block;
}

public UTXOPool getMaxHeightUTXOPool() {
    return latestBlockState.utxoPool;
}
```

Attention: If there are multiple blocks at the same height, the `getMaxHeightBlock()` function should return the **oldest block**, so when faced with the same height, `latestBlockState` stores the state of the oldest block at all times.

**Part 3. Get the Pool of Txs: `getTransactionPool()`**

This method is used to get the transaction pool to mine a new block. From the assumptions and hints, we know that we could maintain **only one global transaction pool for the block chain**. So the interface should be:

```java
public TransactionPool getTransactionPool() {
    return new TransactionPool(transactionPool);
}
```

**Part 4. Add block to block chain: `addBlock()`**

This method is used to add the block to the block chain if it is valid.  It returns `true` only if the block meets the following three conditions:

- Condition 1. The block's parent node is valid.  If the block claims to be a genesis block (parent is a null hash) or its parent node isn't in the previous block chain, returns `false`.

  ```java
  if (block.getPrevBlockHash() == null || !blockChain.containsKey(block.getPrevBlockHash())) {
      return false;
  }
  ```

- Condition 2. All the transactions in the block should be valid. This condition makes sure that there is **no invalid transactions in the block**. Using `block.getTransactions()` to get the block's transactions and checking by `TxHandler().handleTxs()`, if the number of transaction is not change, then all are valid!

  ```java
  else if (!allTransactionsValid(block)) {
      return false;
  }
  
  private boolean allTransactionsValid(Block block) {
      // get the block's transactions
      Transaction[] blockTxs = block.getTransactions().toArray(new     Transaction[block.getTransactions().size()]);
      // checking them by handler to get valid transactions
      BlockState parentBlockState = blockChain.get(block.getPrevBlockHash());
      Transaction[] validTxs = new TxHandler(parentBlockState.getUtxoPool()).handleTxs(blockTxs);
      // test whether number of Txs is change or not
      return blockTxs.length == validTxs.length;
  }
  ```

- Condition 3. The CUT_OFF_AGE condition should be satisfied. For simplicity, the block should also meet the criteria that the `height > (maxHeight - CUT_OFF_AGE)`. If not, it returns `false`. Get the height of the block's parent node, check if `parentBlockState Height + 1 > maxHeight - CUT_OFF_AGE`.

  ```java
  else if (!CUT_OFF_AGE_Condition(block)) {
      return false;
  }
  
  private boolean CUT_OFF_AGE_Condition(Block block) {
      BlockState parentBlockState = blockChain.get(block.getPrevBlockHash());
      return parentBlockState.getHeight() + 1 > latestBlockState.getHeight() - CUT_OFF_AGE;
  }
  ```

If the given block can meet the above three conditions, it returns `true`, which means the block is valid and can be added to the block chain. Then we need to do the following 3 steps to finish the `addBlock()` process:

- Step 1. Add this block into the block chain by recording its block state and the hash into the map `blockChain`.
- Step 2. Update the TransactionPool. From the assumptions and hints we know that it’s okay if some transactions get dropped during a block chain reorganization. So I don't take the block chain reorganization's effect into consideration and remove the block's transactions out of the TransactionPool.
- Step 3. Delete the previous block to meet the storage condition. Since the entire block chain could be huge in size, I just keep around the most recent blocks. The threshold value is represented as `STORAGE=20`.

The code of this part is long, so i don't paste it here, you can check it in the raw code file.

**Part 5. Add Transactions: `addTransaction()`**

This method is used to add a transaction to the transaction pool.

```java
public void addTransaction(Transaction tx) {
    transactionPool.addTransaction(tx);
}
```



### 2.2 Details of the test suite



## 3. Environment

:e-mail: Because JDK versions are updated so quickly, there are a lot of things in the old code that will be wrong in the new JDK version, such as the `finalize()` function. It is highly recommended to **install Java 8 instead of the latest version of the JDK**. 

:star2: **All of the code in this repo is run on the MacOS (M2) with JDK1.8, [junit-4.13.2](https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar) and [hamcrest-1.3](https://repo1.maven.org/maven2/org/hamcrest/hamcrest-all/1.3/hamcrest-all-1.3.jar)**. Just download the `.jar` files and use the `file => project structure` to organize them.

:cat: To demonstrate the reproducibility of the results, I built a CI tool using GitHub Actions to run the test code online and show the results. Figure 4. shows the results from the CI, and again, it passes all the tests!



## References

:books: Actually, the homework for this class **haven't changed since 2018**. For the 2018 to 2019 academic year, the siblings use GitHub to manage the tasks, so you can find a ton of [**references**](https://github.com/search?q=PHBS_BlockChain&type=repositories).

There are 2 high quality open-sourced codes we can refer to. Thanks for the siblings!

- [**JiZhong Cao**](https://github.com/1901212561/PHBS_BlockChain_2019/)
- [**TingWei Shen**](https://github.com/SnakeWayne/PHBS_BlockChain_2019)

