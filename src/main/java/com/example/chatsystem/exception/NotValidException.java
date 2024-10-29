package com.example.chatsystem.exception;

import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

public class NotValidException extends MethodArgumentNotValidException {
    public NotValidException(MethodParameter parameter, BindingResult bindingResult) {
        super(parameter, bindingResult);
    }
}
