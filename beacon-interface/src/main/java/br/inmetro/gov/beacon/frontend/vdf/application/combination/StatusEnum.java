package br.inmetro.gov.beacon.frontend.vdf.application.combination;

public enum StatusEnum {

    OPEN("Time Slot Open"),
    RUNNING("Running"),
    STOPPED("Stopped");

    private final String description;

    StatusEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
