# H1 Project: ScroogeCoin

```python
H1_Proejct/
├── docs
    ├── Homework_1.pdf # The need of homework 1.
    └── Homework_1.docx # The report of homework 1.
├── src # The source codes (code framework is following the maven framework).
    ├── main.java # The main classes.
      ├── Transaction.java
      ├── Crypto.java
    ├── test.java # The test classes.
└── .gitignore # The gitignore file.
```

This is a brief description of the homework 1 project.



## 1. Overview & Analysis

The detailed requirements of homework 1 are in the `docs/Homework_1.pdf`:

- You will implement the logic used by Scrooge to **process transactions and produce the ledger**.
- Transactions can’t be validated in isolation; it is a tricky problem to choose a subset of transactions that are **together** valid.
- You will be responsible for **creating** a file called `TxHandler.java` that implements the required API and **testing** it.

This is a cryptocurrency trading process that matches what was taught in class. We need to write the transaction handler to ensure that the transaction is correct. To complete this assignment, we must first analyze the given class code:

- ***Transaction***: Represents a ScroogeCoin transaction and has inner classes `Transaction.Output` and `Transaction.Input`. 
  - Consists of **a list of inputs**, which have a **value** and a **public key** to which it is being paid. 
  - Consists of **a list of outputs**,  which have the **hash of the transaction that contains the corresponding output**, the **index** of this output in that transaction, and a **digital signature**. The **raw data that is signed** is obtained from the `getRawDataToSign(int index)` method.
  - Consists of a **unique ID** (see the getRawTx() method).
  - Contains methods to **add and remove an input**, **add an output**, **compute digests** to sign/hash, add a signature to an input, and **compute and store the hash** of the transaction once all inputs/outputs/signatures have been added.
- ***Crypto***: Verify a signature, using the `verifySignature()` method.
- 

**The core class** is the `main.java.Transaction` class that represents a ScroogeCoin transaction and has inner classes `main.java.Transaction.Output ` and `main.java.Transaction.Input`. 这两个类是交易的核心, `Output` 是进行支付的, 包含了支付数量和支付目标的 `PublicKey`. `Input` 是进行获取的, 包含了来源交易的 `hash` 值以及具体的 `index`, 当然还有**数字签名**用于与对应 `Output` 值中的 `PublicKey` 做验证。

紧接着问题就来了, 数字签名到底是怎么来的？优势如何做验证的？

- 数字签名: 使用 ` getRawDataToSign(int index)` 对一个 `main.java.Transaction` 中指定 `index` 的 `Input` 和所有 `Output` 做签名，并标注在该 `Input` 中。
- 验证: 使用  `verifySignature(PublicKey pubKey, byte[] message, byte[] signature)` 借助 `PublicKey` 根据获得的数字签名对目标 `message` 做验证。



## Environment

Because JDK versions are updated so quickly, there are a lot of things in the old code that will be wrong in the new JDK version, such as the `finalize()` function. It is highly recommended to **install Java 8 instead of the latest version of the JDK**. 

:star2: **All of the code in this repo is run on the MacOS (M2) with JDK1.8 and [junit-4.13.2](https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar)**.



## References

There are 2 open-sourced codes we can refer to. Thanks for the siblings.

- https://github.com/SnakeWayne/PHBS_BlockChain_2019
- https://github.com/1901212561/PHBS_BlockChain_2019/

The assignments for this class **haven't changed since 2018**. For the 2018 to 2019 academic year, the siblings use GitHub to manage the tasks, so you can find a ton of [**references**](https://github.com/search?q=PHBS_BlockChain&type=repositories).