package com.example.beacon.vdf.application.infra;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Arrays;


@Getter
public class ApiError {
    private int httpStatus;
    private HttpStatus status;
    private String message;
    private List<String> errors;

    public ApiError(HttpStatus status, String message, String error) {
        super();
        this.httpStatus = status.value();
        this.status = status;
        this.message =message;
        errors = Arrays.asList(error);
    }

}
