package org.framework.minidao.hibernate.dao.impl;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.framework.minidao.hibernate.dao.IGenericBaseCommonDao;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**   
 *  
 * @Description: DAO层泛型基类实现(使用hibernate中的sessionFactory机制)
 * @author LiuYu   
 * @date 2014-6-20 上午12:02:37 
 *    
 */
@Component
@Transactional
public class GenericBaseCommonDao<T, PK extends Serializable> implements IGenericBaseCommonDao {

	private static final Logger log = Logger.getLogger(GenericBaseCommonDao.class);
	
	/**
	 * 注入sessionFactory属性，并注入到父类HibernateDaoSupport
	 */
	private SessionFactory sessionFactory;
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * 获取session
	 */
	public Session getSession() {
		//因为Minidao接口自动扫描，导致开启事物失败，所以捕获获取session异常，如果从当前线程获取不到，就重新常见session
		try {
			return sessionFactory.getCurrentSession();
		} catch (Exception e) {
			return sessionFactory.openSession();
		}
	}

	/**
	 * 持久化对象
	 */
	public <T> void save(T entity) {
		try {
			getSession().save(entity);
			getSession().flush();
			if(log.isDebugEnabled()){
				log.debug("保存实体成功，" + entity.getClass().getName());
			}
		} catch (RuntimeException e) {
			log.error("保存实体异常,", e);
		}
	}

	/**
	 * 添加或更新
	 */
	public <T> void saveOrUpate(T entity) {
		try {
			getSession().saveOrUpdate(entity);
			getSession().flush();
			if(log.isDebugEnabled()){
				log.debug("添加或更新实体成功，" + entity.getClass().getName());
			}
		} catch (RuntimeException e) {
			log.error("添加或删除实体异常," + e);
		}
	}

	/**
	 * 删除实体
	 */
	public <T> void delete(T entity) {
		try {
			getSession().delete(entity);
			getSession().flush();
			if(log.isDebugEnabled()){
				log.debug("删除实体成功，" + entity.getClass().getName());
			}
		} catch (RuntimeException e) {
			log.error("删除实体异常," + e);
		}
	}

	/**
	 * 通过实体类型获取单个对象
	 */
	public <T> T get(T entity) {
		//通过session创建一个查询标准
		Criteria c = getSession().createCriteria(entity.getClass());
		//给标准定义实例规则，也就是对应实体类型
		c.add(Example.create(entity));
		if(c.list().size() == 0){
			return null;
		}
		return (T) c.list().get(0);
	}

	/**
	 * 根据实体类型获取对应的所有实体
	 */
	public <T> List<T> loadAll(T entity) {
		Criteria c = getSession().createCriteria(entity.getClass());
		c.add(Example.create(entity));
		return c.list();
	}

	/**
	 * 通过ID获取对象
	 */
	public <T> T get(Class<T> entityClass, Serializable id) {
		return (T) getSession().get(entityClass, id);
	}

	public <T> T findUniqueByProperty(Class<T> entityClass,
			String propertyName, Object value) {
		Assert.hasText(propertyName);
		
		return null;
	}

	/**
	 * 根据主键删除指定的实体
	 */
	public <T> void deleteEntityById(Class<T> entity, Serializable id) {
		this.getSession().delete(this.get(entity, id));
		this.getSession().flush();
	}
	
	/**
	 * 创建一个标准规则，有排序功能
	 * @param entityClass 
	 * @param isAsc
	 * @param criterions
	 * @return
	 */
	private <T> Criteria createCriteria(Class<T> entityClass, boolean isAsc, Criterion... criterions){
		Criteria c = this.createCriteria(entityClass, criterions);
		if(isAsc){
			c.addOrder(Order.asc("asc"));
		}else{
			c.addOrder(Order.desc("desc"));
		}
		return c;
	}
	
	/**
	 * 创建Criteria对象带属性比较
	 * @param entityClass
	 * @param criterions
	 * @return
	 */
	private <T> Criteria createCriteria(Class<T> entityClass, Criterion... criterions){
		Criteria c = this.getSession().createCriteria(entityClass);
		for(Criterion criterion : criterions){
			c.add(criterion);
		}
		return c;
	}

}


