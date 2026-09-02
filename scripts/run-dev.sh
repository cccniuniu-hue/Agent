#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

DEFAULT_OLLAMA_BIN="$(command -v ollama || true)"
if [ -z "$DEFAULT_OLLAMA_BIN" ] && [ -x "/Applications/Ollama.app/Contents/Resources/ollama" ]; then
  DEFAULT_OLLAMA_BIN="/Applications/Ollama.app/Contents/Resources/ollama"
fi
OLLAMA_BIN="${OLLAMA_BIN:-$DEFAULT_OLLAMA_BIN}"
OLLAMA_HOST="${OLLAMA_HOST:-127.0.0.1:11434}"
OLLAMA_BASE_URL="${OLLAMA_BASE_URL:-http://127.0.0.1:11434}"
OLLAMA_MODEL="${OLLAMA_MODEL:-mindbridge-qwen2.5-7b-ft:latest}"
if [ -z "${JAVA_HOME:-}" ] && [ -d "$ROOT_DIR/.tools/amazon-corretto-17.jdk/Contents/Home" ]; then
  export JAVA_HOME="$ROOT_DIR/.tools/amazon-corretto-17.jdk/Contents/Home"
fi
if [ -x "$ROOT_DIR/.tools/apache-maven-3.9.9/bin/mvn" ]; then
  DEFAULT_MAVEN_BIN="$ROOT_DIR/.tools/apache-maven-3.9.9/bin/mvn"
else
  DEFAULT_MAVEN_BIN="$(command -v mvn || true)"
fi
MAVEN_BIN="${MAVEN_BIN:-$DEFAULT_MAVEN_BIN}"

if [ ! -x "$OLLAMA_BIN" ]; then
  echo "Cannot find Ollama."
  echo "Install Ollama or set OLLAMA_BIN to the ollama executable path."
  exit 1
fi

if [ ! -x "$MAVEN_BIN" ]; then
  echo "Cannot find Maven."
  echo "Install Maven or set MAVEN_BIN to the mvn executable path."
  exit 1
fi

mkdir -p data

if ! curl -fsS "$OLLAMA_BASE_URL/api/tags" >/dev/null 2>&1; then
  echo "Starting Ollama on $OLLAMA_HOST ..."
  OLLAMA_HOST="$OLLAMA_HOST" "$OLLAMA_BIN" serve > data/ollama.log 2>&1 &

  for _ in $(seq 1 30); do
    if curl -fsS "$OLLAMA_BASE_URL/api/tags" >/dev/null 2>&1; then
      break
    fi
    sleep 1
  done
fi

if ! curl -fsS "$OLLAMA_BASE_URL/api/tags" >/dev/null 2>&1; then
  echo "Ollama did not start. Check data/ollama.log."
  exit 1
fi

if ! "$OLLAMA_BIN" list | awk 'NR > 1 {print $1}' | grep -qx "$OLLAMA_MODEL"; then
  if [ "$OLLAMA_MODEL" = "mindbridge-qwen2.5-7b-ft:latest" ] && [ -f "$ROOT_DIR/models/mindbridge-qwen2.5-7b-ft/Modelfile" ]; then
    echo "Creating mindbridge-qwen2.5-7b-ft:latest from models/mindbridge-qwen2.5-7b-ft/Modelfile ..."
    "$OLLAMA_BIN" create mindbridge-qwen2.5-7b-ft:latest -f "$ROOT_DIR/models/mindbridge-qwen2.5-7b-ft/Modelfile"
  else
    echo "Pulling $OLLAMA_MODEL ..."
    "$OLLAMA_BIN" pull "$OLLAMA_MODEL"
  fi
fi

AI_PROVIDER=ollama \
OLLAMA_BASE_URL="$OLLAMA_BASE_URL" \
OLLAMA_MODEL="$OLLAMA_MODEL" \
  "$MAVEN_BIN" -Dmaven.repo.local=.m2/repository spring-boot:run
