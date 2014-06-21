package org.minidao.framework.aop;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**   
 *  
 * @Description: MiniDao拦截器
 * @author LiuYu   
 * @date 2014-6-16 下午9:57:08 
 *    
 */
public class MiniDaoHandler implements MethodInterceptor {

	private static final Logger log = Logger.getLogger(MiniDaoHandler.class);
	
	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
//	private 
	
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		Method method = invocation.getMethod();
		
		
		return null;
	}

}


