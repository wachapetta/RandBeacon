# Beacon Device Random Generator (`beacon-device-generator`)

Utilitário de linha de comando (CLI) em **Java 8** para geração de arquivos massivos de aleatoriedade (ex: **4 GB**) a partir de um dispositivo físico de entropia (como o ComScire PQ32MS) no **CentOS / Linux**.

A ferramenta utiliza a mesma lógica de iteração e amostragem de 512 bits presente na classe `EntropySourceComScirePQ32MSImpl` do projeto `beacon-input`, com otimizações para execução em lote e controle estrito de descritores de processos e streams do sistema operacional.

---

## 📋 Pré-requisitos

- **Java 8** (JRE ou JDK instalado no CentOS: `yum install java-1.8.0-openjdk`)
- Maven (ou utilizar o `./mvnw` embutido)

---

## 🔨 Como Compilar

### 1. Compilar o Utilitário Nativo C++ (`qngstream`)
O `qngstream` é o utilitário nativo que conversa diretamente com a biblioteca `libqwqng` e o hardware ComScire, entregando blocos contínuos de 4.096 bytes (32.768 bits) com recuperação de timeouts.

No servidor onde o dispositivo está instalado:
```bash
cd libqwqng-1.4
g++ -O3 qngstream.cpp -Ilibqwqng-1.4/src -Lbuild/src -Wl,-rpath,/home/beacon/libqwqng-1.4/build/src -lqwqng -lftdi1 -lusb-1.0 -lpthread -o qngstream
```
*(Ou execute o script automatizado `./compile_qngstream.sh`)*.

Teste o executável:
```bash
./qngstream | head -n 1
```

### 2. Compilar o Gerador Java (`beacon-device-generator.jar`)
Dentro da pasta `beacon-device-generator`, execute:
```bash
./mvnw clean package
```
O arquivo JAR executável standalone será gerado em:
```
target/beacon-device-generator.jar
```

---

## 🚀 Como Executar

### 1. Gerar 4 GB com Alta Performance (`qngstream` por padrão)
O utilitário agora auto-detecta o `qngstream` e ativa o modo streaming com buffer em RAM por padrão:
```bash
java -jar target/beacon-device-generator.jar -s 4G -o entropy_4gb.hex
```

### 2. Executar em segundo plano no CentOS (`nohup`) e Monitorar
Ideal para geração de arquivos grandes sem travar o terminal:
```bash
nohup java -jar target/beacon-device-generator.jar -s 4G -o entropy_4gb.hex > gerador.log 2>&1 &
```

#### Como saber se o processo está sendo executado e acompanhar o progresso:

1. **Acompanhar o progresso em tempo real pelo log:**
   ```bash
   tail -f gerador.log
   ```
   *(Pressione `Ctrl + C` para sair do monitoramento sem parar a execução)*

2. **Verificar se o processo Java está ativo no sistema:**
   ```bash
   # Opção A: Ver linha de comando completa
   ps aux | grep beacon-device-generator | grep -v grep

   # Opção B: Utilitário nativo do Java (mostra o PID)
   jps -l | grep beacon-device-generator
   ```

3. **Acompanhar o crescimento do arquivo de saída em disco:**
   ```bash
   # Tamanho atual legível
   ls -lh /dados/random_4g.bin

   # Atualização contínua a cada 2 segundos na tela
   watch -n 2 ls -lh /dados/random_4g.bin
   ```

4. **Verificar se o processo nativo do dispositivo físico está ativo:**
   ```bash
   ps aux | grep -E "randbytes|rnorm" | grep -v grep
   ```

5. **Verificar o encerramento do processo após a conclusão:**
   ```bash
   cat gerador.log
   ```

### 6. Modo Simulação / Teste Rápido (`--mock`)
Gera dados criptográficos locais via `SecureRandom`/`NativePRNG` sem necessitar do hardware físico conectado:
```
java -jar target/beacon-device-generator.jar -s 100M -o /tmp/teste_100m.bin --mock
```

---

## ⚙️ Tabela de Opções e Parâmetros

| Opção | Nome Longo | Padrão | Descrição |
|---|---|---|---|
| `-s` | `--size` | `4G` | Tamanho do arquivo desejado (ex: `4G`, `4096M`, `500K`, `4294967296`). |
| `-o` | `--output` | `entropy_4gb.bin` | Caminho do arquivo de destino. |
| `-c` | `--command` | `/var/beacon-input/./randbytesbeacon` | Comando do sistema executado para ler do dispositivo. |
| `-l` | `--line` | `57` | Número da linha na saída do comando contendo a sequência hexadecimal. |
| `-f` | `--format` | `binary` | Formato do arquivo gerado: `binary` (raw 64 bytes) ou `hex` (texto). |
| `-t` | `--stream` | `false` | Modo streaming contínuo: mantém 1 único processo aberto para altíssima vazão. |
| `-b` | `--buffer` | `10000` | Capacidade da fila em memória RAM para pré-busca contínua (Produtor-Consumidor). |
| - | `--no-buffer` | `false` | Desativa o buffer assíncrono em memória (execução estritamente síncrona). |
| `-m` | `--mock` | `false` | Executa no modo simulação usando `SecureRandom` (NativePRNG). |
| `-h` | `--help` | - | Exibe o menu de ajuda com a lista de opções. |

---

## 📊 Detalhes Técnicos

- **Amostragem**: Cada leitura produz 512 bits = 64 bytes = 128 caracteres hexadecimais.
- **Pipelining Assíncrono (Entropy Pool)**: Uma thread de segundo plano pré-carrega continuamente amostras do dispositivo físico para uma fila em RAM (`ArrayBlockingQueue`), desacoplando o I/O do hardware do I/O de escrita em disco.
- **Escrita em Disco**: Buffer de 1 MB (`BufferedOutputStream`) para alta eficiência de I/O no CentOS / Linux.
- **Gerenciamento de Recursos**: Encerramento limpo via `AutoCloseable`, garantindo finalização das threads e destruição de processos externos (`Process.destroy()`).
- **Monitoramento**: Exibição de progresso em tempo real com porcentagem, volume gravado (MB), taxa instantânea (MB/s) e estimativa de término (ETA).
