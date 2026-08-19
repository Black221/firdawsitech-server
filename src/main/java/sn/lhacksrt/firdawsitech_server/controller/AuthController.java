package sn.lhacksrt.firdawsitech_server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.BeanDefinitionDsl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import sn.lhacksrt.firdawsitech_server.domain.UserAccount;
import sn.lhacksrt.firdawsitech_server.dto.AuthRequest;
import sn.lhacksrt.firdawsitech_server.dto.RefreshRequest;
import sn.lhacksrt.firdawsitech_server.dto.RegisterRequest;
import sn.lhacksrt.firdawsitech_server.dto.TokenResponse;
import sn.lhacksrt.firdawsitech_server.repository.UserAccountRepository;
import sn.lhacksrt.firdawsitech_server.security.JwtService;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthenticationManager authManager;
  private final UserAccountRepository users;
  private final JwtService jwt;

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody AuthRequest req) {
    authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
    var user = users.findByUsername(req.username()).orElseThrow();
    var access = jwt.createAccess(user.getUsername());
    var refresh = jwt.createRefresh(user.getUsername());
    return ResponseEntity.ok(new TokenResponse(access, refresh, "Bearer"));
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest req) {
    var decoded = jwt.verify(req.refreshToken());
    if (!"refresh".equals(decoded.getClaim("typ").asString()))
      return ResponseEntity.badRequest().build();
    var username = decoded.getSubject();
    var user = users.findByUsername(username).orElseThrow();
    var access = jwt.createAccess(user.getUsername());
    var refresh = jwt.createRefresh(user.getUsername());
    return ResponseEntity.ok(new TokenResponse(access, refresh, "Bearer"));
  }
}
