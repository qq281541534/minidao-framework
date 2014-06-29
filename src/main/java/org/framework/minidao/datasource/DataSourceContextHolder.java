package org.framework.minidao.datasource;
/**   
 *  
 * @Description: 获得和设置上下文环境的类，主要负责改变上下文数据源的名称
 * @author LiuYu   
 * @date 2014-6-28 下午4:52:39 
 *    
 */
public class DataSourceContextHolder {
	
	
	/**
	 * ThreadLocal 不是用来解决共享对象的多线程访问问题的，一般情况下，通过ThreadLocal.set() 到线程中的对象是该线程自己使用的对象，
	 * 其他线程是不需要访问的，也访问不到的.各个线程中访问的是不同的对象。
	 * 说ThreadLocal使得各线程能够保持各自独立的一个对象，并不是通过ThreadLocal.set()来实现的，
	 * 而是通过每个线程中的new 对象 的操作来创建的对象，每个线程创建一个，不是什么对象的拷贝或副本。
	 * 
	 */
	private static final ThreadLocal contextHolder = new ThreadLocal();
	
	public static void setDataSourceType(DataSourceType dataSourceType){
		contextHolder.set(dataSourceType);
	}
	
	public static DataSourceType getDataSourceType(){
		return (DataSourceType) contextHolder.get();
	}
	
	
	public static void clearDataSourceType(){
		contextHolder.remove();
	}
}


