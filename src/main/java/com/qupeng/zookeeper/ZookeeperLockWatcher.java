/**
 * @项目名：zjsProject
 * @创建人： qupeng
 * @创建时间： 2019-09-05
 * @公司： www.qupeng.com
 * @描述：TODO
 */

package com.qupeng.zookeeper;

/**
 * <p>NAME: ZookeeperLockWatcher</p>
 * @author qupeng
 * @date 2019-09-05 14:54:27
 * @version 1.0
 */

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

/**
 * 监听器，监听接节点事件
 */
public class ZookeeperLockWatcher implements Watcher {

    private CountDownLatch countDownLatch;

    public ZookeeperLockWatcher(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void process(WatchedEvent event) {
        //如果是节点删除事件
        if (event.getType() == Event.EventType.NodeDeleted) {
            //倒计数器减1
            countDownLatch.countDown();
        }
    }
}
