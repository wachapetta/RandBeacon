#!/bin/bash
# ==============================================================================
# RandBeacon - Script Auxiliar de Execução do Gerador no CentOS
# ==============================================================================

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
JAR_FILE="$DIR/target/beacon-device-generator.jar"

# 1. Se o JAR não existir, compila via Maven Wrapper se disponível ou mvn
if [ ! -f "$JAR_FILE" ]; then
    echo "[INFO] Compilando o módulo beacon-device-generator..."
    if [ -f "$DIR/../beacon-input/mvnw" ]; then
        (cd "$DIR/../beacon-input" && ./mvnw clean package -f "$DIR/pom.xml")
    elif command -v mvn >/dev/null 2>&1; then
        (cd "$DIR" && mvn clean package)
    else
        echo "[INFO] Maven não encontrado no path. Compilando diretamente com javac..."
        mkdir -p "$DIR/target/classes"
        javac -source 1.8 -target 1.8 -d "$DIR/target/classes" $(find "$DIR/src/main/java" -name "*.java")
        jar cfe "$JAR_FILE" br.gov.inmetro.beacon.generator.BeaconDeviceRandomGenerator -C "$DIR/target/classes" .
    fi
    echo "[OK] Compilação concluída: $JAR_FILE"
fi

# 2. Executa a aplicação passando todos os parâmetros recebidos
java -jar "$JAR_FILE" "$@"
