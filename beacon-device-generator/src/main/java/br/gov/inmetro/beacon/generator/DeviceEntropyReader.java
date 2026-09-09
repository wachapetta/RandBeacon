package br.gov.inmetro.beacon.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Implementação da leitura do dispositivo físico de aleatoriedade (ex: ComScire PQ32MS),
 * seguindo o mesmo padrão da classe EntropySourceComScirePQ32MSImpl do projeto beacon-input,
 * com garantia de fechamento estrito de processos e streams para execuções massivas em lote.
 */
public class DeviceEntropyReader implements IEntropyReader {

    private final String command;
    private final int targetLine;

    public DeviceEntropyReader(String command, int targetLine) {
        this.command = command;
        this.targetLine = targetLine;
    }

    @Override
    public String getNoise512Bits() throws Exception {
        Process p = null;
        BufferedReader stdInput = null;
        try {
            p = Runtime.getRuntime().exec(command);
            stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String s;
            int currentLine = 1;
            String result = null;

            while ((s = stdInput.readLine()) != null) {
                // System.out.println("[Saída do comando - Linha " + currentLine + "]: " + s);
                if (currentLine == targetLine) {
                    result = s.replaceAll(" ", "");
                    break;
                }
                currentLine++;
            }

            if (currentLine == 1 && result == null) {
                StringBuilder stderr = new StringBuilder();
                try (BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String errLine;
                    while ((errLine = errReader.readLine()) != null) {
                        stderr.append("\n[STDERR]: ").append(errLine);
                    }
                } catch (Exception ignored) {
                }
                throw new IOException("Dispositivo indisponível ou sem saída retornada pelo comando: " + command + stderr);
            }

            if (result == null || result.trim().isEmpty()) {
                StringBuilder stderr = new StringBuilder();
                try (BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String errLine;
                    while ((errLine = errReader.readLine()) != null) {
                        stderr.append("\n[STDERR]: ").append(errLine);
                    }
                } catch (Exception ignored) {
                }
                throw new IOException("Linha alvo " + targetLine + " não encontrada na saída do comando (total de linhas lidas: " + (currentLine - 1) + ")" + stderr);
            }

            return result;

        } finally {
            // Fechamento estrito para evitar 'Too many open files' no Linux/CentOS
            if (stdInput != null) {
                try {
                    stdInput.close();
                } catch (Exception ignored) {
                }
            }
            if (p != null) {
                try {
                    if (p.getOutputStream() != null) p.getOutputStream().close();
                } catch (Exception ignored) {
                }
                try {
                    if (p.getErrorStream() != null) p.getErrorStream().close();
                } catch (Exception ignored) {
                }
                p.destroy();
            }
        }
    }

    public String getCommand() {
        return command;
    }

    public int getTargetLine() {
        return targetLine;
    }
}
