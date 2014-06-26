package org.framework.minidao.factory;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.framework.minidao.annotation.MiniDao;
import org.framework.minidao.util.MiniDaoUtil;
import org.framework.minidao.util.PackagesToScanUtil;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**   
 *  
 * @Description: 用于扫描、解析有MiniDao注释的类，并为其增加miniDaoHandler拦截器
 * @author LiuYu   
 * @date 2014-6-16 下午10:00:44 
 *    
 */
public class MiniDaoBeanFactory implements BeanFactoryPostProcessor{
	
	private static final Logger log = Logger.getLogger(MiniDaoBeanFactory.class);
	
	//MiniDao扫描路径
	private List<String> packagesToScan;
	
	public List<String> getPackagesToScan() {
		return packagesToScan;
	}
	public void setPackagesToScan(List<String> packagesToScan) {
		this.packagesToScan = packagesToScan;
	}

	/**
	 * 1、 在该类中循环传入的minidao配置项，解析配置项查出该配置项对应文件夹下的所有class文件并进行加载
	 * 2、 循环这些class文件类，判断是否是以MiniDao为注解方式，如果是则单独加载一个接口的代理类，
	 *    将spring容器传入改代理类，并将miniDaoHandler作为拦截器传入，将对应的类注入给spring代理工厂管理，
	 *    也就是说当这些类的方法在被调用的时候，都会去动态加载miniDaoHandler中的invoke方法。
	 */
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		log.debug("--------------MiniDaoBeanFactory----------正在加载----------");
		try {
			//循环解析传递进来的包名
			for(String pack : packagesToScan){
				//判断是否为空
				if(StringUtils.isEmpty(pack)){
					//获取该包名下的所有class
					Set<Class<?>> classSet = PackagesToScanUtil.getClasses(pack);
					
					for(Class<?> miniDaoClass : classSet){
						//如果注解为MiniDao,则单独加载一个代理类的接口
						if(miniDaoClass.isAnnotationPresent(MiniDao.class)){
							ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
							proxyFactoryBean.setBeanFactory(beanFactory);
							proxyFactoryBean.setInterfaces(new Class[]{miniDaoClass});
							proxyFactoryBean.setInterceptorNames(new String[]{ "MiniDaoHandler" });
							String beanName = MiniDaoUtil.getFirstSmall(miniDaoClass.getSimpleName());
							//如果springbean工厂中还没有包含该bean
							if(!beanFactory.containsBean(beanName)){
								log.info("MiniDao Interface [/"+miniDaoClass.getName()+"/] onto Spring Bean '"+beanName+"'");
								//则将该bean和它的代理工厂注入到bean工厂中
								beanFactory.registerSingleton(beanName, proxyFactoryBean);
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error("MiniDaoBeanFactory加载类异常！");
		}
		log.debug("------------------MiniDaoBeanFactory-----------加载完毕-------------------");
	}
	
}


