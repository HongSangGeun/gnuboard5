package com.gnuboard.springboard.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
    private int meId;
    private String meCode;
    private String meName;
    private String meLink;
    private String meTarget;
    private int meOrder;
    private int meUse;
    private int meMobileUse;
}
