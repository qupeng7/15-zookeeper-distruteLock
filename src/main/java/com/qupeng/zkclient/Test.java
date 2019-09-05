package com.qupeng.zkclient;

import com.qupeng.service.GoodsService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Test {

	//倒计算器
	CountDownLatch countDownLatch = new CountDownLatch(1);

	public static void main(String[] args) {
		Test test = new Test();
		test.runThread();
	}

	/**
	 * 多线程并发执行
	 */
	private  void runThread() {
		//spring容器
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		GoodsService goodsService = context.getBean("goodsServiceImpl", GoodsService.class);

		//创建一个确定的线程池
		ExecutorService executorService = Executors.newFixedThreadPool(16);
		for (int i=0; i<20; i++) {
			//提交线程到线程池去执行
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						//等待，线程就位，但是不运行
						countDownLatch.await();
						System.out.println("Thread:"+Thread.currentThread().getName() + ", time: "+System.currentTimeMillis());

						try {
							//TODO 执行业务代码 (超卖测试)
							goodsService.updateByPrimaryKeyStore(1);

						} catch (Throwable e) {
							System.out.println("Thread:"+Thread.currentThread().getName() + e.getMessage());
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
		}
		//倒计算器 -1，那么16个线程就同时开始执行，那么就达到并发效果
		countDownLatch.countDown();

		try {
			// 传达完毕信号，等任务执行完才关闭
			executorService.shutdown();
			// (所有的任务都结束的时候，返回TRUE)
			if(!executorService.awaitTermination(60000, TimeUnit.MILLISECONDS)){
				// 超时的时候向线程池中所有的线程发出中断(interrupted)，立刻马上关闭
				executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			// awaitTermination方法被中断的时候也中止线程池中全部的线程的执行。
			System.out.println("awaitTermination interrupted: " + e);
			executorService.shutdownNow();
		}
	}
}