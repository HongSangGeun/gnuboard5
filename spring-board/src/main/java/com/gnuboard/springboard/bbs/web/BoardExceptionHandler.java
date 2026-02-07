package com.gnuboard.springboard.bbs.web;

import com.gnuboard.springboard.bbs.service.BoardPermissionDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class BoardExceptionHandler {

    @ExceptionHandler(BoardPermissionDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handlePermissionDenied(BoardPermissionDeniedException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "bbs/error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "bbs/error";
    }
}
