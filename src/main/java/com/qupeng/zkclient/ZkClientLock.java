package com.qupeng.zkclient;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

/**
 * 基于zkClient第三方客户端实现分布式锁
 *
 */
public class ZkClientLock {

    //zkclient第三档客户端对象
    private ZkClient zkClient;

    private String zkAddress = "127.0.0.1:2181";

    //分布式锁的根节点的名称
    private String lockRootName = "/locks";

    //锁节点的名称
    private String lockName;

    //当前的锁节点名称
    private String currentLockName;

    private static final int sessionTimeOut = 10000;

    private static final int connectionTimeOut = 25000;

    //默认的节点的数据
    private static final byte[] bytes = new byte[0];

    //倒计数器
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     * 构造方法
     *
     * @param lockName
     */
    public ZkClientLock(String lockName) {
        //锁节点的名称通过构造方法初始化
        this.lockName = lockName;
        try {
            //建立zookeeper连接
            zkClient = new ZkClient(zkAddress, sessionTimeOut, connectionTimeOut);

            //创建zkclient对象完成，说明连接zookeeper成功，我们创建一个锁的根节点 lockRootName = "/locks";
            /**
             * --/locks  --业务区分
             *   --storeLock000000001
             *   --storeLock000000001
             *   --storeLock000000001
             *   ......
             */
            boolean isExists = zkClient.exists(lockRootName);
            //如果锁的根节点不存在
            if (!isExists) {
                //根节点持久化，acl的开放的
                zkClient.create(lockRootName, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * zookeeper分布式锁：加锁 (获取分布式锁)
     */
    public void lock() {
        try {
            /**
             * 返回：
             * /locks/lockName0000001
             * /locks/lockName0000002
             * /locks/lockName0000003
             * .........
             */
            String myNode = zkClient.create(lockRootName + "/" + lockName, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL);

            //拿到根节点下的所有临时有序子节点
            List<String> subNodes = zkClient.getChildren(lockRootName);

            //把所有子节点排序一下 (默认字典排序，0-9，a-z)
            TreeSet<String> sortNodes = new TreeSet<String>();
            for (String node : subNodes) {
                //    /locks/lockName0000001
                sortNodes.add(lockRootName + "/" + node);
            }

            //从排好顺序的set集合中取第一个节点，它是节点编号最小的
            String minNode = sortNodes.first();

            System.out.println("当前的myNode=" + myNode);
            System.out.println("最小节点minNode=" + minNode);

            //获取一下前一个节点，获取指定节点的前一个节点
            // myNode: /locks/lockName0000003,  （/locks/lockName0000002、/locks/lockName0000001）
            String preNode = sortNodes.lower(myNode);
            System.out.println("前一个节点preNode=" + preNode);

            //最小节点能拿到分布式锁
            if (myNode.equals(minNode)) {
                //当前进来的这个线程所创建的myNode就是分布式锁节点
                currentLockName = myNode;
                //已经获取到分布式锁
                return;
            }

            //其他进来的线程没有拿到分布锁，因为它所创建的节点不是最小的，那么他就监听前一个节点的删除事件
            //一个并发线程工具类：倒计数器
            CountDownLatch countDownLatch = new CountDownLatch(1);
            //判断字符串是否为null
            if (null != preNode) {
                //如果前一个节点不是空的，那么我就要监听前一个节点，当它删除时触发我的监听事件
                boolean isExists = zkClient.exists(preNode);

                if (isExists) {
                    //监听前一个节点
                    zkClient.subscribeDataChanges(preNode, new ZkClientLockWatcher(countDownLatch));

                    countDownLatch.await();

                    //又拿到分布式锁
                    currentLockName = myNode;

                    //倒计数对象置为null
                    countDownLatch = null;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * zookeeper分布式锁：解锁
     */
    public void unLock() {
        //解锁主要就是把当前锁的节点从zookeeper中删除
        //解锁：另外一个做法是直接关闭zookeeper客户端（问题就是：你下次还有用锁，那就需要重新再建立zookeeper连接）
        try {
            if (currentLockName != null) {
                //版本号 -1 表示任何版本，不需要匹配版本，不管你是什么版本，我都可以删除
                zkClient.delete(currentLockName, -1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}