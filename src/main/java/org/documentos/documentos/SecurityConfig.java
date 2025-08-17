package org.documentos.documentos;

import org.documentos.documentos.entidades.Usuario;
import org.documentos.documentos.repositorios.UsuarioRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;


@Configuration
public class SecurityConfig {

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UsuarioRepo usuarioRepo) {
        return username -> {
            Usuario u = usuarioRepo.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            // Mapea tu enum a rol Spring (sin el prefijo ROLE_)
            String role = switch (u.getRol()) {
                case TRABAJADOR -> "TRABAJADOR";
                case SECRETARIA -> "SECRETARIA";
                case JEFA -> "JEFA";
            };

            UserDetails ud = User.withUsername(u.getEmail())
                    .password(u.getPasswordHash())
                    .roles(role)                // Spring agregará "ROLE_" internamente
                    .disabled(!u.isActivo())
                    .build();
            return ud;
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**","/js/**","/img/**","/login","/invite/**").permitAll()
                        .requestMatchers("/secretaria/**").hasRole("SECRETARIA")
                        .requestMatchers("/jefa/**").hasRole("JEFA")
                        .requestMatchers("/trabajador/**").hasRole("TRABAJADOR")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login").permitAll()
                        .defaultSuccessUrl("/home", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                );
        return http.build();
    }
}