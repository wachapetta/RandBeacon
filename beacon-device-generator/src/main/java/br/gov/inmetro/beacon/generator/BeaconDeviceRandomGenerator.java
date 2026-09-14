package br.gov.inmetro.beacon.generator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Locale;

public class BeaconDeviceRandomGenerator {

    private static final int DISK_BUFFER_SIZE = 1024 * 1024; // 1 MB buffer

    public static void main(String[] args) {
        if (args.length == 0 || containsHelp(args)) {
            printUsage();
            return;
        }

        String sizeArg = "4G";
        String outputPath = "entropy_4gb.hex";
        String command = resolveDefaultCommand();
        int targetLine = 1;
        String format = "hex"; // "binary" ou "hex"
        boolean mock = false;
        boolean streamMode = true; // Streaming contínuo ativado por padrão
        int bufferCapacity = BufferedDeviceEntropyReader.DEFAULT_BUFFER_CAPACITY;
        boolean useBuffer = true;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-s":
                case "--size":
                    if (i + 1 < args.length) sizeArg = args[++i];
                    break;
                case "-o":
                case "--output":
                    if (i + 1 < args.length) outputPath = args[++i];
                    break;
                case "-c":
                case "--command":
                    if (i + 1 < args.length) command = args[++i];
                    break;
                case "-l":
                case "--line":
                    if (i + 1 < args.length) targetLine = Integer.parseInt(args[++i]);
                    break;
                case "-f":
                case "--format":
                    if (i + 1 < args.length) format = args[++i].toLowerCase(Locale.ROOT);
                    break;
                case "-m":
                case "--mock":
                    mock = true;
                    break;
                case "--stream":
                case "-stream":
                case "-t":
                    streamMode = true;
                    break;
                case "-b":
                case "--buffer":
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        bufferCapacity = Integer.parseInt(args[++i]);
                    }
                    useBuffer = true;
                    break;
                case "--no-buffer":
                    useBuffer = false;
                    break;
                default:
                    if (!args[i].startsWith("-") && i == 0) {
                        sizeArg = args[i];
                    }
                    break;
            }
        }

        long totalTargetBytes;
        try {
            totalTargetBytes = parseSize(sizeArg);
        } catch (Exception e) {
            System.err.println("[ERRO] Formato de tamanho inválido: " + sizeArg + ". Use exemplos como: 4G, 500M, 100K, 1048576");
            System.exit(1);
            return;
        }

        IEntropyReader rawReader;
        if (mock) {
            rawReader = new LocalRngFallbackReader();
        } else if (streamMode) {
            rawReader = new StreamingDeviceEntropyReader(command);
        } else {
            rawReader = new DeviceEntropyReader(command, targetLine);
        }

        IEntropyReader reader;
        if (useBuffer && bufferCapacity > 0) {
            reader = new BufferedDeviceEntropyReader(rawReader, bufferCapacity);
        } else {
            reader = rawReader;
        }

        System.out.println("==================================================================");
        System.out.println(" RandBeacon - Gerador de Arquivo de Aleatoriedade (CentOS/Java 8)");
        System.out.println("==================================================================");
        System.out.printf("Tamanho Alvo    : %s (%d bytes)%n", sizeArg, totalTargetBytes);
        System.out.printf("Arquivo Destino : %s%n", outputPath);
        System.out.printf("Fonte / Modo    : %s%n", mock ? "Simulação (SecureRandom/NativePRNG)" : (streamMode ? "Dispositivo Físico (Streaming Contínuo)" : "Dispositivo Físico (Padrão)"));
        System.out.printf("Buffer / Prefetch: %s%n", useBuffer ? (bufferCapacity + " amostras em RAM") : "Desativado (Síncrono)");
        if (!mock) {
            System.out.printf("Comando Dispos. : %s%n", command);
            System.out.printf("Linha Retorno   : %d%n", targetLine);
        }
        System.out.printf("Formato Saída   : %s%n", format);
        System.out.println("==================================================================");

        try {
            File outputFile = new File(outputPath);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }

            long startTime = System.currentTimeMillis();
            long lastProgressUpdate = 0;
            long bytesWritten = 0;
            long totalIterations = 0;

            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile), DISK_BUFFER_SIZE)) {
                while (bytesWritten < totalTargetBytes) {
                    String rawDataHex = reader.getNoise512Bits();

                    if (rawDataHex == null || rawDataHex.trim().isEmpty()) {
                        System.err.println("\n[ERRO] Leitura nula ou vazia retornada na iteração " + totalIterations);
                        System.exit(1);
                        return;
                    }

                    if ("hex".equals(format)) {
                        byte[] hexLine = (rawDataHex + "\n").getBytes("ASCII");
                        int toWrite = (int) Math.min(hexLine.length, totalTargetBytes - bytesWritten);
                        out.write(hexLine, 0, toWrite);
                        bytesWritten += toWrite;
                    } else {
                        byte[] binaryData = hexStringToByteArray(rawDataHex);
                        int toWrite = (int) Math.min(binaryData.length, totalTargetBytes - bytesWritten);
                        out.write(binaryData, 0, toWrite);
                        bytesWritten += toWrite;
                    }

                    totalIterations++;

                    long now = System.currentTimeMillis();
                    if (now - lastProgressUpdate >= 300 || bytesWritten >= totalTargetBytes) {
                        out.flush(); // Descarrega o buffer para o disco em tempo real para monitoramento
                        printProgress(bytesWritten, totalTargetBytes, totalIterations, startTime);
                        lastProgressUpdate = now;
                    }
                }

                out.flush();
            }

            long totalTime = Math.max(1, System.currentTimeMillis() - startTime);
            double speedMBs = ((double) bytesWritten / (1024.0 * 1024.0)) / (totalTime / 1000.0);

            System.out.println();
            System.out.println("==================================================================");
            System.out.printf("[SUCESSO] Arquivo gerado com sucesso!%n");
            System.out.printf("Total de bytes  : %d bytes (%.2f MB)%n", bytesWritten, (double) bytesWritten / (1024.0 * 1024.0));
            System.out.printf("Total iterações : %d leituras de 512 bits%n", totalIterations);
            System.out.printf("Tempo total     : %.2f s%n", totalTime / 1000.0);
            System.out.printf("Taxa média      : %.2f MB/s%n", speedMBs);
            System.out.printf("Caminho arquivo : %s%n", outputFile.getAbsolutePath());
            System.out.println("==================================================================");

        } catch (Exception e) {
            System.err.println("\n[ERRO FATAL] Falha durante a geração do arquivo: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (reader instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) reader).close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static long parseSize(String sizeStr) {
        if (sizeStr == null || sizeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Tamanho não informado");
        }
        String clean = sizeStr.trim().toUpperCase(Locale.ROOT);
        long multiplier = 1;

        if (clean.endsWith("G") || clean.endsWith("GB") || clean.endsWith("GIB")) {
            multiplier = 1024L * 1024L * 1024L;
            clean = clean.replaceAll("[A-Z]", "");
        } else if (clean.endsWith("M") || clean.endsWith("MB") || clean.endsWith("MIB")) {
            multiplier = 1024L * 1024L;
            clean = clean.replaceAll("[A-Z]", "");
        } else if (clean.endsWith("K") || clean.endsWith("KB") || clean.endsWith("KIB")) {
            multiplier = 1024L;
            clean = clean.replaceAll("[A-Z]", "");
        } else if (clean.endsWith("B")) {
            clean = clean.replaceAll("[A-Z]", "");
        }

        return (long) (Double.parseDouble(clean) * multiplier);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static void printProgress(long currentBytes, long totalBytes, long iterations, long startTime) {
        double percent = (double) currentBytes / totalBytes * 100.0;
        long elapsedSec = Math.max(1, (System.currentTimeMillis() - startTime) / 1000);
        double mbWritten = (double) currentBytes / (1024.0 * 1024.0);
        double speed = mbWritten / elapsedSec;

        // Estimativa de tempo restante (ETA)
        long remainingSec = speed > 0 ? (long) (((double) totalBytes / (1024.0 * 1024.0) - mbWritten) / speed) : 0;
        long hours = remainingSec / 3600;
        long minutes = (remainingSec % 3600) / 60;
        long seconds = remainingSec % 60;

        System.out.printf(Locale.US,
                "\rProgresso: [%6.2f%%] - %.2f MB / %.2f MB | Leituras: %d | Vel: %.2f MB/s | ETA: %02d:%02d:%02d",
                percent, mbWritten, (double) totalBytes / (1024.0 * 1024.0), iterations, speed, hours, minutes, seconds);
    }

    private static boolean containsHelp(String[] args) {
        for (String arg : args) {
            if ("-h".equals(arg) || "--help".equals(arg)) return true;
        }
        return false;
    }

    private static String resolveDefaultCommand() {
        String[] candidates = {
            "/usr/local/bin/qngstream",
            "/home/beacon/libqwqng-1.4/qngstream",
            System.getProperty("user.home") + "/libqwqng-1.4/qngstream",
            "./qngstream"
        };
        for (String path : candidates) {
            File f = new File(path);
            if (f.exists() && f.canExecute()) {
                return path;
            }
        }
        return "qngstream";
    }

    private static void printUsage() {
        System.out.println("Uso: java -jar beacon-device-generator.jar [opções]");
        System.out.println();
        System.out.println("Opções:");
        System.out.println("  -s, --size <tamanho>      Tamanho do arquivo (ex: 4G, 4096M, 500K, 1073741824). Padrão: 4G");
        System.out.println("  -o, --output <caminho>    Caminho do arquivo de saída. Padrão: entropy_4gb.hex");
        System.out.println("  -c, --command <comando>   Comando do dispositivo. Padrão: qngstream (auto-detectado)");
        System.out.println("  -l, --line <número>       Linha de retorno do dado hex. Padrão: 1");
        System.out.println("  -f, --format <bin|hex>    Formato de saída: 'binary' (raw bytes) ou 'hex' (texto). Padrão: hex");
        System.out.println("  -t, --stream              Modo streaming contínuo (ativado por padrão)");
        System.out.println("  -b, --buffer <capacidade> Tamanho da fila de pré-busca em RAM (Produtor-Consumidor). Padrão: 10000");
        System.out.println("  --no-buffer               Desativa o buffer assíncrono (execução estritamente síncrona)");
        System.out.println("  -m, --mock                Modo simulação usando SecureRandom (útil sem dispositivo conectado)");
        System.out.println("  -h, --help                Exibe esta mensagem de ajuda");
        System.out.println();
        System.out.println("Exemplos no CentOS:");
        System.out.println("  java -jar beacon-device-generator.jar -s 4G -o entropy_4gb.hex");
        System.out.println("  nohup java -jar beacon-device-generator.jar -s 4G -o entropy_4gb.hex > gerador.log 2>&1 &");
        System.out.println("  java -jar beacon-device-generator.jar -s 100M -o /dados/teste.bin --mock");
    }
}
