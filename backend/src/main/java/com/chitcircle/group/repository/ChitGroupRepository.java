package com.chitcircle.group.repository;

import com.chitcircle.group.domain.ChitGroup;
import com.chitcircle.group.domain.ChitGroupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChitGroupRepository extends JpaRepository<ChitGroup, UUID> {
    List<ChitGroup> findByStatus(ChitGroupStatus status);

    @Query("SELECT g FROM ChitGroup g WHERE g.createdBy.id = :userId")
    List<ChitGroup> findByCreatedById(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT g FROM ChitGroup g JOIN ChitMember m ON m.group.id = g.id WHERE m.user.id = :userId")
    List<ChitGroup> findGroupsByMemberUserId(@Param("userId") UUID userId);
}

