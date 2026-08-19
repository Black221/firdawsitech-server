package sn.lhacksrt.firdawsitech_server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import sn.lhacksrt.firdawsitech_server.domain.UserAccount;
import sn.lhacksrt.firdawsitech_server.repository.UserAccountRepository;

@Configuration @RequiredArgsConstructor
public class BootstrapAdmin {
  private final UserAccountRepository repo;
  private final PasswordEncoder encoder;

  @Bean
  ApplicationRunner initAdmin() {
    return args -> {
      if (!repo.existsByUsername("admin@firdawsitech.sn")) {
        var u = UserAccount.builder()
            .username("admin@firdawsitech.sn")
            .password(encoder.encode("firdawsitech")) // change en prod !
            .enabled(true)
            .build();
        repo.save(u);
      }
    };
  }
}
