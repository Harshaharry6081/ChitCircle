package com.chitcircle.member.service;

import com.chitcircle.common.exception.BusinessException;
import com.chitcircle.common.exception.ResourceNotFoundException;
import com.chitcircle.group.domain.ChitGroup;
import com.chitcircle.group.repository.ChitGroupRepository;
import com.chitcircle.identity.domain.Role;
import com.chitcircle.identity.domain.RoleName;
import com.chitcircle.identity.domain.User;
import com.chitcircle.identity.repository.RoleRepository;
import com.chitcircle.identity.repository.UserRepository;
import com.chitcircle.member.domain.ChitMember;
import com.chitcircle.member.domain.MemberStatus;
import com.chitcircle.member.dto.AddMemberRequest;
import com.chitcircle.member.dto.MemberResponse;
import com.chitcircle.member.repository.ChitMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberApplicationService {

    private final ChitGroupRepository groupRepository;
    private final ChitMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse addMember(UUID groupId, AddMemberRequest request) {
        ChitGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        long currentCount = memberRepository.countByGroupId(groupId);
        if (currentCount >= group.getTotalMembers()) {
            throw new BusinessException("Group is already full. Maximum allowed members: " + group.getTotalMembers());
        }

        // Find or create User
        User user;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        } else {
            user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                    .orElseGet(() -> {
                        Role memberRole = roleRepository.findByRoleName(RoleName.GROUP_MEMBER)
                                .orElseGet(() -> roleRepository.save(Role.builder().roleName(RoleName.GROUP_MEMBER).build()));
                        User newUser = User.builder()
                                .fullName(request.getFullName())
                                .email(request.getEmail().toLowerCase().trim())
                                .phoneNumber(request.getPhoneNumber())
                                .passwordHash(passwordEncoder.encode("Welcome@123"))
                                .status("ACTIVE")
                                .roles(Set.of(memberRole))
                                .build();
                        return userRepository.save(newUser);
                    });
        }

        if (memberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new BusinessException("User is already a member of this chit group");
        }

        int memberNumber = request.getMemberNumber() != null ? request.getMemberNumber() :
                memberRepository.findMaxMemberNumberByGroupId(groupId) + 1;

        if (memberRepository.findByGroupIdAndMemberNumber(groupId, memberNumber).isPresent()) {
            throw new BusinessException("Member number " + memberNumber + " is already taken in this group");
        }

        ChitMember member = ChitMember.builder()
                .group(group)
                .user(user)
                .memberNumber(memberNumber)
                .status(MemberStatus.ACTIVE)
                .prizeReceived(false)
                .build();

        ChitMember saved = memberRepository.save(member);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers(UUID groupId) {
        return memberRepository.findByGroupIdOrderByMemberNumberAsc(groupId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemberResponse getMember(UUID memberId) {
        ChitMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
        return mapToResponse(member);
    }

    @Transactional
    public MemberResponse updateStatus(UUID memberId, MemberStatus newStatus) {
        ChitMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
        member.setStatus(newStatus);
        return mapToResponse(memberRepository.save(member));
    }

    public MemberResponse mapToResponse(ChitMember member) {
        return MemberResponse.builder()
                .id(member.getId())
                .groupId(member.getGroup().getId())
                .groupName(member.getGroup().getName())
                .userId(member.getUser().getId())
                .fullName(member.getUser().getFullName())
                .email(member.getUser().getEmail())
                .phoneNumber(member.getUser().getPhoneNumber())
                .memberNumber(member.getMemberNumber())
                .status(member.getStatus().name())
                .prizeReceived(member.getPrizeReceived())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}

