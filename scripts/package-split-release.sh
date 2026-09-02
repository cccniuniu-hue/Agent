#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT_NAME="MindBridge"
MODEL_DIR_NAME="mindbridge-qwen2.5-7b-ft"
MODEL_FILE_NAME="mindbridge-qwen2.5-7b-ft-q4_k_m.gguf"
DATASET_FILE="$ROOT_DIR/data/lora/psychqa_synthetic.jsonl"
STAMP="$(date +%Y%m%d-%H%M%S)"
DIST_DIR="$ROOT_DIR/dist"
STAGE_DIR="$DIST_DIR/split-stage-$STAMP"
APP_ARCHIVE="$DIST_DIR/${PROJECT_NAME}-app-$STAMP.tar.gz"
MODEL_ARCHIVE="$DIST_DIR/${PROJECT_NAME}-model-$STAMP.tar.gz"

MODEL_DIR="${MODEL_DIR:-$ROOT_DIR/dist/MindBridge-langding/models/$MODEL_DIR_NAME}"
if [ ! -f "$MODEL_DIR/$MODEL_FILE_NAME" ]; then
  MODEL_DIR="$ROOT_DIR/models/$MODEL_DIR_NAME"
fi

if [ ! -f "$MODEL_DIR/$MODEL_FILE_NAME" ]; then
  echo "Missing model file: $MODEL_DIR/$MODEL_FILE_NAME"
  echo "Set MODEL_DIR to the directory containing $MODEL_FILE_NAME."
  exit 1
fi

if [ ! -f "$DATASET_FILE" ]; then
  echo "Missing dataset file: $DATASET_FILE"
  exit 1
fi

mkdir -p "$DIST_DIR"
rm -rf "$STAGE_DIR"
mkdir -p "$STAGE_DIR/$PROJECT_NAME" "$STAGE_DIR/models"

rsync -a "$ROOT_DIR/" "$STAGE_DIR/$PROJECT_NAME/" \
  --exclude '.git/' \
  --exclude '.idea/' \
  --exclude '.vscode/' \
  --exclude '.m2/' \
  --exclude '.tools/' \
  --exclude 'target/' \
  --exclude 'dist/' \
  --exclude 'models/' \
  --include 'data/' \
  --include 'data/lora/' \
  --include 'data/lora/psychqa_synthetic.jsonl' \
  --exclude 'data/**' \
  --exclude 'logs/' \
  --exclude 'scripts/generate-lora-dataset.py' \
  --exclude '*.pdf' \
  --exclude '.DS_Store' \
  --exclude '*.iml' \
  --exclude '*.log' \
  --exclude 'run.log' \
  --exclude 'data/*.db' \
  --exclude 'data/*.mv.db' \
  --exclude 'data/*.trace.db' \
  --exclude 'data/*.xlsx'

rsync -a "$MODEL_DIR/" "$STAGE_DIR/models/$MODEL_DIR_NAME/"

(
  cd "$STAGE_DIR"
  COPYFILE_DISABLE=1 tar -czf "$APP_ARCHIVE" "$PROJECT_NAME"
  COPYFILE_DISABLE=1 tar -czf "$MODEL_ARCHIVE" models
)

rm -rf "$STAGE_DIR"

echo "Created split release packages:"
echo "$APP_ARCHIVE"
du -sh "$APP_ARCHIVE"
echo "$MODEL_ARCHIVE"
du -sh "$MODEL_ARCHIVE"
