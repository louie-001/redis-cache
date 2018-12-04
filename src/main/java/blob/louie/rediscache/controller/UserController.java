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
