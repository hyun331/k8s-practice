package com.beyond.order_system.member.controller;

import com.beyond.order_system.common.auth.JwtTokenProvider;
import com.beyond.order_system.common.dto.CommonErrorDto;
import com.beyond.order_system.common.dto.CommonResDto;
import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.dto.*;
import com.beyond.order_system.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Qualifier("2")
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secretKeyRt}")
    String secretKeyRt;

    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider, RedisTemplate<String, Object> redisTemplate){
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }





    @PostMapping("/create") //@Valid - 이 데이터를 validation 해볼거다
    public ResponseEntity<CommonResDto> createMember(@Valid @RequestBody MemberSaveReqDto memberSaveReqDto){
        System.out.println("create\n\n\n\n");
        MemberDetResDto memberDetResDto = memberService.memberCreate(memberSaveReqDto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "member is successfully created", memberDetResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    //admin만 회원목록 전체 조회 가능
    //ROLE_안붙여도됨
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/list")
//    public ResponseEntity<CommonResDto> memberList(@PageableDefault(size = 10, sort = "createdTime", direction = Sort.Direction.DESC)Pageable pageable){
//        Page<MemberListResDto> memberListResDtos = memberService.memberList(pageable);
//        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "members are found", memberListResDtos);
//        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
//    }

    //List버정
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<CommonResDto> memberList(){
        List<MemberListResDto> memberListResDtos = memberService.memberListList();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "members are found", memberListResDtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    //본인은 본인 회원 정보만 조회 가능
    @GetMapping("/myinfo")
    public ResponseEntity<CommonResDto> memberMyInfo(){
        MemberDetResDto memberDetResDto = memberService.memberMyinfo();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "member is found", memberDetResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginDto dto){
        //email, password가 일치하는지 검증
        Member member = memberService.login(dto);

        //일치할경우 access Token 생성. =bearerToken =jwt token
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        //생성된 토큰을 CommonResDto에 담아 사용자에게 return

        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRole().toString());


        //redis에 email과 refresh token을 key:value로 저장
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 240, TimeUnit.HOURS);  //240시간. 여기서는 시간 단위로

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", refreshToken);

        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "member login is successful", loginInfo), HttpStatus.OK);
    }
    


    @PostMapping("/refresh-token")
    //at 만료시 refresh token를 body로 받아와서 다시 at 발급. access token은 헤더에 있었음
    public ResponseEntity<?> generateNewAccessToken(@RequestBody MemberRefreshDto dto){
        Claims claims = null;
        String rt = dto.getRefreshToken();
        try{
            //코드를 통해 rt 검증
            claims = Jwts.parser().setSigningKey(secretKeyRt).parseClaimsJws(rt).getBody();
        }catch (Exception e){
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, "invalid refresh token"), HttpStatus.BAD_REQUEST);
        }

        //access token 새로 발급해야함
        String email = claims.getSubject();
        String role = claims.get("role").toString();

        //redis를 조회하여 rt추가검증
        Object obj = redisTemplate.opsForValue().get(email);

        if(obj == null || !obj.toString().equals(rt)){
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, "invalid refresh token"), HttpStatus.BAD_REQUEST);

        }
        String newAt = jwtTokenProvider.createToken(email, role);
        Map<String, Object> info = new HashMap<>();
        info.put("token", newAt);

        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "at is renewed", info), HttpStatus.OK);
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody MemberResetPasswordDto dto){
        memberService.resetPassword(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "비밀번호 변경 성공", "ok");
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);

    }
    
    
}
