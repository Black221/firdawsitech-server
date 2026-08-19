package sn.lhacksrt.firdawsitech_server.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
  private final Algorithm algo;
  private final long accessTtlSeconds;
  private final long refreshTtlSeconds;
  private final String issuer;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.issuer:firdawsitech}") String issuer,
      @Value("${app.jwt.access-ttl-seconds:3600}") long accessTtlSeconds,
      @Value("${app.jwt.refresh-ttl-seconds:2592000}") long refreshTtlSeconds) {
    this.algo = Algorithm.HMAC256(secret);
    this.issuer = issuer;
    this.accessTtlSeconds = accessTtlSeconds;
    this.refreshTtlSeconds = refreshTtlSeconds;
  }

  public String createAccess(String sub) {
    Instant now = Instant.now();
    return JWT.create()
      .withIssuer(issuer)
      .withSubject(sub)
      .withIssuedAt(Date.from(now))
      .withExpiresAt(Date.from(now.plusSeconds(accessTtlSeconds)))
      .sign(algo);
  }

  public String createRefresh(String sub) {
    Instant now = Instant.now();
    return JWT.create()
      .withIssuer(issuer)
      .withSubject(sub)
      .withClaim("typ", "refresh")
      .withIssuedAt(Date.from(now))
      .withExpiresAt(Date.from(now.plusSeconds(refreshTtlSeconds)))
      .sign(algo);
  }

  public com.auth0.jwt.interfaces.DecodedJWT verify(String token) {
    return JWT.require(algo).withIssuer(issuer).build().verify(token);
  }
}
