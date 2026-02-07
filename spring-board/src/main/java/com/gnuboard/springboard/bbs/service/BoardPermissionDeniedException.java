package com.gnuboard.springboard.bbs.service;

public class BoardPermissionDeniedException extends RuntimeException {
    public BoardPermissionDeniedException(String message) {
        super(message);
    }
}

