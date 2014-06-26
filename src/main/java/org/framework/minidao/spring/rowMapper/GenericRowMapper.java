package org.framework.minidao.spring.rowMapper;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.framework.minidao.util.BigDecimalConverter;
import org.framework.minidao.util.CamelCaseUtils;
import org.framework.minidao.util.DateConverter;
import org.framework.minidao.util.IntegerConverter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

/**   
 *  
 * @Description: 
 * @author LiuYu   
 * @date 2014-6-22 上午1:01:24 
 *    
 */
public class GenericRowMapper<T> implements RowMapper<T> {
	
	private Class<T> clazz;
	
	public GenericRowMapper(Class<T> clz){
		this.clazz = clz;
	}

	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		try {
			T bean = clazz.newInstance();
			//org.apache.commons.beanutils.ConvertUtils这个类的使用介绍，这个工具类的职能是在字符串和指定类型的实例之间进行转换
			ConvertUtils.register(new DateConverter(), Date.class);
			ConvertUtils.register(new BigDecimalConverter(), BigDecimal.class);
			ConvertUtils.register(new IntegerConverter(), Integer.class);
			
			for(int i = 0; i <= columnCount; i++){
				String key = JdbcUtils.lookupColumnName(rsmd, i);
				Object obj = JdbcUtils.getResultSetValue(rs, i);
				String cameKey = CamelCaseUtils.toCamelCase(key);
				BeanUtils.setProperty(bean, cameKey, obj);
			}
			return bean;
			
		} catch (Exception e) {
			throw new SQLException("mapRow error.", e);
		} 
	}

}


