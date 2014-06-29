package org.framework.minidao.datasource;

import java.util.Map;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;

/**   
 *  
 * @Description: 动态数据源
 * @author LiuYu   
 * @date 2014-6-28 下午4:43:54 
 *    
 */
public class DynamicDataSource extends AbstractRoutingDataSource{

	/**
	 * 该方法继承了父类的抽象方法，用于根据数据库标示符取得当前的数据库
	 */
	@Override
	protected Object determineCurrentLookupKey() {
		DataSourceType dataSourceType = DataSourceContextHolder.getDataSourceType();
		return dataSourceType;
	}
	
	@Override
	public void setDataSourceLookup(DataSourceLookup dataSourceLookup){
		super.setDataSourceLookup(dataSourceLookup);
	}
	
	@Override
	public void setDefaultTargetDataSource(Object defaultTargetDataSource){
		super.setDefaultTargetDataSource(defaultTargetDataSource);
	}
	
	@Override
	public void setTargetDataSources(Map targetDataSources) {
		super.setTargetDataSources(targetDataSources);
	}
}


