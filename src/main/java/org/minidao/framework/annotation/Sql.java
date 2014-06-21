package org.minidao.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**   
 *  
 * @Description: 用于映射方法对应的SQL注解
 * @author LiuYu   
 * @date 2014-6-16 下午9:05:12 
 *    
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Sql {
	String value();
	String dbms() default "";
}


