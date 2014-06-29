package org.framework.minidao.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

/**   
 *  
 * @Description: 用于解析扫描包下java文件的工具类
 * @author LiuYu   
 * @date 2014-6-16 下午10:56:06 
 *    
 */
public class PackagesToScanUtil {
	
	private static final Logger log = Logger.getLogger(PackagesToScanUtil.class);
	private static final String SUB_PACKAGE_SCREEN__SUFFIX = ".*";
	private static final String SUB_PACKAGE_SCREEN__SUFFIX_RE = ".\\*";//替换使用
	
	/**
	 * 获取pack下的所有class
	 * @param pack
	 * @return
	 */
	public static Set<Class<?>> getClasses(String pack){
		
		//是否循环迭代
		boolean recursive = false;
		String[] packArr = {};
		//如果包名最后是以.*结尾的话，则继续拆分
		if(pack.lastIndexOf(SUB_PACKAGE_SCREEN__SUFFIX) != -1){
			//用".\\*"继续进行切分，这里主要是为了扫描诸如：com.minidao.framework.*.annotation.* 这种情况
			packArr = pack.split(SUB_PACKAGE_SCREEN__SUFFIX_RE);
			//如果有上述情况
			if(packArr.length > 1){
				//需匹配中间任意层包
				pack = packArr[0];
				//循环去除被截取的包中的  * 字符
				for(int i = 0; i < packArr.length; i++){
					packArr[i] = packArr[i].replace(SUB_PACKAGE_SCREEN__SUFFIX.substring(1), "");
				}
			} else {
				pack = pack.replace(SUB_PACKAGE_SCREEN__SUFFIX, "");
			}
			recursive = true;
		}
		
		//存储类的集合
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		//获取包名并替换
		String packageName = pack;
		String packageDirName = packageName.replace('.', '/');
		//定义一个枚举的集合，并处理该文件夹下的所有类
		Enumeration<URL> dirs;
		try {
			//获取所在项目资源路径下的packageDirName
			dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
			//循环迭代每一个元素
			while(dirs.hasMoreElements()){
				//获取下一个元素
				URL url = dirs.nextElement();
				//获取协议（意思就是文件的类型：file，jar等）
				String protocol = url.getProtocol();
				//如果是文件
				if("file".equals(protocol)){
					log.debug("----------file文件的扫描-------");
					//获取对应的物理路径
					String filePath = URLDecoder.decode(url.getPath(), "UTF-8");
					
					//扫描包下的所有文件，并添加到集合中
					findAndAddClassesInPackageByFile(packageName, packArr, filePath, recursive, classes);
				} else if("jar".equals(protocol)){
					findAndAddClassesInPackageByJarFile(packageName, packArr, url, packageDirName, recursive, classes);
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return classes;
	}
	
	

	/**
	 * 以文件的形势找到对应包下的所有class并添加到集合中
	 * @param packageName 包名称
	 * @param packArr 包数组（com.minidao.framework.*.annotation.* 这种情况）
	 * @param packagePath 包的物理路径
	 * @param recursive 是否可循环迭代
	 * @param classes 装载类的集合
	 */
	private static void findAndAddClassesInPackageByFile(String packageName, String[] packArr, String packagePath,final boolean recursive, Set<Class<?>> classes){
		//获取包目录
		File dir = new File(packagePath);
		//判断该目录是否存在，如果不存在或者 也不是目录就直接返回
		if(!dir.exists() || !dir.isDirectory()){
			return ;
		}
		
		//如果存在，就获取该目录下的所有文件，包括目录
		File[] dirfiles = dir.listFiles(new FileFilter() {
			//自定义过滤规则，如果该目录下还包含目录，或者是以.class结尾的文件,则会保存到dirfiles中
			public boolean accept(File pathname) {
				return ((recursive && pathname.isDirectory()) || (pathname.getName().endsWith(".class")));
			}
		});
		
		//循环所有文件
		for(File file : dirfiles){
			//如果是目录，则继续扫描
			if(file.isDirectory()){
				findAndAddClassesInPackageByFile(packageName + "." + file.getName(), packArr, file.getAbsolutePath(), recursive, classes);
			} else {
				//如果是java类文件 去掉后缀.class留下类名称
				String className = file.getName().substring(0, file.getName().length()-6);
				try {
					String classUrl = packageName + "." + className;
					//判断路径是否以.开头,是的话去掉 .
					if(classUrl.startsWith(".")){
						classUrl.replaceFirst(".", "");
					}
					
					boolean flag = true;
					if(packArr.length > 1){
						for(int i = 0; i < packArr.length; i++){
							//判断该类的路径是否包含在packArr下的所有路径
							//例如 ：【org.minidao.framework.*.annotation.*】， 该类的路径就必须是
							//org.minidao.framework下的任何路径下的annotation路径下的类
							if(classUrl.indexOf(packArr[i]) <= -1){
								flag = flag && false;
							} else {
								flag = flag && true;
							}
						}
					}
				
					if(flag){
						classes.add(Thread.currentThread().getContextClassLoader().loadClass(classUrl));
					}
				} catch (ClassNotFoundException e) {
					log.error("添加用户自定义视图类错误 找不到此类的.class文件");
					e.printStackTrace();
				}
				
			}
		}
		
	}
	
	
	/**
	 * 以JAR包的形势获取包下的所有class
	 * @param packageName  包名
	 * @param packArr  包数组（com.minidao.framework.*.annotation.* 这种情况）
	 * @param url 
	 * @param packageDirName 包名
	 * @param recursive 是否允许循环迭代
	 * @param classes 装载类的集合
	 */
	private static void findAndAddClassesInPackageByJarFile(String packageName,
			String[] packArr, URL url, String packageDirName,
			boolean recursive, Set<Class<?>> classes) {
		log.debug("--------------------jar类型扫描---------------------");
		//定义一个jar包文件
		JarFile jar;
		try {
			//获取jar文件
			jar = ((JarURLConnection)url.openConnection()).getJarFile();
			//从此jar包得到一个枚举类型
			Enumeration<JarEntry> entries = jar.entries();
			
			//循环迭代
			while(entries.hasMoreElements()){
				JarEntry entry = entries.nextElement();
				//获取元素的名称
				String name = entry.getName();
				//如果是以/开头的
				if(name.charAt(0) == '/'){
					//获取后面的字符串
					name = name.substring(1);
				}
				//如果前缀和定义的包名相同
				if(name.startsWith(packageDirName)){
					//获取最后一个 / 的下标
					int idx = name.lastIndexOf('/');
					//如果是以/结尾的，是一个包
					if(idx != -1){
						packageName = name.substring(0, idx).replace('/', '.');
					}
					//如果是一个包或者可以迭代
					if(idx != -1 || recursive){
						//如果元素名称是以.class结尾，并且元素不是文件夹
						if(name.endsWith(".class") && !entry.isDirectory()){
							//去掉.class后缀，获取class名
							String className = name.substring(packageName.length() + 1, name.length() - 6);
							try {
								//添加到classes
								
								boolean flag = true;
								if(packArr.length > 1){
									for(int i = 0; i<packArr.length; i++){
										//判断该类的路径是否包含在packArr下的所有路径
										//例如 ：【org.minidao.framework.*.annotation.*】， 该类的路径就必须是
										//org.minidao.framework下的任何路径下的annotation路径下的类
										if (packageName.indexOf(packArr[i]) <= -1) {
											flag = flag && false;
										} else {
											flag = flag && true;
										}
									}
								}
								
								if(flag){
									classes.add(Class.forName(packageName + '.' + className));
								}
							} catch (ClassNotFoundException e) {
								log.error("添加用户自定义视图类错误 找不到此类的.class文件");
								e.printStackTrace();
							}
						}
					}
				}
			}
		} catch (IOException e) {
			log.error("在扫描用户定义视图时从jar包获取文件出错");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		PackagesToScanUtil.getClasses("org.minidao.framework.*.annotation.*");
		
		String str = "org.minidao.framework.annotation.*";
		System.out.println(str.lastIndexOf("."));
//		System.out.println(str.indexOf("org.minidao.framework"));
//		
//		if(str.lastIndexOf(".*") != -1){
//			String[] ob = str.split(".\\*");
//		}
	}
}


