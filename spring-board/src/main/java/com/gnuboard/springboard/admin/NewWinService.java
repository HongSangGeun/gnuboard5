package com.gnuboard.springboard.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewWinService {

    private final NewWinMapper newWinMapper;

    public List<NewWin> getNewWinList() {
        return newWinMapper.findAll();
    }

    public NewWin getNewWin(int nwId) {
        return newWinMapper.findById(nwId);
    }

    @Transactional
    public void addNewWin(NewWin newWin) {
        newWinMapper.insert(newWin);
    }

    @Transactional
    public void updateNewWin(NewWin newWin) {
        newWinMapper.update(newWin);
    }

    @Transactional
    public void deleteNewWin(int nwId) {
        newWinMapper.delete(nwId);
    }
}
