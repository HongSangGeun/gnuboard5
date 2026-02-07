package com.gnuboard.springboard.board;

import com.gnuboard.springboard.member.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardConfigService {
    private final BoardConfigMapper boardMapper;

    public List<Board> getBoardList(SearchCriteria criteria) {
        if (criteria.getPage() < 1)
            criteria.setPage(1);
        if (criteria.getLimit() < 1)
            criteria.setLimit(15);
        if (criteria.getSst() == null || criteria.getSst().isEmpty())
            criteria.setSst("gr_id, bo_table");
        if (criteria.getSod() == null || criteria.getSod().isEmpty())
            criteria.setSod("asc");

        return boardMapper.findAll(criteria);
    }

    public int getBoardCount(SearchCriteria criteria) {
        return boardMapper.count(criteria);
    }

    public Board getBoard(String boTable) {
        return boardMapper.findById(boTable);
    }

    @Transactional
    public void saveBoard(Board board, boolean isUpdate) {
        if (isUpdate) {
            boardMapper.update(board);
        } else {
            if (boardMapper.findById(board.getBoTable()) != null) {
                throw new IllegalStateException("Board Table already exists");
            }
            boardMapper.insert(board);
            boardMapper.createWriteTable(board.getBoTable());
        }
    }

    @Transactional
    public void deleteBoard(String boTable) {
        boardMapper.delete(boTable);
        // TODO: Drop the write table too?
    }
}
