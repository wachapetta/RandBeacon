package com.example.beacon.interfac.api;

import com.example.beacon.vdf.application.infra.ApiError;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public abstract class ResourceResponseUtil {

    public static ResponseEntity createErrorResponse(HttpStatus status, String message){
        ApiError apiError = new ApiError(
                status, message, status.getReasonPhrase());
        HttpHeaders  headers= new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<Object>(apiError, headers, apiError.getStatus());
    }

    public static ResponseEntity pulseNotAvailable() {
        return ResourceResponseUtil.createErrorResponse(HttpStatus.NOT_FOUND,"Pulse Not Available.");
    }

    public static ResponseEntity invalidCall() {
        return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,"Invalid parameter format or call function.");
    }

    public static ResponseEntity internalError() {
        return ResourceResponseUtil.createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,"Server Error.");
    }

    public static ResponseEntity notImplemented() {
        return ResourceResponseUtil.createErrorResponse(HttpStatus.NOT_IMPLEMENTED,"Feature not implemented.");
    }

    public static ResponseEntity badRequest() {
        return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,"Bad request.");
    }

    public static ResponseEntity notFound() {
        return ResourceResponseUtil.createErrorResponse(HttpStatus.NOT_FOUND,"Not Found or implemented.");
    }

    public static ResponseEntity unsupportedType() {
        return ResourceResponseUtil.createErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE,"Unsupported media type.");
    }

}
