package br.gov.inmetro.beacon.generator;

import java.security.SecureRandom;

/**
 * Leitor simulado de 512 bits baseado em SecureRandom (Local RNG / NativePRNG).
 * Útil para testes de desempenho, simulação e desenvolvimento sem necessidade do hardware físico conectado.
 */
public class LocalRngFallbackReader implements IEntropyReader {

    private final SecureRandom secureRandom;
    private final byte[] buffer = new byte[64]; // 512 bits = 64 bytes

    public LocalRngFallbackReader() {
        SecureRandom sr;
        try {
            sr = SecureRandom.getInstance("NativePRNGNonBlocking");
        } catch (Exception e) {
            try {
                sr = SecureRandom.getInstance("NativePRNG");
            } catch (Exception ex) {
                sr = new SecureRandom();
            }
        }
        this.secureRandom = sr;
    }

    public LocalRngFallbackReader(String algorithm) {
        SecureRandom sr;
        try {
            sr = SecureRandom.getInstance(algorithm);
        } catch (Exception e) {
            sr = new SecureRandom();
        }
        this.secureRandom = sr;
    }

    @Override
    public String getNoise512Bits() {
        secureRandom.nextBytes(buffer);
        return bytesToHex(buffer);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
