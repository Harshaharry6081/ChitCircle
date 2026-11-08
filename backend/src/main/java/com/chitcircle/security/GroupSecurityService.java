package com.chitcircle.security;

import com.chitcircle.group.repository.ChitGroupRepository;
import com.chitcircle.member.repository.ChitMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("groupSecurity")
@RequiredArgsConstructor
public class GroupSecurityService {

    private final ChitGroupRepository groupRepository;
    private final ChitMemberRepository memberRepository;

    public boolean canAccessGroup(Authentication authentication, UUID groupId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }

        // SUPER_ADMIN has global access
        boolean isSuperAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) {
            return true;
        }

        // Group creator/admin check
        boolean isCreator = groupRepository.findById(groupId)
                .map(g -> g.getCreatedBy() != null && g.getCreatedBy().getId().equals(principal.getId()))
                .orElse(false);
        if (isCreator) {
            return true;
        }

        // Active member check
        return memberRepository.findByGroupIdAndUserId(groupId, principal.getId()).isPresent();
    }

    public boolean isGroupAdmin(Authentication authentication, UUID groupId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }

        boolean isSuperAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) {
            return true;
        }

        return groupRepository.findById(groupId)
                .map(g -> g.getCreatedBy() != null && g.getCreatedBy().getId().equals(principal.getId()))
                .orElse(false);
    }
}

