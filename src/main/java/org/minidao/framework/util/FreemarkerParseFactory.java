package org.minidao.framework.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;


/**   
 *  
 * @Description: freemarker引擎包装类
 * @author LiuYu   
 * @date 2014-6-21 下午4:59:24 
 *    
 */
public class FreemarkerParseFactory {
	private static final Configuration tplConfig = new Configuration();
	
	public FreemarkerParseFactory(){
		//以一个Class作为一个输入参数，当你想使用ClassLoader的方式来加载模版的时候，你就可以使用这种方式，
		//这种方式将会调用来寻找模版文件，同时这种模版加载的方式要比前一种稳定一些尤其是在生产系统中。你可以很容易的把资源文件，以及图标等打包到.jar 文件中
		tplConfig.setClassForTemplateLoading(this.getClass(), "/");
//		tplConfig.setNumberFormat("0.#####################"); 
	}
	
	
	/**
	 * 解析ftl
	 * @param tplName 模板名
	 * @param encoding 编码
	 * @param paras 参数
	 * @return
	 */
	public String parseTemplate(String tplName, String encoding, Map<String ,Object> paras){
		
		StringWriter sw = new StringWriter();
		try {
			Template tpl = tplConfig.getTemplate(tplName, encoding);
			tpl.process(paras, sw);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sw.toString();
	}
	
	
	/**
	 * 解析ftl
	 * @param tplContent 模板内容
	 * @param map 参数
	 * @return 模板解析后的内容
	 */
	public String parseTemplateContent(String tplContent, Map<String, Object> map){
		StringWriter sw = new StringWriter();
		tplConfig.setTemplateLoader(new StringTemplateLoader(tplContent));
		tplConfig.setDefaultEncoding("utf-8");
		Template template;
		try {
			template = tplConfig.getTemplate("");
			template.process(map, sw);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sw.toString();
	}
	
	public String parseTemplate(String tplName, Map<String, Object> map){
		return this.parseTemplate(tplName, "utf-8", map);
	}
	
}


