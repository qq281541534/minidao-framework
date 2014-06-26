package org.framework.minidao.def;
/**   
 *  
 * @Description: minidao常量配置文件
 * @author LiuYu   
 * @date 2014-6-21 下午3:14:59 
 *    
 */
public class MiniDaoConstants {
	
	/**
	 * 接口方法定义规则：
	 * 添加：insert add create
	 * 修改：update modify store
	 * 删除：delete remove
	 * 检索以上个单词之外
	 */
	public static final String INF_METHOD_ACTIVE = "insert, add, create, update, modify, store, delete, remove";
	public static final String INF_METHOD_BATCH = "batch";
	
	/**
	 * 方法有且只有一个参数
	 * 用户未使用@Agruments标签
	 * 模板中引用参数默认为：dto
	 */
	public static final String SQL_FTL_DTO = "dto";
	
	
	public static final String METHOD_SAVE_BY_HIBER = "saveByHiber";
	public static final String METHOD_GET_BY_ID_HIBER = "getByIdHiber";
	public static final String METHOD_GET_BY_ENTITY_HIBER = "getByEntityHiber";
	public static final String METHOD_UPDATE_BY_HIBER = "updateByHiber";
	public static final String METHOD_DELETE_BY_HIBER = "deleteByHiber";
	public static final String METHOD_LIST_BY_HIBER = "listByHiber";
	public static final String METHOD_DELETE_BY_ID_HIBER = "deleteByIdHiber"; 
	
}


