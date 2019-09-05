package com.qupeng.service;

import java.io.FileNotFoundException;

public interface GoodsService {

	/**
	 * 减库存
	 *
	 * @param id
	 * @return
	 * @throws FileNotFoundException
	 * @throws InterruptedException
	 */
	public int updateByPrimaryKeyStore(Integer id);

}