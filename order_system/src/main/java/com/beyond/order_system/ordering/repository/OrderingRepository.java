package com.beyond.order_system.ordering.repository;

import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.ordering.domain.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderingRepository extends JpaRepository<Ordering, Long> {
    List<Ordering> findAllByMemberId(Long id);

    List<Ordering> findAllByMember(Member member);
}
