package sn.lhacksrt.firdawsitech_server.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import sn.lhacksrt.firdawsitech_server.domain.UserAccount;
import sn.lhacksrt.firdawsitech_server.repository.UserAccountRepository;

import java.security.SecureRandom;

@Configuration @RequiredArgsConstructor
public class BootstrapAdmin {
  private static final Logger log = LoggerFactory.getLogger(BootstrapAdmin.class);
  private static final String ADMIN_USERNAME = "admin@firdawsitech.sn";

  private final UserAccountRepository repo;
  private final PasswordEncoder encoder;

  @Value("${app.bootstrap.admin-password:}")
  private String configuredPassword;

  @Bean
  ApplicationRunner initAdmin() {
    return args -> {
      if (!repo.existsByUsername(ADMIN_USERNAME)) {
        String password = configuredPassword;
        boolean generated = password == null || password.isBlank();
        if (generated) {
          password = generateRandomPassword();
        }
        var u = UserAccount.builder()
            .username(ADMIN_USERNAME)
            .password(encoder.encode(password))
            .enabled(true)
            .build();
        repo.save(u);
        if (generated) {
          log.warn("Compte admin '{}' cree avec un mot de passe genere aleatoirement : {} " +
              "(a changer immediatement ; definir APP_BOOTSTRAP_ADMIN_PASSWORD pour choisir ce mot de passe au demarrage)",
              ADMIN_USERNAME, password);
        }
      }
    };
  }

  private String generateRandomPassword() {
    String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    SecureRandom random = new SecureRandom();
    StringBuilder sb = new StringBuilder(24);
    for (int i = 0; i < 24; i++) {
      sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
    }
    return sb.toString();
  }
}
