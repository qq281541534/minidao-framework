package org.minidao.framework.util;

import java.text.ParseException;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.time.DateUtils;

/**   
 *  
 * @Description: 时间日期转换类
 * @author LiuYu   
 * @date 2014-6-22 上午1:09:31 
 *    
 */
public class DateConverter implements Converter {

	String[] parsePatterns = new String[] { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.S" };
	
	public Object convert(Class type, Object value) {
		if(value == null){
			return null;
		}
		if(value instanceof String){
			String tmp = (String) value;
			if(tmp.trim().length() == 0){
				return null;
			} else {
				try {
					return DateUtils.parseDate(tmp, parsePatterns);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		} else {
			throw new ConversionException("not String");
		}
		return value;
	}

}


