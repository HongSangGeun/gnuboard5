package com.gnuboard.springboard.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardGroupMember {
    private Long gmId;
    private String grId;
    private String mbId;
    private String gmDatetime;
    private String grSubject;
}

