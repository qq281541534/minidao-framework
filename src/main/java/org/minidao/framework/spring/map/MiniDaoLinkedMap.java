package org.minidao.framework.spring.map;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;


/**   
 *  
 * @Description: 提供默认小写作为key的Map
 * @author LiuYu   
 * @date 2014-6-21 下午11:59:49 
 *    
 */
public class MiniDaoLinkedMap extends LinkedHashMap<String, Object> {

	private static final long serialVersionUID = 1L;
	
	//Locale确定了一种专门的语言和区域。通过使用java.util.Locale对象来为那些区域敏感型的对象定制格式化数据以及向用户的展示。
	//Locale影响到用户界面的语言,情形映射,整理(排序),日期和时间的格式以及货币格式
	private final Locale locale;
	
	public MiniDaoLinkedMap(){
		this(((Locale)(null)));
	}

	public MiniDaoLinkedMap(Locale locale) {
		//Locale.getDefault()获得此java虚拟机实例的当前默认语言环境
		this.locale = locale == null ? Locale.getDefault() : locale;
	}
	
	public MiniDaoLinkedMap(int initialCapacity){
		this(initialCapacity, null);
	}

	public MiniDaoLinkedMap(int initialCapacity, Locale locale) {
		super(initialCapacity);
		this.locale = locale == null ? Locale.getDefault() : locale;
	}
	
	public Object put(String key, Object value){
		return super.put(convertKey(key), value);
	}

	private String convertKey(String key) {
		return key.toLowerCase(locale);
	}
	
	public void putAll(Map map){
		if(map.isEmpty()){
			return;
		}
		java.util.Map.Entry entry;
		for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); 
				put(convertKey((String)entry.getKey()), entry.getValue())){
			entry = (java.util.Map.Entry) iterator.next();
		}
	}
	
	public boolean containsKey(Object key){
		return (key instanceof String) && super.containsKey(this.convertKey((String)key));
	}
	
	public Object get(Object key) {
		if (key instanceof String)
			return super.get(convertKey((String) key));
		else
			return null;
	}

	public Object remove(Object key) {
		if (key instanceof String)
			return super.remove(convertKey((String) key));
		else
			return null;
	}

	public void clear() {
		super.clear();
	}
	
}	


