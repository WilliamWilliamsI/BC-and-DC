# H1 Project: ScroogeCoin

```python
H1_Proejct/
├── docs/
    ├── Homework_1.pdf # The need of homework 1.
    ├── Homework_1.docx # The report of homework 1.
    └── Images.pptx # The images of homework 1.
├── src/ # The source codes (code framework is following the maven framework).
    ├── main.java/ # The main classes.
        ├── Transaction.java
        ├── Crypto.java
        ├── UTXO.java
        ├── UTXOPool.java
        └── TxHandler.java
    ├── test.java/ # The test classes.
└── .gitignore # The gitignore file.
```



## Overview & Analysis

:pencil: The detailed requirements of homework 1 are in the `docs/Homework_1.pdf`:

- We will implement the logic used by Scrooge to **process transactions and produce the ledger**.
- Transactions can’t be validated in isolation; it is a tricky problem to choose a subset of transactions that are **together** valid.
- We will be responsible for **creating** a file called `TxHandler.java` that implements the required API and **testing** it.

This is a cryptocurrency trading process that matches what was taught in class. We need to write the transaction handler to ensure that the transaction is correct. 

:recycle: To complete this assignment, we must first analyze **the given class code**:

- ***Transaction***: Represents a ScroogeCoin transaction and has inner classes `Transaction.Output` and `Transaction.Input`. 
  - Consists of **a list of inputs**, which have a **value** and a **public key** to which it is being paid. 
  - Consists of **a list of outputs**,  which have the **hash of the transaction that contains the corresponding output**, the **index** of this output in that transaction, and a **digital signature**. The **raw data that is signed** is obtained from the `getRawDataToSign(int index)` method.
  - Consists of a **unique ID** (see the getRawTx() method).
  - Contains methods to **add and remove an input**, **add an output**, **compute digests** to sign/hash, add a signature to an input, and **compute and store the hash** of the transaction once all inputs/outputs/signatures have been added.
- ***Crypto***: Verifies a signature, using the `verifySignature()` method.
- ***UTXO***: Represents an **unspent transaction output**. 
  - A UTXO contains the **hash of the transaction** from which it originates as well as **its index** within that transaction.
  - Includes `equals()`, `hashCode()`, and `compareTo()` functions in UTXO that allow the testing of equality and comparison between two UTXOs based **on their indices and the contents of their txHash arrays**.
- ***UTXOPool***: Represents the **current set of outstanding UTXOs** 
  - Contains **a map** from each UTXO to its corresponding transaction output.
  - This class contains **constructors** to create a new empty UTXOPool or a **copy** of a given UTXOPool, and methods to **add and remove** UTXOs from the pool, **get the output** corresponding to a given UTXO, **check** if a UTXO is in the pool, and **get a list of all UTXOs** in the pool.

:page_facing_up: Based on these elements, we need to finish **implementing and testing** the `TxHandler` class:

```java
public class TxHandler {
  
    /** 
     * Creates a public ledger whose current UTXOPool (collection of unspent
     * transaction outputs) is utxoPool. This should make a defensive copy of
     * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
     */
    public TxHandler(UTXOPool utxoPool);
  
    /** 
     * Returns true if
     * (1) all outputs claimed by tx are in the current UTXO pool,
     * (2) the signatures on each input of tx are valid,
     * (3) no UTXO is claimed multiple times by tx,
     * (4) all of tx’s output values are non-negative, and
     * (5) the sum of tx’s input values is greater than or equal to the sum of
           its output values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx);
  
    /** 
     * Handles each epoch by receiving an unordered array of proposed
     * transactions, checking each transaction for correctness,
     * returning a mutually valid array of accepted transactions,
     * and updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs);
}
```

:gear: More detailed requirements:

- The implementation of `handleTxs()` should return a mutually valid transaction **set of maximal size** (one that **can’t be enlarged simply by adding** more transactions). It need not compute a set of maximum size (one for which there is no larger mutually valid transaction set).
- Based on the transactions it has chosen to accept, `handleTxs()` should also **update its internal `UTXOPool`** to reflect the current set of unspent transaction outputs, so that future calls to `handleTxs() `and `isValidTx()` are able to correctly process/validate transactions that claim outputs from transactions that were accepted in a previous call to `handleTxs()`.



## Implementation





## Environment

Because JDK versions are updated so quickly, there are a lot of things in the old code that will be wrong in the new JDK version, such as the `finalize()` function. It is highly recommended to **install Java 8 instead of the latest version of the JDK**. 

:star2: **All of the code in this repo is run on the MacOS (M2) with JDK1.8 and [junit-4.13.2](https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar)**.



## References

:books: Actually, the homework for this class **haven't changed since 2018**. For the 2018 to 2019 academic year, the siblings use GitHub to manage the tasks, so you can find a ton of [**references**](https://github.com/search?q=PHBS_BlockChain&type=repositories).

There are 2 high quality open-sourced codes we can refer to. Thanks for the siblings!

- [**JiZhong Cao**](https://github.com/1901212561/PHBS_BlockChain_2019/)
- [**TingWei Shen**](https://github.com/SnakeWayne/PHBS_BlockChain_2019)

:warning: In addition to these direct references, the framing of background content is critical, and further study of the [references cited by the instructor](https://zhuanlan.zhihu.com/p/121039362) in class can be done here.

