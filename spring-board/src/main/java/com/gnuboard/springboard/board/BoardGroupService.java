package com.gnuboard.springboard.board;

import com.gnuboard.springboard.member.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BoardGroupService {
    private final BoardGroupMapper boardGroupMapper;
    private static final Pattern GROUP_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_]{1,10}$");

    public List<BoardGroup> getBoardGroups(SearchCriteria criteria) {
        if (criteria.getPage() < 1)
            criteria.setPage(1);
        if (criteria.getLimit() < 1)
            criteria.setLimit(15);
        if (criteria.getSst() == null || criteria.getSst().isEmpty())
            criteria.setSst("gr_id"); // default sort
        if (criteria.getSod() == null || criteria.getSod().isEmpty())
            criteria.setSod("asc");

        return boardGroupMapper.findAll(criteria);
    }

    public int getBoardGroupCount(SearchCriteria criteria) {
        return boardGroupMapper.count(criteria);
    }

    public BoardGroup getBoardGroup(String grId) {
        return boardGroupMapper.findById(grId);
    }

    @Transactional
    public void saveBoardGroup(BoardGroup group, boolean isUpdate) {
        validateGroup(group);
        if (isUpdate) {
            boardGroupMapper.update(group);
        } else {
            // Check existence?
            if (boardGroupMapper.findById(group.getGrId()) != null) {
                throw new IllegalStateException("Group ID already exists");
            }
            boardGroupMapper.insert(group);
        }
    }

    @Transactional
    public void deleteBoardGroup(String grId) {
        // Should check dependencies (boards, members) before delete?
        // Legacy system usually just warns.
        boardGroupMapper.delete(grId);
    }

    public int getGroupMemberCount(String grId) {
        return boardGroupMapper.countGroupMembers(grId);
    }

    public int getBoardCount(String grId) {
        return boardGroupMapper.countBoards(grId);
    }

    private void validateGroup(BoardGroup group) {
        if (group == null) {
            throw new IllegalArgumentException("게시판그룹 데이터가 비어 있습니다.");
        }
        if (group.getGrId() == null || !GROUP_ID_PATTERN.matcher(group.getGrId()).matches()) {
            throw new IllegalArgumentException("그룹 ID는 영문자, 숫자, _ 만 사용 가능하며 10자 이하여야 합니다.");
        }
        if (group.getGrSubject() == null || group.getGrSubject().isBlank()) {
            throw new IllegalArgumentException("그룹 제목을 입력하세요.");
        }
        if (group.getGrDevice() == null || group.getGrDevice().isBlank()) {
            group.setGrDevice("both");
        }
        if (group.getGrAdmin() == null) {
            group.setGrAdmin("");
        }
    }
}
