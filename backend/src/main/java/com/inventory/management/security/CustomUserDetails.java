package com.inventory.management.security;

import com.inventory.management.domain.entity.User;
import com.inventory.management.domain.enums.RoleType;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String email;
    private final String password;
    private final RoleType role;
    private final boolean active;

    public CustomUserDetails(User user) {
        this.userId   = user.getId();
        this.email    = user.getEmail();
        this.password = user.getPassword();
        this.role     = user.getRole().getName();
        this.active   = Boolean.TRUE.equals(user.getActive());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getPassword()                 { return password; }
    @Override public String getUsername()                 { return email; }
    @Override public boolean isAccountNonExpired()        { return true; }
    @Override public boolean isAccountNonLocked()         { return true; }
    @Override public boolean isCredentialsNonExpired()    { return true; }
    @Override public boolean isEnabled()                  { return active; }
}
