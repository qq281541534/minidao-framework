package org.framework.minidao.util;

import org.apache.commons.beanutils.Converter;

/**   
 *  
 * @Description: Integer的转换类
 * @author LiuYu   
 * @date 2014-6-22 上午1:29:15 
 *    
 */
public class IntegerConverter implements Converter {

	public Object convert(Class type, Object value) {
		if(value == null){
			return null;
		}
		if(value instanceof String){
			String tmp = (String) value;
			if(tmp.trim().length() == 0){
				return null;
			} else {
				return new Integer(tmp);
			}
		}
		return null;
	}

}


