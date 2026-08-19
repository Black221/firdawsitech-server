package sn.lhacksrt.firdawsitech_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.lhacksrt.firdawsitech_server.domain.UserAccount;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
  Optional<UserAccount> findByUsername(String username);
  boolean existsByUsername(String username);
}
