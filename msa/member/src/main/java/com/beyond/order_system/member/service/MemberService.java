package com.beyond.order_system.member.service;

import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.dto.*;
import com.beyond.order_system.member.repository.MemberRepository;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;

    }

    @Autowired
    private PasswordEncoder passwordEncoder;
    public MemberDetResDto memberCreate(MemberSaveReqDto memberSaveReqDto){
        if(memberRepository.findByEmail(memberSaveReqDto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다.");
        }

        Member member = memberSaveReqDto.toEntity(passwordEncoder.encode(memberSaveReqDto.getPassword()));

        return memberRepository.save(member).detFromEntity();

    }


    public Page<MemberListResDto> memberList(Pageable pageable) {
        Page<Member> memberList = memberRepository.findAll(pageable);
        Page<MemberListResDto> memberListResDtos = memberList.map(a->a.listFromEntity());
        return memberListResDtos;
    }

    public List<MemberListResDto> memberListList() {
        List<Member> memberList = memberRepository.findAll();
        List<MemberListResDto> memberListResDtos = new ArrayList<>();
        for(Member m : memberList){
            memberListResDtos.add(m.listFromEntity());
        }
        return memberListResDtos;
    }

    public Member login(MemberLoginDto dto) {
        //email 존재여부 먼저 체크
        Member member = memberRepository.findByEmail(dto.getEmail()).orElseThrow(()->new EntityNotFoundException("존재하지 않는 이메일 입닌다."));

        //password 일치 여부 검증. 들어온 dto를 암호화해서 비교
        if(!passwordEncoder.matches(dto.getPassword(), member.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    public MemberDetResDto memberMyinfo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        MemberDetResDto memberDetResDto = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member not found")).detFromEntity();
        return memberDetResDto;
    }

    public void resetPassword(MemberResetPasswordDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail()).orElseThrow(()->new EntityNotFoundException("member not found. email X"));
        if(!passwordEncoder.matches(dto.getAsIsPassword(), member.getPassword())){
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
        member.resetPassword(passwordEncoder.encode(dto.getToBePassword()));
    }
}
