package br.inmetro.gov.beacon.frontend.vdf.sources;

public interface SeedInterface {

    /**
     * Classes implementing this interface must return a seed source DTO.
     * @return  a valid DTO object. Must return null if the source is not available,
     * or you want to limit the rate of recovering the seed values.
     * For instance, you wanna three seed values per day, or .
     */
    SeedSourceDto getSeed();
}
