package sn.lhacksrt.firdawsitech_server.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sn.lhacksrt.firdawsitech_server.repository.UserAccountRepository;

import java.io.IOException;

@Component @RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwt;
  private final UserAccountRepository repo;

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String header = req.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      try {
        var decoded = jwt.verify(token);
        var username = decoded.getSubject();
        var user = repo.findByUsername(username).orElse(null);
        if (user != null && user.isEnabled()) {
          var auth = new UsernamePasswordAuthenticationToken(
              new AppUserDetails(user), null,
              new AppUserDetails(user).getAuthorities());
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      } catch (Exception ignore) { /* invalide -> non authentifié */ }
    }
    chain.doFilter(req, res);
  }
}
