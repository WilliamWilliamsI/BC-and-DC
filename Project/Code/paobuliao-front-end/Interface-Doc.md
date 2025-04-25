# Interface Doc

我再理一下我们要实现的基础功能（接口也就在下面顺带列出）

- **注册（sign-up）功能。**分 buyer 和 seller 两种角色创建 account（身份证号 + 密码 + 重输入的密码）

  ```http
  param: 
  	- <str> id_number
  	- <str> password 
  	- <str> repassword
  	- <int> role (0 for buyer, 1 for seller)
  function: 
  	- id_number 不重复
  	- password = repassword
  	- 添加账户到数据库 (不同的 role 入不同的数据库)
  return: True or False
  ```

- **登陆（sign-in）功能**。分 buyer 和 seller 两种角色进行登陆（身份证号 + 密码）

  ```http
  param: 
  	- <str> id_number
  	- <str> password 
  	- <int> role (0 for buyer, 1 for seller)
  function: 
  	- id_number 在对应数据库能找到
  	- password 匹配
  return: True or False
  ```

- **seller 创建卡功能。**seller 可以自己创建充值卡并发布（一个 seller 可以有多个）。

  ```http
  param: 
  	- <str> id_number
  	- <str> card_id
  	- <str> card_info (卡的基本信息)
  function: 
  	- 执行卡创建合约
  return: (最后一步调用 seller 查询所有卡列表功能 见下)
  该 id_number 下所有的 card 的 list (car_id, card_info, 充值人数, 到帐金额/总充值金额)
  ```

- **buyer 账户功能。**buyer 可以往自己 account 里面充钱。

  ```http
  param: 
  	- <str> id_number
  	- <double> money
  function: 
  	- 执行往账户充钱的合约
  return: 该 id_number 下的 money 总数
  ```

- **buyer 充值卡功能。**buyer 可以在发布出来的充值卡列表中找到某个卡进行充值（一个 buyer 可以充值多个）。

  ```http
  param: 
  	- <str> id_number
  	- <str> card_id
  	- <double> money
  function: 
  	- 执行往卡中充钱的合约
  return: (最后一步调用 buyer 查询所有卡列表功能 见下)
  该 id_number 下的 card 总 list (car_id, card_info, 余额/充值金额)
  ```

- **buyer 消费功能。**buyer 可以在自己已经充值的卡上进行操作，输入本次消费的时间、金额。

  ```http
  param: 
  	- <str> id_number
  	- <str> card_id
  	- <double> money
  function: 
  	- 执行用卡消费的合约
  return: 该 id_number 下的 card 总 list (car_id, card_info, 余额/充值金额)
  ```

- **seller 查询卡基本情况功能。**seller 可以看到自己发布的卡的人数 + 充值金额 + 到帐金额

  ```http
  param:
  	- <str> id_number
  function: 
  	- 查询 id_number 下的 card 总 list
  return: 该 id_number 下所有的 card 的 list (car_id, card_info, 充值人数, 到帐金额/总充值金额)
  ```

- **buyer 查询卡基本情况功能**。buyer 可以看到自己充值的卡的充值金额和剩余金额

  ```http
  param:
  	- <str> id_number
  function: 
  	- 查询 id_number 下的 card 总 list
  return: 该 id_number 下所有的 card 的 list (car_id, card_info, 余额/充值金额)
  ```

- **buyer 终止卡功能。**buyer 可以自己选择 terminate 卡，卡信息消失，充值的钱全部返回 buyer 的 account。

  ```http
  param:
  	- <str> id_number
  	- <str> card_id
  function:
  	- 终止 card 合约
  return: 该 id_number 下所有的 card 的 list (car_id, card_info, 余额/充值金额)
  ```

  

