package org.minidao.framework.hibernate;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

/**   
 *  
 * @Description: 
 * 		1、@MappedSuperclass注解只能标准在类上：@Target({java.lang.annotation.ElementType.TYPE})
 * 		2、标注为@MappedSuperclass的类将不是一个完整的实体类，他将不会映射到数据库表，但是他的属性都将映射到其子类的数据库字段中。
 * 		3、标注为@MappedSuperclass的类不能再标注@Entity或@Table注解，也无需实现序列化接口。
 * @author LiuYu   
 * @date 2014-6-20 上午12:54:25 
 *    
 */
@MappedSuperclass
public abstract class IdEntity {
	private String id;

	@Id
	@GeneratedValue(generator = "hibernate-uuid")
	@GenericGenerator(name = "hibernate-uuid", strategy = "uuid")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	
}


