package org.framework.minidao.util;

import java.math.BigDecimal;

import org.apache.commons.beanutils.Converter;

/**   
 *  
 * @Description: BigDecimal的转换类（BigDecimal是针对大小数的处理类） 
 * @author LiuYu   
 * @date 2014-6-22 上午1:20:14 
 *    
 */
public class BigDecimalConverter implements Converter {

	public Object convert(Class type, Object value) {
		if(value == null){
			return null;
		}
		if(value instanceof String){
			String tmp = (String) value;
			if(tmp.trim().length() == 0){
				return null;
			} else {
				return new BigDecimal(tmp);
			}
		}
		return null;
	}

}


