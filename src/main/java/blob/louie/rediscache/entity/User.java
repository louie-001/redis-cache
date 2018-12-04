package blob.louie.rediscache.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * user entity
 * @author louie
 * @date created in 2018-12-3 23:27
 */
@Data
@Entity
@Table(name = "b_user")
public class User implements Serializable {
	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid")
	@GeneratedValue(generator = "uuid")
	private String id;
	private String name;
	private Integer age;
	private String mobile;
	private String address;
}
