#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT_NAME="MindBridge"
DATASET_FILE="$ROOT_DIR/data/lora/psychqa_synthetic.jsonl"
STAMP="$(date +%Y%m%d-%H%M%S)"
DIST_DIR="$ROOT_DIR/dist"
STAGE_DIR="$DIST_DIR/stage-$STAMP"
ARCHIVE="$DIST_DIR/${PROJECT_NAME}-app-$STAMP.tar.gz"

if [ ! -f "$DATASET_FILE" ]; then
  echo "Missing dataset file: $DATASET_FILE"
  exit 1
fi

mkdir -p "$DIST_DIR"
rm -rf "$STAGE_DIR"
mkdir -p "$STAGE_DIR/$PROJECT_NAME"

rsync -a "$ROOT_DIR/" "$STAGE_DIR/$PROJECT_NAME/" \
  --exclude '.git/' \
  --exclude '.idea/' \
  --exclude '.vscode/' \
  --exclude '.m2/' \
  --exclude '.tools/' \
  --exclude 'target/' \
  --exclude 'dist/' \
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
  --exclude '*.gguf' \
  --exclude '*.gguf.zip' \
  --exclude '*.zip' \
  --exclude 'run.log' \
  --exclude 'data/*.db' \
  --exclude 'data/*.mv.db' \
  --exclude 'data/*.trace.db' \
  --exclude 'data/*.xlsx'

(
  cd "$STAGE_DIR"
  COPYFILE_DISABLE=1 tar -czf "$ARCHIVE" "$PROJECT_NAME"
)

rm -rf "$STAGE_DIR"

echo "Created release package:"
echo "$ARCHIVE"
echo "Model GGUF is intentionally excluded. Send the model zip separately."
echo
echo "Package size:"
du -sh "$ARCHIVE"
