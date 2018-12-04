package blob.louie.rediscache.service;

import blob.louie.rediscache.entity.User;

import java.util.Optional;

/**
 * user service
 * @author louie
 * @date created in 2018-12-3 23:26
 */
public interface UserService {
	/**
	 * save user
	 * @param user user data
	 * @return saved user
	 */
	User save(User user);

	/**
	 * find user by id
	 * @param userId user id
	 * @return user
	 */
	User findUser(String userId);

	/**
	 * delete user by id
	 * @param userId user id
	 */
	void deleteUser(String userId);
}
