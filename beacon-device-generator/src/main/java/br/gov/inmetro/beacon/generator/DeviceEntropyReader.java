package br.gov.inmetro.beacon.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Implementação de alto desempenho da leitura do dispositivo físico de aleatoriedade (ex: ComScire PQ32MS / libqwqng).
 * Mantém um único processo ativo em segundo plano (Modo Pipe Persistente / Streaming),
 * eliminando o overhead de criação e destruição de processos ('fork/exec') a cada 512 bits gerados.
 */
public class DeviceEntropyReader implements IEntropyReader, AutoCloseable {

    private final String command;
    private final int targetLine;
    private final int skipLines;

    private Process process;
    private BufferedReader stdInput;
    private boolean started = false;

    public DeviceEntropyReader(String command) {
        this(command, 1);
    }

    public DeviceEntropyReader(String command, int targetLine) {
        this.command = command;
        this.targetLine = Math.max(1, targetLine);
        this.skipLines = Math.max(0, targetLine - 1);
    }

    private synchronized void initProcess() throws IOException {
        if (started && process != null && process.isAlive()) {
            return;
        }

        close();

        ProcessBuilder pb;
        if (command.contains(" ") || command.contains("|") || command.contains("\"") || command.contains("'")) {
            pb = new ProcessBuilder("/bin/sh", "-c", command);
        } else {
            pb = new ProcessBuilder(command);
        }
        configureEnvironment(pb);
        this.process = pb.start();

        this.stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()), 65536);

        // Pula eventuais linhas iniciais (ex: cabeçalhos ou até a targetLine)
        for (int i = 0; i < skipLines; i++) {
            String line = stdInput.readLine();
            if (line == null) {
                break;
            }
        }

        this.started = true;
    }

    @Override
    public synchronized String getNoise512Bits() throws Exception {
        int restartCount = 0;
        int maxRestarts = 10_000_000;

        while (restartCount < maxRestarts) {
            if (!started || process == null || !process.isAlive()) {
                initProcess();
            }

            String line;
            while ((line = stdInput.readLine()) != null) {
                String clean = line.replaceAll(" ", "").trim();
                // Aceita linhas hexadecimais válidas com tamanho compatível
                if (isValidHex(clean)) {
                    return clean;
                }
            }

            // Caso o processo tenha finalizado (EOF do fluxo de saída)
            int exitCode = -1;
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }

            if (exitCode == 0) {
                // Processo terminou com sucesso (ex: comandos que geram amostra única e finalizam).
                // Reinicia o processo e continua a leitura para manter compatibilidade.
                close();
                restartCount++;
            } else {
                StringBuilder stderr = new StringBuilder();
                if (process.getErrorStream() != null) {
                    try (BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String errLine;
                        while ((errLine = errReader.readLine()) != null) {
                            stderr.append("\n[STDERR]: ").append(errLine);
                        }
                    } catch (Exception ignored) {
                    }
                }

                throw new IOException(String.format(
                        "Fluxo de dados finalizado pelo comando com erro (Exit Code: %d). Comando: %s%s",
                        exitCode, command, stderr
                ));
            }
        }

        throw new IOException("Número máximo de tentativas de reinicialização atingido para o comando: " + command);
    }

    private static boolean isValidHex(String s) {
        if (s == null || s.length() < 32) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public synchronized void close() {
        if (stdInput != null) {
            try {
                stdInput.close();
            } catch (Exception ignored) {
            }
            stdInput = null;
        }

        if (process != null) {
            try {
                if (process.getOutputStream() != null) process.getOutputStream().close();
            } catch (Exception ignored) {
            }
            try {
                if (process.getErrorStream() != null) process.getErrorStream().close();
            } catch (Exception ignored) {
            }
            try {
                process.destroy();
            } catch (Exception ignored) {
            }
            process = null;
        }

        this.started = false;
    }

    public String getCommand() {
        return command;
    }

    public int getTargetLine() {
        return targetLine;
    }

    public int getSkipLines() {
        return skipLines;
    }

    public boolean isStarted() {
        return started && process != null && process.isAlive();
    }

    static void configureEnvironment(ProcessBuilder pb) {
        String existingLd = pb.environment().get("LD_LIBRARY_PATH");
        String customLd = "/home/beacon/libqwqng-1.4/build/src:/home/beacon/libqwqng-1.4/libqwqng-1.4/build/src:/usr/local/lib:/usr/local/lib64";
        pb.environment().put("LD_LIBRARY_PATH", existingLd == null || existingLd.isEmpty() ? customLd : customLd + ":" + existingLd);
    }
}
