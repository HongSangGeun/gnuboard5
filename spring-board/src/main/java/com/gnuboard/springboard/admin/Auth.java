package com.gnuboard.springboard.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auth {
    private String mbId;
    private String auMenu;
    private String auAuth;
}
