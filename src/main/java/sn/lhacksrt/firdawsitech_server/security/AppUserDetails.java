package sn.lhacksrt.firdawsitech_server.security;

import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sn.lhacksrt.firdawsitech_server.domain.UserAccount;

import java.util.List;

public record AppUserDetails(UserAccount u) implements UserDetails {
  @Override public List<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }
  @Override public String getPassword() { return u.getPassword(); }
  @Override public String getUsername() { return u.getUsername(); }
  @Override public boolean isAccountNonExpired() { return true; }
  @Override public boolean isAccountNonLocked() { return true; }
  @Override public boolean isCredentialsNonExpired() { return true; }
  @Override public boolean isEnabled() { return u.isEnabled(); }
}
