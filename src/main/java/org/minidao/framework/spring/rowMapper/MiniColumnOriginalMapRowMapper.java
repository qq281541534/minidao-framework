package org.minidao.framework.spring.rowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.collections.map.LinkedMap;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

/**   
 *  
 * @Description: 使用默认的key作为关键字
 * @author LiuYu   
 * @date 2014-6-22 上午12:42:44 
 *    
 */
public class MiniColumnOriginalMapRowMapper implements RowMapper<Map<String, Object>> {

	
	public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Map<String, Object> mapOfColValues = new LinkedMap(columnCount);
		for(int i = 0; i <= columnCount; i++){
			String key = JdbcUtils.lookupColumnName(rsmd, i);
			Object obj = JdbcUtils.getResultSetValue(rs, i);
			mapOfColValues.put(key, obj);
		}
		return mapOfColValues;
	}

}


