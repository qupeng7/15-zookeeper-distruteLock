package com.qupeng.mapper;


import com.qupeng.model.Goods;

public interface GoodsMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(Goods record);

    int insertSelective(Goods record);

    Goods selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Goods record);

    int updateByPrimaryKey(Goods record);

	int updateByPrimaryKeyStore(Integer id);

    //int updateByPrimaryKeyStore(@Param("id") Integer id, @Param("version") Integer version);
}