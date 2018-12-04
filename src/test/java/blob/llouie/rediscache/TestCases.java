package blob.llouie.rediscache;

import blob.louie.rediscache.entity.User;
import com.alibaba.fastjson.JSON;
import org.junit.Test;

/**
 *
 * @author louie
 * @date created in 2018-12-4 0:32
 */
public class TestCases {

	@Test
	public void userJson() {
		User user = new User();
		user.setName("jack");
		user.setAge(28);
		user.setMobile("12345678");

		System.out.println(JSON.toJSONString(user));
	}
}
