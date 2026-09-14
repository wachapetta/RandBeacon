package br.gov.inmetro.beacon.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Leitor de entropia em modo contínuo (Streaming) a partir de um processo do sistema operacional.
 * Mantém um único processo ativo em segundo plano e consome seu fluxo de saída (stdout) continuamente,
 * eliminando a sobrecarga de criação/destruição de processos ('fork/exec') a cada 64 bytes gerados.
 */
public class StreamingDeviceEntropyReader implements IEntropyReader, AutoCloseable {

    private final String command;
    private final int skipLines;
    private Process process;
    private BufferedReader stdInput;
    private boolean started = false;

    public StreamingDeviceEntropyReader(String command) {
        this(command, 0);
    }

    public StreamingDeviceEntropyReader(String command, int skipLines) {
        this.command = command;
        this.skipLines = Math.max(0, skipLines);
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
        DeviceEntropyReader.configureEnvironment(pb);
        this.process = pb.start();
        this.stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()), 65536);

        // Pula eventuais linhas iniciais se configurado explicitamente
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
                // System.out.println("[Saída do comando (Stream)]: " + line);
                String clean = line.replaceAll(" ", "").trim();
                // Filtra linhas em branco, mensagens de 'EXIT...', banners e aceita apenas hex válido
                if (isValidHex(clean)) {
                    return clean;
                }
            }

            // Caso o processo tenha chegado ao EOF
            int exitCode = -1;
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }

            if (exitCode == 0) {
                // Processo terminou com sucesso (ex: utilitários como randbytes geram N amostras e dão exit 0).
                // Reinicia o processo e continua a leitura para a próxima amostra.
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

        throw new IOException("Número máximo de tentativas de execução atingido para o comando: " + command);
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

    public int getSkipLines() {
        return skipLines;
    }

    public boolean isStarted() {
        return started && process != null && process.isAlive();
    }
}
