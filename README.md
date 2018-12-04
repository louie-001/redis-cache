# SpringBoot之redis缓存，注解方式

> spring boot对缓存支持非常灵活，我们可以使用默认的EhCache，也可以整合第三方的框架，只需配置即可。SpringBoot工程使用Redis缓存，也是非常方便（网上许多博文都是自定义RedisTemplate配置来实现，其实就是将SpringMVC的实现方式照搬到了SpringBoot中来，繁琐的同时将SpringBoot的灵活性给丧失掉了），下面代码开始。

## 一、技术栈
- spring boot
- spring data JPA
- Lombok
- H2
- Redis
- Grandle
 
## 二、搭建spring boot工程
### 1、新建工程
新建spring boot工程，jdk1.8，具体步骤不做详述，下面是build.grandle：
```
buildscript {
    ext {
        springBootVersion = '2.1.1.RELEASE'
    }
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
    }
}

apply plugin: 'java'
apply plugin: 'idea'
apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-management'

group = 'blob.louie'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = 1.8
targetCompatibility = 1.8

repositories {
    maven { url 'http://maven.aliyun.com/nexus/content/groups/public/' }
    maven { url 'http://maven.aliyun.com/nexus/content/repositories/jcenter'}
    mavenCentral()
}

dependencies {
    implementation('org.springframework.boot:spring-boot-starter-cache')
    implementation('org.springframework.boot:spring-boot-starter-data-jpa')
    implementation('org.springframework.boot:spring-boot-starter-data-redis')
    implementation('org.springframework.boot:spring-boot-starter-web')
    runtimeOnly('com.h2database:h2')
    compileOnly('org.projectlombok:lombok')
    testImplementation('org.springframework.boot:spring-boot-starter-test')
}

```

### 2、基础配置
application.yml中配置H2数据库、Redis连接信息和jpa：

```
spring:
  datasource:
    url: jdbc:h2:mem:redis-cache
    driver-class-name: org.h2.Driver
  redis:
    host: localhost
    port: 6379
  jpa:
    database: h2
    hibernate:
      ddl-auto: create
  cache:
    type: redis
```

Application.java类添加@EnableCaching注解开启缓存支持：
```java
@SpringBootApplication
@EnableCaching
public class RedisCacheApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisCacheApplication.class, args);
	}
}
```

### 3、API开发，用户信息的增、删、查功能

#### 实体类User.java:

```
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
```
@Data为Lombok插件提供的标签，需IDE支持（IDEA一定要设置Enable annotation processing），否则会报编译错误；

@Table、@Id均为JPA相关标签，对spring data JPA不了解的亲，只需理解这是实现ORM的方式即可；

> 特别说明：实体类要序列化，否则在作为接口参数时无法获取；

#### 用户服务类实现UserServiceImpl.java(此处省略UserService接口类说明)：
```java
package blob.louie.rediscache.service.impl;

import blob.louie.rediscache.dao.UserRepository;
import blob.louie.rediscache.entity.User;
import blob.louie.rediscache.service.UserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * user service implement
 * @author louie
 * @date created in 2018-12-3 23:33
 */
@Service
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;

	public UserServiceImpl (UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * save use, put redis cache
	 * @param user user data
	 * @return saved user data
	 */
	@Override
	@CachePut(value = "user", key = "#result.id", unless = "#result eq null")
	public User save(User user) {
		return userRepository.save(user);
	}

	/**
	 * find user by id,redis cacheable
	 * @param userId user id
	 * @return if exist return the user, else return null
	 */
	@Override
	@Cacheable(value = "user", key = "#userId", unless = "#result eq null")
	public User findUser(String userId) {
		return userRepository.findById(userId).orElse(null);
	}

	/**
	 * delete user by id, and remove redis cache
	 * @param userId user id
	 */
	@Override
	@CacheEvict(value = "user", key = "#userId")
	public void deleteUser(String userId) {
		userRepository.findById(userId).ifPresent(userRepository::delete);
	}
}

```

@CachePut、@Cacheable、@CacheEvict是spring缓存相关标签，其中参数及其含义不做过多说明。

#### 用户数据仓库UserRepository：
```java
package blob.louie.rediscache.dao;

import blob.louie.rediscache.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * use repository
 * @author louie
 * @date created in 2018-12-3 23:36
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
}

```
JpaRepository为JPA提供的持久层接口，实现了多种常用的数据库操作方法，也支持自定义函数，只需在UserRepository中添加自定义的函数即可；

#### 用户服务控制器UserController.java
```java
package blob.louie.rediscache.controller;

import blob.louie.rediscache.entity.User;
import blob.louie.rediscache.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * user controller
 * @author louie
 * @date created in 2018-12-3 23:25
 */
@RestController
@RequestMapping(value = "/user")
public class UserController {
	private final UserService userService;

	public UserController (UserService userService) {
		this.userService = userService;
	}

	@PostMapping
	public User saveUser(@RequestBody User user) {
		return userService.save(user);
	}

	@GetMapping(value = "/{userId}")
	public ResponseEntity<User> getUser(@PathVariable String userId) {
		User user = userService.findUser(userId);
		HttpStatus status = user == null ? HttpStatus.NOT_FOUND: HttpStatus.OK;
		return new ResponseEntity<>(user, status);
	}

	@DeleteMapping(value = "/{userId}")
	public ResponseEntity deleteUser(@PathVariable String userId) {
		userService.deleteUser(userId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

```

## 三、测试验证
执行Application.java类启动服务，使用PostMan进行接口验证：

#### 新建用户POST：
![post](https://note.youdao.com/yws/public/resource/83df1c391ac1a7fb2ec1de37ccb818e3/32FB8A35D5A3428EBE67FF67E8C96D4F)
响应信息为创建成功后的用户数据，id由uuid策略自动生成；

查看redis中的数据，可以看到以cache的value和key组合作为键值的缓存数据：
![redis put](https://note.youdao.com/yws/public/resource/83df1c391ac1a7fb2ec1de37ccb818e3/AF45C29C6F1841C9B9D2EB4450FE73E0)

#### 获取用户GET：
![get](https://note.youdao.com/yws/public/resource/83df1c391ac1a7fb2ec1de37ccb818e3/988A463CEBD043A3B4A93030ECF18692)
验证查询是否是从redis缓存中获取，只需在查询的实现处打断点，若调用查询时未进入断点则说明数据来源不是Repository，或者开启JPA的sql输出功能，查看是否有sql输出。

#### 删除用户DELETE：
![delete](https://note.youdao.com/yws/public/resource/83df1c391ac1a7fb2ec1de37ccb818e3/BAC22860672A4F1985EFBC51B511EE26)
返回状态码204，说明资源已删除；

调用查询api，返回404，资源不存在：
![404](https://note.youdao.com/yws/public/resource/83df1c391ac1a7fb2ec1de37ccb818e3/6AE2A64E7C834328A6C4862A9FD27CFF)

查看Redis数据，缓存已清除：

![empty redis](https://note.youdao.com/yws/public/resource/83df1c391ac1a7fb2ec1de37ccb818e3/F24646AC584742F9BEFF7BF2D4DF936A)

