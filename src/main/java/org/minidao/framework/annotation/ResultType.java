package org.minidao.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**   
 *  
 * @Description: 定义返回的List中的具体类型，便于返回类型的确认，如果没有或者是java.util.Map,则为java.util.Map,否则为对应实体类的全名
 * @author LiuYu   
 * @date 2014-6-16 下午9:12:11 
 *    
 */

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResultType {
	String[] value() default {};
}


