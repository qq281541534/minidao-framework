package org.minidao.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;


/**   
 *  
 * @Description: 该注解用于对class类（ElementType.TYPE）
 * @author LiuYu  
 * @date 2014-6-16 下午8:54:04 
 *    
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MiniDao {
	
}


