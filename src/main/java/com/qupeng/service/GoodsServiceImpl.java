package com.qupeng.service;


import com.qupeng.mapper.GoodsMapper;
import com.qupeng.model.Goods;
import com.qupeng.service.GoodsService;
import com.qupeng.zkclient.ZkClientLock;
import com.qupeng.zookeeper.ZookeeperLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 当类上和方法上都有@Transactional注解，以方法上的注解为准
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private GoodsMapper goodsMapper;

	//使用原生zookeeper客户端api，底层创建zookeeper连接、创建锁的根节点
//	private ZookeeperLock lock = new ZookeeperLock("storeLock");
//
	//使用第三方zkclient客户端api，底层创建zookeeper连接、创建锁的根节点
	private ZkClientLock lock = new ZkClientLock("storeLock");
//
	/**
	 * 采用Zookeeper分布式锁解决库存超卖问题
	 *
	 * @param id
	 * @return
	 */
	/*@Transactional(transactionManager="transactionManager", readOnly = false,
			isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED,
			noRollbackFor = FileNotFoundException.class, rollbackFor = Exception.class, timeout = -1)*/
	public int updateByPrimaryKeyStore(Integer id) {
		int update = 0;
		try {
			//TODO 加锁，然后下面的业务代码就会按顺序排队执行
			lock.lock();

			//TODO ======以下是业务代码 =====
			Goods goods = goodsMapper.selectByPrimaryKey(id);
			System.out.println("库存：" + goods.getStore());

			//判断库存是否大于0
			if (goods.getStore() > 0) {

				//库存大于0，可以减库存，排它锁
				update = goodsMapper.updateByPrimaryKeyStore(id);

				if (update > 0) {
					System.out.println("减库存成功，可以下订单");

				} else {
					System.out.println("减库存失败，不能下订单");
					throw new RuntimeException();
				}
			} else {
				System.out.println("没有库存");
			}
			//TODO ======以上是业务代码 =====

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			//TODO 解锁，释放锁
			lock.unLock();
		}
		//返回结果
		return update;
	}
}