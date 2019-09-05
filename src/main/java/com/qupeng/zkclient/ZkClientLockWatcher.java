package com.qupeng.zkclient;

import org.I0Itec.zkclient.IZkDataListener;

import java.util.concurrent.CountDownLatch;

/**
 * 对节点的监听，当前一个节点被删除的时候，触发节点删除事件的监听
 *
 */
public class ZkClientLockWatcher implements IZkDataListener {

    private CountDownLatch countDownLatch;

    public ZkClientLockWatcher(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void handleDataChange(String dataPath, Object data) throws Exception {
    }

    /**
     * 监听节点删除事件
     *
     * @param dataPath
     * @throws Exception
     */
    @Override
    public void handleDataDeleted(String dataPath) throws Exception {
        //节点删除，把倒计数器减1
        countDownLatch.countDown();
    }
}