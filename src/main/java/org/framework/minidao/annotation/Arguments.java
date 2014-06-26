package org.framework.minidao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**   
 *  规则：
 *  1、[注释标签参数]必须和[方法参数]顺序保持一致
 *  2、[注释标签参数]的参数数目不能大于[方法参数]的参数数目
 *  3、只有在[注释标签参数]标注的参数，才会传入模板中
 *  4、如果[方法参数]中有一个，如果用户不设置[注释标签参数]，则默认参数名为miniDao
 *  
 * @Description: 用于对方法的注解（ElementType.METHOD）默认属性可以为String数组
 * @author LiuYu   
 * @date 2014-6-16 下午9:02:02 
 *    
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Arguments {
	String[] value() default {};
}


