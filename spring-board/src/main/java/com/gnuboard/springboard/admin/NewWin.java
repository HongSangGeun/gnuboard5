package com.gnuboard.springboard.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewWin {
    private int nwId;
    private String nwDivision;
    private String nwDevice;
    private LocalDateTime nwBeginTime;
    private LocalDateTime nwEndTime;
    private int nwDisableHours;
    private int nwLeft;
    private int nwTop;
    private int nwHeight;
    private int nwWidth;
    private String nwSubject;
    private String nwContent;
    private int nwContentHtml;
}
