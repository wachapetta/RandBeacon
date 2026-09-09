# Beacon Device Random Generator (`beacon-device-generator`)

Utilitário de linha de comando (CLI) em **Java 8** para geração de arquivos massivos de aleatoriedade (ex: **4 GB**) a partir de um dispositivo físico de entropia (como o ComScire PQ32MS) no **CentOS / Linux**.

A ferramenta utiliza a mesma lógica de iteração e amostragem de 512 bits presente na classe `EntropySourceComScirePQ32MSImpl` do projeto `beacon-input`, com otimizações para execução em lote e controle estrito de descritores de processos e streams do sistema operacional.

---

## 📋 Pré-requisitos

- **Java 8** (JRE ou JDK instalado no CentOS: `yum install java-1.8.0-openjdk`)
- Maven (ou utilizar o `./mvnw` embutido)

---

## 🔨 Como Compilar

Dentro da pasta do projeto, execute:

```
./mvnw clean package
```

O arquivo JAR executável standalone será gerado em:
```
target/beacon-device-generator.jar
```

---

## 🚀 Como Executar

### 1. Gerar 4 GB Binários via Dispositivo Físico
```
java -jar target/beacon-device-generator.jar -s 4G -o /dados/random_4g.bin -c "/var/beacon-input/./randbytesbeacon" -l 57
```

### 2. Gerar 4 GB usando outro comando (ex: `rnorm`)
```
java -jar target/beacon-device-generator.jar -s 4G -o /dados/random_4g.bin -c "rnorm --precision 40" -l 57
```

### 3. Gerar em formato texto Hexadecimal (linhas de 128 caracteres)
```
java -jar target/beacon-device-generator.jar -s 4G -o /dados/random_4g.hex -f hex
```

### 4. Modo Streaming Contínuo (Alta Performance - Recomendado para 4 GB)
Mantém um único processo aberto no sistema operacional e consome o fluxo contínuo de entropia, eliminando a sobrecarga de recriar processos a cada 64 bytes:
```
java -jar target/beacon-device-generator.jar -s 4G -o /dados/random_4g.bin -c "/home/beacon/libqwqng-1.4/libqwqng-1.4/build/examples/./randbytes" -l 1 --stream
```

### 5. Executar em segundo plano no CentOS (`nohup`)
Ideal para geração de arquivos grandes que levam tempo:
```
nohup java -jar target/beacon-device-generator.jar -s 4G -o /dados/random_4g.bin --stream > gerador.log 2>&1 &
```
Acompanhe o progresso em tempo real:
```
tail -f gerador.log
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
| `-f` | `--format` | `binary` | Formato do arquivo gerado: `binary` (raw bytes) ou `hex` (texto). |
| `-t` | `--stream` | `false` | Modo streaming contínuo: mantém 1 único processo aberto para altíssima vazão. |
| `-m` | `--mock` | `false` | Executa no modo simulação usando `SecureRandom` (NativePRNG). |
| `-h` | `--help` | - | Exibe o menu de ajuda com a lista de opções. |

---

## 📊 Detalhes Técnicos

- **Amostragem**: Cada chamada ao dispositivo produz 512 bits = 64 bytes = 128 caracteres hexadecimais.
- **Escrita em Disco**: Buffer de 1 MB (`BufferedOutputStream`) para alta eficiência de I/O no CentOS.
- **Gerenciamento de Recursos**: Fechamento rigoroso de streams (`stdin`, `stdout`, `stderr`) e encerramento de processos (`Process.destroy()`) a cada iteração, prevenindo o erro `Too many open files`.
- **Monitoramento**: Exibição de progresso com porcentagem, volume gravado (MB), taxa instantânea (MB/s) e estimativa de término (ETA).
