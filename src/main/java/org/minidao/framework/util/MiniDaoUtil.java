package org.minidao.framework.util;
/**   
 *  
 * @Description: MiniDao工具类
 * @author LiuYu   
 * @date 2014-6-19 上午12:38:40 
 *    
 */
public class MiniDaoUtil {
	
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
}


