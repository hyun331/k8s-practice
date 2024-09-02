package com.beyond.order_system.common.config;

import com.beyond.order_system.common.auth.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//security code다
@EnableWebSecurity
//사전에 검증할지, 사후에 검증할지. 우리는 사전 검증
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfigs {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf().disable()
                .cors().and()// CORS활성화
                .httpBasic().disable()
                .authorizeRequests()
                    .antMatchers("/member/create", "/", "/member/doLogin", "/member/refresh-token", "/product/list","/member/reset-password")
                    .permitAll()
                .anyRequest().authenticated()
                .and()
                //session은 statefull, token은 stateless(서버가 정보..(사용자 정보?)를 가지고 있지 않아도 됨
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                //로그인시 사용자는 서버로부터 토큰을 발급받고
                //매 요청마다 해당 토큰을 http header에 넣어 요청
                //아래 코드는 사용자로부터 받아온 토큰이 정상인지 아닌지 검증하는 코드
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)



                .build();


    }
}
