package diplom.repository;


import diplom.entity.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    @Query("""
       SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
       FROM Account u
       WHERE u.email = :email
       """)
    boolean isExistsByEmail(@Param("email") String email);

    @Query("""
           SELECT u
           FROM Account u
           WHERE u.email = :email
           """)
    Optional<Account> findUserByEmail(@Param("email") String email);
}
