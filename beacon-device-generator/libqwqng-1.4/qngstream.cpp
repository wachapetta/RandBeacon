#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <signal.h>
#include <unistd.h>
#include <qwqng.hpp>

/**
 * Utilitário de streaming de alta velocidade para ComScire / libqwqng.
 * Lê blocos contínuos de entropia (padrão: 4.096 bytes = 32.768 bits) com recuperação automática de timeouts.
 * 
 * Uso:
 *   ./qngstream               # Gera blocos de 4 KB em hexadecimal por linha
 *   ./qngstream 8192          # Gera blocos de 8 KB em hexadecimal por linha
 *   ./qngstream -b            # Gera fluxo contínuo de bytes binários puros
 */
int main(int argc, char* argv[])
{
    // Ignora sinal SIGPIPE para evitar crash se o leitor fechar
    signal(SIGPIPE, SIG_IGN);

    int blockSize = 4096; // 4 KB por leitura (32.768 bits - altamente estável)
    bool binaryMode = false;

    for (int i = 1; i < argc; i++) {
        if (strcmp(argv[i], "--binary") == 0 || strcmp(argv[i], "-b") == 0) {
            binaryMode = true;
        } else {
            int customSize = atoi(argv[i]);
            if (customSize > 0) {
                blockSize = (customSize > 65536) ? 65536 : customSize;
            }
        }
    }

    QWQNG* QNG = new QWQNG();
    char* status = QNG->StatusString();
    if (status != NULL && (strstr(status, "error") != NULL || strstr(status, "not found") != NULL)) {
        fprintf(stderr, "[ERRO] Falha ao inicializar dispositivo QNG: %s\n", status);
        delete QNG;
        return EXIT_FAILURE;
    }

    char* buffer = new char[blockSize];

    // Loop contínuo: gera blocos enquanto a saída (pipe / stdout) estiver aberta
    while (true) {
        int qngStatus = QNG->RandBytes(buffer, blockSize);
        
        // Se ocorrer timeout ou buffer cheio temporário, tenta auto-recuperar
        if (qngStatus != S_OK) {
            fprintf(stderr, "[AVISO] Timeout na leitura (%s). Tentando purgar buffers...\n", QNG->StatusString());
            QNG->Clear();
            usleep(10000); // 10ms
            qngStatus = QNG->RandBytes(buffer, blockSize);

            if (qngStatus != S_OK) {
                fprintf(stderr, "[AVISO] Tentando reset do hardware QNG...\n");
                QNG->Reset();
                usleep(50000); // 50ms
                qngStatus = QNG->RandBytes(buffer, blockSize);
            }

            if (qngStatus != S_OK) {
                fprintf(stderr, "[ERRO FATAL] Falha irrecuperável na leitura do dispositivo: %s\n", QNG->StatusString());
                delete[] buffer;
                delete QNG;
                return EXIT_FAILURE;
            }
        }

        if (binaryMode) {
            size_t written = fwrite(buffer, 1, blockSize, stdout);
            if (written < (size_t)blockSize) {
                break; // O leitor fechou o pipe
            }
        } else {
            for (int i = 0; i < blockSize; i++) {
                printf("%02x", (unsigned char)buffer[i]);
            }
            if (printf("\n") < 0) {
                break; // Pipe fechado
            }
        }
        if (fflush(stdout) != 0) {
            break; // Pipe quebrado
        }
    }

    delete[] buffer;
    delete QNG;
    return EXIT_SUCCESS;
}
