## 先读我
> 曾经在网上看过花样繁多的分页，很多都号称如何通用，但很多时候往往不尽如人意：有在分页类中还加入URL地址信息的，有在分页类中还进行分页动作处理(此动作完全属于操作数据库方面的事情)的。现在好了，经本人总结与提炼：
 无论你是否自己手动分页，还是借助了框架进行分页。此工具类都可以帮助你达到稳定的分页效果(包括导航页码功能)，而且使用方法也相对简单。
 
### 使用场景之Hibernate
此类在构造时最多只要３个参数。由于容错需要，list的setter得进行后继处理。
假设你使用了Hibernate，核心代码如下：
```java
int totalCount=Integer.valueOf(queryCount.uniqueResult().toString());
Pager pager=new Pager<T>(totalCount, pageNumber,limit);
queryList.setFirstResult((pager.getPageNumber()-1)*limit); //容错处理
queryList.setMaxResults(limit);
pager.setList(queryList.list());
return pager;
```

---
#### [附注]讨论详情可参考
[https://bbs.csdn.net/topics/360010907](https://bbs.csdn.net/topics/360010907)

