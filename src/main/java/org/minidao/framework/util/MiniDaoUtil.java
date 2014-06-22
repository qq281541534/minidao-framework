package org.minidao.framework.util;

import java.lang.reflect.Method;
import java.text.MessageFormat;

import org.apache.log4j.Logger;

import javassist.Modifier;

/**   
 *  
 * @Description: MiniDao工具类
 * @author LiuYu   
 * @date 2014-6-19 上午12:38:40 
 *    
 */
public class MiniDaoUtil {
	
	/**
	 * 数据库类型
	 */
	public static final String DATABASE_TYPE_MYSQL = "mysql";
	public static final String DATABASE_TYPE_ORACLE ="oracle";
	public static final String DATABASE_TYPE_POSTGRE = "postgresql";
	public static final String DATABASE_TYPE_SQLSERVER = "sqlserver";
	
	/**
	 * 分页sql
	 */
	public static final String MYSQL_SQL = "select *　from ({0}) sel_tab00 limit {1}, {2}";
	public static final String ORACLE_SQL = "select * from (select row_.*, rownum rownum_ from ({0}) row_ where rownum <= {1}) where rownum_ > {2}";
	public static final String POSTGRE_SQL = "select * from ({0}) sel_tab00 limit {2} offset {1}";
	public static final String SQLSERVER_SQL = "select * from ( select row_number() over(order by tempColumn) tempRowNumber, * from (select top {1} tempColumn = 0, {0}) t ) tt where tempRowNumber > {2}";
	
	private static final Logger log = Logger.getLogger(MiniDaoUtil.class);
	
	
	/**
	 * 返回首字母变为小写的字符串
	 * @param className
	 * @return
	 */
	public static String getFirstSmall(String className){
		className = className.trim();
		if(className.length() > 2){
			return className.substring(0, 1).toLowerCase() + className.substring(1);
		} else {
			return className.toLowerCase();
		}
	}
	
	/**
	 * 判断该方法是否是抽象方法
	 * @param method
	 * @return
	 */
	public static boolean isAbstract(Method method){
		//以整数形式返回此 Method 对象所表示方法的 Java 语言修饰符
		int mod = method.getModifiers();
		return Modifier.isAbstract(mod);
	}

	/**
	 * 按照数据库类型，封装sql
	 * @param dbType2
	 * @param executeSql
	 * @param page
	 * @param rows
	 * @return
	 */
	public static String createPageSql(String dbType2, String sql,
			int page, int rows) {
		int beginNum = (page - 1) * rows;
		String[] sqlParam = new String[3];
		sqlParam[0] = sql;
		sqlParam[1] = beginNum + "";
		sqlParam[2] = rows + "";
		String jdbcType = dbType2;
		if(jdbcType == null || "".equals(jdbcType)){
			throw new RuntimeException("org.minidao.framework.aop.MiniDaoHandler:(数据库类型：dbType)没有设置，请检查配置文件");
		}
		
		if(jdbcType.indexOf(DATABASE_TYPE_MYSQL) != -1){
			sql = MessageFormat.format(MYSQL_SQL, sqlParam);
		} else if(jdbcType.indexOf(DATABASE_TYPE_POSTGRE) != -1){
			sql = MessageFormat.format(POSTGRE_SQL, sqlParam);
		} else {
			int beginIndex = (page - 1) * rows;
			int endIndex = beginIndex + rows;
			sqlParam[2] = Integer.toString(beginIndex);
			sqlParam[1] = Integer.toString(endIndex);
			
			if(jdbcType.indexOf(DATABASE_TYPE_ORACLE) != -1){
				sql = MessageFormat.format(ORACLE_SQL, sqlParam);
			} else if(jdbcType.indexOf(DATABASE_TYPE_SQLSERVER) != -1){
				sqlParam[0] = sql.substring(getAfterSelectInsertPoint(sql));
				sql = MessageFormat.format(SQLSERVER_SQL, sqlParam);
			}
		}
		return sql;
	}
	
	private static int getAfterSelectInsertPoint(String sql) {
	    int selectIndex = sql.toLowerCase().indexOf("select");
	    int selectDistinctIndex = sql.toLowerCase().indexOf("select distinct");
	    return selectIndex + (selectDistinctIndex == selectIndex ? 15 : 6);
    }

	/**
	 * 判断Class是否是基本包装类
	 * @param returnType
	 * @return
	 */
	public static boolean isWrapClass(Class clz) {
		try {
			return ((Class)clz.getField("TYPE").get(null)).isPrimitive();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
	}
	
	
}


