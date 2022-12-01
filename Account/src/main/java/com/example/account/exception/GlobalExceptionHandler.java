package com.example.account.exception;

import com.example.account.dto.ErrorResponse;
import com.example.account.type.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice // 모든 controller에서 발생한 것을 처리하겠다.
public class GlobalExceptionHandler {

    // AccountException이 발생했을 때 그 error를 받아와서 해당 error의 code, message를 응답으로 준다.
    @ExceptionHandler(AccountException.class)
    public ErrorResponse handleAccountException(AccountException e){
        log.error("{} is occurred.", e.getErrorCode());

        return new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
    }

    // DB 접근 시 unique key 등과 같은 곳에서 중복이 발생할 때
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException e){
        log.error("DataIntegrityViolationException is occurred.", e);

        return new ErrorResponse(
                ErrorCode.INVALID_REQUEST,
                ErrorCode.INVALID_REQUEST.getDescription()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        log.error("MethodArgumentNotValidException is occurred.", e);

        return new ErrorResponse(
                ErrorCode.INVALID_REQUEST,
                ErrorCode.INVALID_REQUEST.getDescription()
        );
    }

    // 개발자가 예상하기 못한 에러 발생시...ㅎㄷㄷ
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(Exception e){
        log.error("Exception is occurred.", e);

        return new ErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getDescription()
        );
    }
}
