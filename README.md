# common2.pool.stat
org.apache.commons.pool2 是帮助构建一个池子的工具，jedis/lettcue 的连接池都用了他。这里把这个池子的状态从jmx中拿出来返回为一个json，方便接入一些监控系统
