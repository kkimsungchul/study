//package com.sungchul.stock.config.security;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
//
//@Configuration
//@EnableWebSecurity
//@Slf4j
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//    @Autowired
//    private UserDetailsService userDetailsService;
//
//    @Override
//    public void configure(AuthenticationManagerBuilder auth) throws Exception {
//
////        String password = passwordEncoder().encode("1111");
////        auth.inMemoryAuthentication().withUser("user").password(password).roles("USER");
////        auth.inMemoryAuthentication().withUser("manager").password(password).roles("MANAGER");
////        auth.inMemoryAuthentication().withUser("admin").password(password).roles("ADMIN");
//
//        auth.userDetailsService(userDetailsService);
//
//    }
//
//    @Bean
//    // BCryptPasswordEncoder는 Spring Security에서 제공하는 비밀번호 암호화 객체입니다.
//    // Service에서 비밀번호를 암호화할 수 있도록 Bean으로 등록합니다.
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .csrf().disable()
//                .authorizeRequests()
//                //.antMatchers("/").permitAll()
//                //스웨거에는 바로 접속할수 있도록 아래의 URL에서 예외처리
//                .antMatchers("/", "/v2/api-docs", "/configuration/**", "/swagger*/**", "/webjars/**").permitAll()
//
//                .antMatchers("/user").hasRole("USER")
//                .antMatchers("/manager").hasRole("MANAGER")
//                .antMatchers("/admin" , "/test" , "/test/**","/user" , "/user/**").hasRole("ADMIN")
//                .anyRequest().authenticated()
//                .and()
//                .formLogin();
//
//        http.csrf()
//                .requireCsrfProtectionMatcher(new CsrfRequireMatcher())
//                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
//    }
//
//
//}