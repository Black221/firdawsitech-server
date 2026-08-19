package sn.lhacksrt.firdawsitech_server.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import sn.lhacksrt.firdawsitech_server.repository.UserAccountRepository;

@Service @RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
  private final UserAccountRepository repo;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var u = repo.findByUsername(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return new AppUserDetails(u);
  }
}
