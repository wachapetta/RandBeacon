package com.example.beacon.vdf.application.infra;

import com.example.beacon.interfac.api.ResourceResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@RestController
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomRestExceptionHandler implements ErrorController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    //@ExceptionHandler(NoHandlerFoundException.class)
    @RequestMapping("/error")
    @ExceptionHandler(value
            = { Exception.class })
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {

        if (ex instanceof NoHandlerFoundException) {

            return ResourceResponseUtil.notFound();

        }
        else if (ex instanceof HttpMediaTypeNotSupportedException) {

            return ResourceResponseUtil.unsupportedType();
        }
        else if (ex instanceof MethodArgumentNotValidException) {

            return ResourceResponseUtil.invalidCall();
        }
        else if (ex instanceof MissingServletRequestParameterException) {
            return ResourceResponseUtil.invalidCall();
        }

        return ResourceResponseUtil.badRequest();

    }

    //@Override
    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();

        return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,error);
    }

    private static HttpHeaders getResponseHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        return responseHeaders;
    }

    //@Override
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        StringBuilder builder = new StringBuilder();

        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t + ", "));

        return ResourceResponseUtil.createErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE,builder.substring(0, builder.length() - 2));

    }

    //@Override
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,"Validation failed.");

    }

    //@Override
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        String error = ex.getParameterName() + " parameter is missing";

        return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,error);
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

}
