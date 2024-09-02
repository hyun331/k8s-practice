package com.beyond.order_system.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//우리가 사용하는 토큰의 유형이 Jwt이다
//토큰 검증 구현

@Component
@Slf4j
public class JwtAuthFilter extends GenericFilter {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        //토큰은 header에 존재
        String bearerToken = ((HttpServletRequest)request).getHeader("Authorization");
        try{
            if(bearerToken!=null){
                //토큰은 관례적으로 Bearer 로(한칸띄어쓰기) 시작하는 문구를 넣어서 요청함
                if(!bearerToken.substring(0, 7).equals("Bearer ")){
                    throw new AuthenticationServiceException("Bearer 형식이 아닙니다.");
                }
                String token = bearerToken.substring(7);
                //token 검증 및 claims 추출. claims : 사용자 정보 추출
                //token 생성시에 사용한 secret키값을 넣어 토큰 검증에 사용. application.yml에서 설정한 secret key
                Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();

                //Autentication 객체 생성. filter단계에서 토큰을 까서 검증하고 스프링엔 Authentication 객체를 주자
                //UserDetails객체도 필요 - UserDetails 인터페이스를 상속한 user 객체를 통해 검증 후 Authentication 객체를 생성

                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_"+claims.get("role")));
                UserDetails userDetails = new User(claims.getSubject(), "",authorities);

                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            //filterchain에서 그 다음 filtering으로 넘어가도록 하는 메서드
            filterChain.doFilter(request, response);
        }catch (Exception e){
            log.error(e.getMessage());
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write("token 에러");
        }
    }
}
