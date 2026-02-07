package com.gnuboard.springboard.board;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardGroupMemberService {

    private final BoardGroupMapper boardGroupMapper;

    public List<BoardGroupMember> getMemberGroupAssignments(String mbId) {
        return boardGroupMapper.findGroupMembersByMemberId(mbId);
    }

    public List<BoardGroup> getAccessEnabledGroups() {
        return boardGroupMapper.findAccessEnabledGroups();
    }

    @Transactional
    public void addMemberToGroup(String mbId, String grId) {
        if (mbId == null || mbId.isBlank()) {
            throw new IllegalArgumentException("회원 아이디가 비어 있습니다.");
        }
        if (grId == null || grId.isBlank()) {
            throw new IllegalArgumentException("접근가능 그룹을 선택하세요.");
        }

        BoardGroup group = boardGroupMapper.findById(grId);
        if (group == null) {
            throw new IllegalArgumentException("존재하지 않는 게시판그룹입니다.");
        }
        if (group.getGrUseAccess() != 1) {
            throw new IllegalStateException("접근회원사용이 설정된 게시판그룹만 지정할 수 있습니다.");
        }
        if (boardGroupMapper.countGroupMemberByUser(grId, mbId) > 0) {
            throw new IllegalStateException("이미 지정된 접근가능 그룹입니다.");
        }

        boardGroupMapper.insertGroupMember(grId, mbId);
    }

    @Transactional
    public int removeMemberFromGroups(String mbId, List<Long> gmIds) {
        if (mbId == null || mbId.isBlank() || gmIds == null || gmIds.isEmpty()) {
            return 0;
        }

        int removed = 0;
        for (Long gmId : gmIds) {
            if (gmId == null) {
                continue;
            }
            removed += boardGroupMapper.deleteGroupMemberById(gmId, mbId);
        }
        return removed;
    }

    public List<Long> parseIds(String[] selectedIds) {
        List<Long> parsed = new ArrayList<>();
        if (selectedIds == null) {
            return parsed;
        }
        for (String id : selectedIds) {
            if (id == null || id.isBlank()) {
                continue;
            }
            try {
                parsed.add(Long.parseLong(id));
            } catch (NumberFormatException ignored) {
                // Skip invalid id values from request.
            }
        }
        return parsed;
    }
}

