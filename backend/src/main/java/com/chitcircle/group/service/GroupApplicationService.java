package com.chitcircle.group.service;

import com.chitcircle.common.exception.BusinessException;
import com.chitcircle.common.exception.ResourceNotFoundException;
import com.chitcircle.common.util.MoneyUtils;
import com.chitcircle.group.domain.ChitGroup;
import com.chitcircle.group.domain.ChitGroupStatus;
import com.chitcircle.group.dto.CreateGroupRequest;
import com.chitcircle.group.dto.GroupResponse;
import com.chitcircle.group.repository.ChitGroupRepository;
import com.chitcircle.identity.domain.User;
import com.chitcircle.identity.repository.UserRepository;
import com.chitcircle.member.repository.ChitMemberRepository;
import com.chitcircle.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupApplicationService {

    private final ChitGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ChitMemberRepository memberRepository;

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User creator = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Creator user not found"));

        // Chit value = monthlyContribution * totalMembers * durationMonths
        BigDecimal monthlyPool = request.getMonthlyContribution().multiply(BigDecimal.valueOf(request.getTotalMembers()));
        BigDecimal totalChitValue = monthlyPool.multiply(BigDecimal.valueOf(request.getDurationMonths()));

        ChitGroup group = ChitGroup.builder()
                .name(request.getName())
                .chitValue(MoneyUtils.format(totalChitValue))
                .monthlyContribution(MoneyUtils.format(request.getMonthlyContribution()))
                .totalMembers(request.getTotalMembers())
                .durationMonths(request.getDurationMonths())
                .startDate(request.getStartDate())
                .status(ChitGroupStatus.DRAFT)
                .createdBy(creator)
                .build();

        ChitGroup saved = groupRepository.save(group);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> listGroups(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        boolean isSuperAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        List<ChitGroup> groups;
        if (isSuperAdmin) {
            groups = groupRepository.findAll();
        } else {
            // Find groups created by user or where user is a member
            List<ChitGroup> created = groupRepository.findByCreatedById(principal.getId());
            List<ChitGroup> memberOf = groupRepository.findGroupsByMemberUserId(principal.getId());
            created.addAll(memberOf);
            groups = created.stream().distinct().collect(Collectors.toList());
        }

        return groups.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(UUID groupId) {
        ChitGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        return mapToResponse(group);
    }

    @Transactional
    public GroupResponse activateGroup(UUID groupId) {
        ChitGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (group.getStatus() == ChitGroupStatus.ACTIVE) {
            throw new BusinessException("Group is already active");
        }

        group.setStatus(ChitGroupStatus.ACTIVE);
        return mapToResponse(groupRepository.save(group));
    }

    @Transactional
    public GroupResponse closeGroup(UUID groupId) {
        ChitGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        group.setStatus(ChitGroupStatus.COMPLETED);
        return mapToResponse(groupRepository.save(group));
    }

    public GroupResponse mapToResponse(ChitGroup group) {
        int memberCount = (int) memberRepository.countByGroupId(group.getId());
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .chitValue(group.getChitValue())
                .monthlyContribution(group.getMonthlyContribution())
                .totalMembers(group.getTotalMembers())
                .durationMonths(group.getDurationMonths())
                .startDate(group.getStartDate())
                .status(group.getStatus().name())
                .createdById(group.getCreatedBy() != null ? group.getCreatedBy().getId() : null)
                .createdByName(group.getCreatedBy() != null ? group.getCreatedBy().getFullName() : null)
                .createdAt(group.getCreatedAt())
                .currentMemberCount(memberCount)
                .build();
    }
}

