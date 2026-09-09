package br.gov.inmetro.beacon.generator;

public interface IEntropyReader {
    /**
     * Retorna uma sequência de 512 bits (64 bytes) em formato Hexadecimal (128 caracteres hex).
     *
     * @return String hexadecimal de 512 bits (128 caracteres)
     * @throws Exception caso ocorra erro na leitura da fonte de entropia
     */
    String getNoise512Bits() throws Exception;
}
