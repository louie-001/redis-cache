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
