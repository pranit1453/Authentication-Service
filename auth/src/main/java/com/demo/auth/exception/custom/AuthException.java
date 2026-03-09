package com.demo.auth.exception.custom;

import com.demo.auth.exception.BaseException;
import org.springframework.http.HttpStatus;

public class AuthException extends BaseException {

    public AuthException(HttpStatus status, String message) {
        super(status, message);
    }
}
