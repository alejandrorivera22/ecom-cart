package com.alex.ecom_cart.api.controllers.error_handler;

import com.alex.ecom_cart.api.dtos.response.BaseErrorResponse;
import com.alex.ecom_cart.api.dtos.response.ErrorResponse;
import com.alex.ecom_cart.util.exceptions.ResourceNotEnabledException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ResoureceNotEnabledController {

    @ExceptionHandler(value = ResourceNotEnabledException.class)
    public BaseErrorResponse handleIdNotFound(ResourceNotEnabledException exception) {
        return ErrorResponse
                .builder()
                .message(exception.getMessage())
                .status(HttpStatus.FORBIDDEN.name())
                .code(HttpStatus.FORBIDDEN.value())
                .build();

    }

}
