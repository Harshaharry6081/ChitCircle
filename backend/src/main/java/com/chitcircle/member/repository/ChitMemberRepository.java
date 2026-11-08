package com.chitcircle.member.repository;

import com.chitcircle.member.domain.ChitMember;
import com.chitcircle.member.domain.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChitMemberRepository extends JpaRepository<ChitMember, UUID> {
    List<ChitMember> findByGroupIdOrderByMemberNumberAsc(UUID groupId);

    List<ChitMember> findByGroupIdAndStatus(UUID groupId, MemberStatus status);

    Optional<ChitMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    Optional<ChitMember> findByGroupIdAndMemberNumber(UUID groupId, Integer memberNumber);

    long countByGroupId(UUID groupId);

    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    @Query("SELECT COALESCE(MAX(m.memberNumber), 0) FROM ChitMember m WHERE m.group.id = :groupId")
    int findMaxMemberNumberByGroupId(@Param("groupId") UUID groupId);
}

