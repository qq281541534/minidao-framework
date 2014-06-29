package org.framework.minidao.aop;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ognl.Ognl;
import ognl.OgnlException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.framework.minidao.annotation.Arguments;
import org.framework.minidao.annotation.ResultType;
import org.framework.minidao.annotation.Sql;
import org.framework.minidao.def.MiniDaoConstants;
import org.framework.minidao.hibernate.dao.IGenericBaseCommonDao;
import org.framework.minidao.pojo.MiniDaoPage;
import org.framework.minidao.spring.rowMapper.GenericRowMapper;
import org.framework.minidao.spring.rowMapper.MiniColumnMapRowMapper;
import org.framework.minidao.spring.rowMapper.MiniColumnOriginalMapRowMapper;
import org.framework.minidao.util.FreemarkerParseFactory;
import org.framework.minidao.util.MiniDaoUtil;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;

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
	private IGenericBaseCommonDao miniDaoHiberCommonDao;
	
	private BasicFormatterImpl formatter = new BasicFormatterImpl();
	
	private String UPPER_KEY = "upper";
	private String LOWER_KEY = "lower";
	
	private String keyType = "origin";
	private boolean formatSql = false;
	private boolean showSql = false;
	private String dbType;
	
	public Object invoke(MethodInvocation invocation) throws Throwable {
		//获取方法
		Method method = invocation.getMethod();
		//获取方法中的参数
		Object[] args = invocation.getArguments();
		//分页参数
		MiniDaoPage miniDaoPage = new MiniDaoPage();
		//Sql模板
		String templateSql = null;
		//返回结果
		Object returnObj = null;
		
		//判断是都是抽象方法，如果不是抽象方法，则不执行minidao拦截器
		if(!MiniDaoUtil.isAbstract(method)){
			return invocation.proceed();
		}
		
		//判断是否是hibernate实体维护方法，如果是执行hibernate方式实体维护
		Map<String, Object> map = new HashMap<String, Object>();
		if(MiniDaoHiber(map, method, args)){
			return map.get("returnObj");
		}
		
		//装载SQL模板所需参数
		templateSql = installDaoMetaData(miniDaoPage, method, map, args);
		
		//解析SQL模板，返回可执行的SQL语句
		String executeSql = parseSqlTemplate(method, templateSql, map);
		
		//组装sql占位符参数
		Map<String, Object> sqlMap = instanllPlaceholderSqlParam(executeSql, map);
		
		//获取SQL执行的返回值
		returnObj = getReturnMinidaoResult(dbType, miniDaoPage, jdbcTemplate, method, executeSql, sqlMap);
		
		if(showSql){
			log.info("MiniDao-SQL:\n\n"+(formatSql == true?formatter.format(executeSql):executeSql)+"\n");
		}
		
		return returnObj;
	}
	
	/**
	 * 获取MiniDao处理结果集
	 * 调用springjdbc引擎，执行SQL返回值
	 * 注意：1、检查是否是可执行方法（insert, add, create, update, modify, store, delete, remove），
	 * 		    如果是则判断是否有参数传入，分别用不同的jdbc方式进行执行（namedParameterJdbcTemplate是jdbc的包装类，允许传参）
	 * 	   2、检查是否是批处理语句，如果是批处理语句则以;符号进行切割执行，并将结果再以;符号拼接好返回
	 * 	   3、如果以上两条都不满足，则是查询操作；查询操作需要判断返回值的类型（基本类型，List，Map,基本包装类型，String,对象类型）：
	 * 		  a、如果是基本类型，则将执行SQL后对应的结果转换成int,long,double,这三种常用的类型
	 * 		  b、如果是List类型，则需要注意是否有分页参数，然后判断是否使用了@ResultType注解，并且注解中是否指定了类型，
	 * 			如果注解中有制定了参数类型，则用过反射拿到该类型，然后再进行封装处理
	 * 		  c、如果是Map类型，基本包装类型，String，对象类型，基本操作相同
	 * @param dbType2
	 * @param miniDaoPage
	 * @param jdbcTemplate2
	 * @param method
	 * @param executeSql
	 * @param sqlMap
	 * @return
	 */
	private Object getReturnMinidaoResult(String dbType2, MiniDaoPage miniDaoPage, 
			JdbcTemplate jdbcTemplate2, Method method, String executeSql, Map<String, Object> sqlMap) {
		
		String methodName = method.getName();
		//判断是否是可执行方法
		if(checkActiveKey(methodName)){
			//如果有传参数
			if(sqlMap != null){
				return namedParameterJdbcTemplate.update(executeSql, sqlMap);
			}else{
				return jdbcTemplate2.update(executeSql);
			}
		} else if(checkBatchKey(methodName)) {
			return batchUpdate(jdbcTemplate2, executeSql);
		} 
		//如果是查询操作
		else {
			Class<?> returnType = method.getReturnType();
			
			//判断returnType对象是否是一个基本类型（boolean、byte、char、short、int、long、float、double和void）
			if(returnType.isPrimitive()){
				Number number = jdbcTemplate2.queryForObject(executeSql, BigDecimal.class);
				if("int".equals(returnType.toString())){
					return number.intValue();
				} else if("long".equals(returnType.toString())){
					return number.longValue();
				} else if("double".equals(returnType.toString())){
					return number.doubleValue();
				}
			} 
			//判断returnType对象所表示的类或接口是否是List,或者是否是其超类或者超接口
			else if(returnType.isAssignableFrom(List.class)){
				int page = miniDaoPage.getPage();
				int rows = miniDaoPage.getRows();
				if(page != 0 && rows != 0){
					executeSql = MiniDaoUtil.createPageSql(dbType2, executeSql, page, rows);
				}
				//支持返回MAP和实体list
				ResultType resultType = method.getAnnotation(ResultType.class);
				String[] values = null;
				if(resultType != null){
					//获取@ResultType注解的 参数
					values = resultType.value();
				}
				//判断@ResultType注解有没有【注解标签】参数
				if(values == null || values.length == 0 || "java.util.Map".equals(values[0])){
					if(sqlMap != null){
						return namedParameterJdbcTemplate.query(executeSql, sqlMap, getColumnMapRowMapper());
					} else {
						return jdbcTemplate2.query(executeSql, getColumnMapRowMapper());
					}
				} else {
					//如果@ResultType有注解参数，获得注解参数，并反射该类
					Class<?> clz = null;
					try {
						clz = Class.forName(values[0]);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					if(sqlMap != null){
						return namedParameterJdbcTemplate.query(executeSql, sqlMap, new GenericRowMapper(clz));
					} else {
						return jdbcTemplate2.query(executeSql, new GenericRowMapper(clz));
					}
				}
			}
			//判断returnType对象所表示的类或接口是否是Map,或者是否是其超类或者超接口
			else if(returnType.isAssignableFrom(Map.class)){
				//Map类型
				if(sqlMap != null){
					return (Map)namedParameterJdbcTemplate.queryForObject(executeSql, sqlMap,getColumnMapRowMapper());
				}else{
					return (Map)jdbcTemplate.queryForObject(executeSql,getColumnMapRowMapper());
				}
			}
			///判断returnType对象所表示的类或接口是否是String,或者是否是其超类或者超接口
			else if(returnType.isAssignableFrom(String.class)){
				try{  
					if(sqlMap != null){
						return namedParameterJdbcTemplate.queryForObject(executeSql, sqlMap, String.class);
					}else{
						return jdbcTemplate.queryForObject(executeSql,String.class);
					}
		        }catch (EmptyResultDataAccessException e) {  
		            return null;  
		        }
			}
			//基本类型的包装类
			else if(MiniDaoUtil.isWrapClass(returnType)){
				try{  
					if(sqlMap != null){
						return namedParameterJdbcTemplate.queryForObject(executeSql, sqlMap, returnType);
					}else{
						return jdbcTemplate.queryForObject(executeSql, returnType);
					}
		        }catch (EmptyResultDataAccessException e) {  
		            return null;  
		        }
			}
			//对象类型
			else{
				RowMapper<?> rm = ParameterizedBeanPropertyRowMapper.newInstance(returnType);
				try{  
					if(sqlMap != null){
						return namedParameterJdbcTemplate.queryForObject(executeSql, sqlMap, rm);
					}else{
						return jdbcTemplate.queryForObject(executeSql, rm);
					}
		        }catch (EmptyResultDataAccessException e) {  
		            return null;  
		        }
			}
		}
		return null;
	}
	
	/**
	 * 根据参数设置map的key大小写
	 * @return
	 */
	private RowMapper<Map<String, Object>> getColumnMapRowMapper(){
		if(getKeyType().equalsIgnoreCase(LOWER_KEY)){
			return new MiniColumnMapRowMapper();
		} else if(getKeyType().equalsIgnoreCase(UPPER_KEY)){
			return new ColumnMapRowMapper();
		} else {
			return new MiniColumnOriginalMapRowMapper();
		}
	}
	
	/**
	 * 批处理
	 * @param jdbcTemplate2
	 * @param executeSql
	 * @return
	 */
	private Object batchUpdate(JdbcTemplate jdbcTemplate2, String executeSql) {
		String[] sqls = executeSql.split(";");
		if(sqls.length < 100){
			return jdbcTemplate2.batchUpdate(sqls);
		}
		
		int[] result = new int[sqls.length];
		List<String> sqlList = new ArrayList<String>();
		for(int i = 0; i < sqls.length; i++){
			sqlList.add(sqls[i]);
			if(i % 100 == 0){
				addResultArray(result, i + 1, jdbcTemplate2.batchUpdate(sqlList.toArray(new String[0])));
				sqlList.clear();
			}
		}
		addResultArray(result, sqls.length, jdbcTemplate2.batchUpdate(sqlList.toArray(new String[0])));
		return result;
	}
	
	/**
	 * 把批量处理的结果拼接起来
	 * @param result
	 * @param index
	 * @param arr
	 */
	private void addResultArray(int[] result, int index, int[] arr){
		int length = arr.length;
		for(int i = 0; i < length; i++){
			result[index - length + i] = arr[i];
		}
	}

	/**
	 * 判断是否是批处理
	 * @param methodName
	 * @return
	 */
	private boolean checkBatchKey(String methodName) {
		String keys[] = MiniDaoConstants.INF_METHOD_BATCH.split(",");
		for(String key : keys){
			if(methodName.contains(key)){
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断是否是可执行的方法
	 * @param methodName
	 * @return
	 */
	private static boolean checkActiveKey(String methodName){
		String[] keys = MiniDaoConstants.INF_METHOD_ACTIVE.split(",");
		for(String key : keys){
			if(methodName.contains(key)){
				return true;
			}
		}
		return false;
	}

	/**
	 * 组装占位符参数
	 * 将方法传入的参数与sql中：对应的字段匹配 以键值对的方式存入到Map中返回
	 * @param executeSql
	 * @param sqlParamsMap
	 * @return
	 * @throws OgnlException
	 */
	private Map<String, Object> instanllPlaceholderSqlParam(String executeSql, Map<String, Object> sqlParamsMap) throws OgnlException {
		Map<String, Object> map = new HashMap<String, Object>();
		//表示以：开头，[0-9或者.或者A-Z大小写]的任意字符，超过一个
		String regEx = ":[ tnx0Bfr]*[0-9a-z.A-Z]+";
		Pattern pattern = Pattern.compile(regEx);
		Matcher m = pattern.matcher(executeSql);
		while(m.find()){
			log.debug(" Match [" + m.group() +"] at positions " + m.start() + "-" + (m.end() - 1));
			String ognl_key = m.group().replace(":", "").trim();
			map.put(ognl_key, Ognl.getValue(ognl_key, sqlParamsMap));
		}
		return map;
	}

	/**
	 * 解析SQL模板
	 * 注意：1、如果templateSql模板不为空的情况，直接解析，如果为空则看第2条
	 * 	   2、根据命名规范【接口名_方法名.sql】,获取sql模板文件的路径
	 * @param method
	 * @param templateSql
	 * @param map
	 * @return
	 */
	private String parseSqlTemplate(Method method, String templateSql,
			Map<String, Object> map) {
		
		String executeSql = null;
		
		//如果templateSQL不为空，则用freemark模板引擎解析
		if(StringUtils.isNotEmpty(templateSql)){
			executeSql = new FreemarkerParseFactory().parseTemplateContent(templateSql, map);
		}else{
			//扫描规则--首先扫描同位置sql目录,如果没找到文件再搜索dao目录
			//扫描相同目录下的sql文件夹下的【接口名_方法名.sql】文件
			String sqlTemplatePath = "/" + method.getDeclaringClass().getName().replace(".", "/")
										.replace("/dao/", "/sql/") + "_" + method.getName() + ".sql";
			URL sqlfileURL = this.getClass().getClassLoader().getResource(sqlTemplatePath);
			//如果sql文件下的路径没有加载成功，则不替换dao路径为sql路径
			if(sqlfileURL == null){
				sqlTemplatePath = "/"+method.getDeclaringClass().getName().replace(".", "/")+"_"+method.getName()+".sql";
			}
			log.debug("Minidao - SQL - path" + sqlTemplatePath);
			executeSql = new FreemarkerParseFactory().parseTemplate(sqlTemplatePath, map);
		}
		
		
		return getSqlText(executeSql);
	}
	
	
	/**
	 * 去除无效字符，不然批量处理可能报错
	 * @param executeSql
	 * @return
	 */
	private String getSqlText(String executeSql) {
		return executeSql.replaceAll("\\n", " ").replaceAll("\\t", " ").replaceAll("\\s{1,}", " ").trim();
	}

	/**
	 * 装载所需数据模板
	 * 注意：1、如果使用的Arguments自定义的标签的话，这里只是把参数封装进了map中，返回值是空
	 * 	   2、如果使用的SQL自定义标签的话，这里是直接当SQL语句返回
	 * @param miniDaoPage
	 * @param method
	 * @param map
	 * @param args
	 * @return
	 * @throws Exception 
	 */
	private String installDaoMetaData(MiniDaoPage miniDaoPage, Method method,
			Map<String, Object> map, Object[] args) throws Exception {
		
		String templateSql = null;
		//如果方法参数大于1个的话，方法必须使用注释标签Arguments
		boolean arguments_flag = method.isAnnotationPresent(Arguments.class);
		if(arguments_flag){
			//获取所有参数
			Arguments arguments = method.getAnnotation(Arguments.class);
			log.debug("@Arguments -------------------" + Arrays.toString(arguments.value()));
			if(arguments.value().length > args.length){
				throw new Exception("[注释标签]参数数目，不能大于[方法参数]参数数目");
			}
			//循环注解中的参数，
			int args_num = 0;
			for(String v : arguments.value()){
				//匹配[注释标签]是否有miniDaoPage分页的参数，如果有，则从对应下标位置的[方法参数]中获取
				if("page".equalsIgnoreCase(v)){
					miniDaoPage.setPage(Integer.parseInt(args[args_num].toString()));
				}
				if("rows".equalsIgnoreCase(v)){
					miniDaoPage.setRows(Integer.parseInt(args[args_num].toString()));
				}
				map.put(v, args[args_num]);
				args_num ++;
			}
		} else {
			//如果未使用【参数标签】
			if(args.length > 1){
				throw new Exception("[方法参数]数目 >= 2, 方法必须使用注释标签@Arguments");
			} else if(args.length == 1){
				//如果【方法参数】只有一个的话，封装key为dto
				map.put(MiniDaoConstants.SQL_FTL_DTO, args[0]);
			}
		}
		
		//判断是否使用了SQL注解
		if(method.isAnnotationPresent(Sql.class)){
			Sql sql = method.getAnnotation(Sql.class);
			//如果自定义标签SQL中的值不为空
			if(StringUtils.isNotEmpty(sql.value())){
				templateSql = sql.value();
			}
			log.debug("@Sql---------------------" + sql.value());
		}
		return templateSql;
	}

	/**
	 * 使用hibernate实体维护
	 * 说明：向下兼容Hibernate实体维护方式,实体的增删改查SQL自动生成,不需要写SQL
	 * @param map
	 * @param method
	 * @param args
	 * @return
	 */
	private boolean MiniDaoHiber(Map<String, Object> map, Method method,
			Object[] args) {
		
		//判断如果是持久化对象，则调用Hibernate进行持久化维护
		if(MiniDaoConstants.METHOD_SAVE_BY_HIBER.equals(method.getName())){
			miniDaoHiberCommonDao.save(args[0]);
			return true;
		}
		if(MiniDaoConstants.METHOD_UPDATE_BY_HIBER.equals(method.getName())){
			miniDaoHiberCommonDao.saveOrUpate(args[0]);
			return true;
		}
		if(MiniDaoConstants.METHOD_GET_BY_ID_HIBER.equals(method.getName())){
			//args[0]实体类型，arg1实体对应主键
			Class<?> clz = (Class<?>) args[0];
			map.put("returnObj", miniDaoHiberCommonDao.get(clz, args[1].toString()));
			return true;
		}
		if(MiniDaoConstants.METHOD_GET_BY_ENTITY_HIBER.equals(method.getName())){
			map.put("returnObj", miniDaoHiberCommonDao.get(args[0]));
			return true;
		}
		if(MiniDaoConstants.METHOD_DELETE_BY_HIBER.equals(method.getName())){
			miniDaoHiberCommonDao.delete(args[0]);
			return true;
		}
		if(MiniDaoConstants.METHOD_DELETE_BY_ID_HIBER.equals(method.getName())){
			Class<?> clz = (Class<?>) args[0];
			miniDaoHiberCommonDao.deleteEntityById(clz, args[1].toString());
			return true;
		}
		if(MiniDaoConstants.METHOD_LIST_BY_HIBER.equals(method.getName())){
			map.put("returnObj", miniDaoHiberCommonDao.loadAll(args[0]));
			return true;
		}
		return false;
	}
	
	
	

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return namedParameterJdbcTemplate;
	}

	public void setNamedParameterJdbcTemplate(
			NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public IGenericBaseCommonDao getMiniDaoHiberCommonDao() {
		return miniDaoHiberCommonDao;
	}

	public void setMiniDaoHiberCommonDao(IGenericBaseCommonDao miniDaoHiberCommonDao) {
		this.miniDaoHiberCommonDao = miniDaoHiberCommonDao;
	}

	public String getKeyType() {
		return keyType;
	}

	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}

	public boolean isFormatSql() {
		return formatSql;
	}

	public void setFormatSql(boolean formatSql) {
		this.formatSql = formatSql;
	}

	public boolean isShowSql() {
		return showSql;
	}

	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	
	
}


