package br.inmetro.gov.beacon.frontend.interfac.domain.service;

public class BadRequestException extends RuntimeException  {
    public BadRequestException(String message) {
        super(message);
    }
}
