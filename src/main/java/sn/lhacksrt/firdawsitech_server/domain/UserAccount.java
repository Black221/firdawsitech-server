package sn.lhacksrt.firdawsitech_server.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "users")
public class UserAccount {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private UUID uuid;

  @PrePersist
  void onCreate() { if (uuid == null) uuid = UUID.randomUUID(); }

  @Column(nullable = false, unique = true, length = 120)
  private String username; // email ou login

  @Column(nullable = false)
  private String password; // hashé BCrypt


  /** Optionnel : lier à Worker si nécessaire */
  private UUID workerUuid;
  private boolean enabled = true;
}
