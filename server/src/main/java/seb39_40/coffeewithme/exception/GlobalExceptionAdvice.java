package seb39_40.coffeewithme.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionAdvice {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleBusinessLogicException(BusinessLogicException e) {
        System.out.println(e.getExceptionCode().getStatus());
        System.out.println(e.getMessage());

        final ErrorResponse response = ErrorResponse.of(e.getExceptionCode());

        return response;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleMethodNotAllowedException(HttpRequestMethodNotSupportedException e) {
        System.out.println(e.getMessage());
        System.out.println(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase());
        System.out.println(HttpStatus.METHOD_NOT_ALLOWED.value());
        final ErrorResponse response = ErrorResponse.of(HttpStatus.METHOD_NOT_ALLOWED);
        return response;
    }
}
