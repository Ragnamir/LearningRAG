#!/usr/bin/env bash
# Downloads the multilingual MiniLM ONNX encoder used for embeddings.
# The model is not committed: models/ is ignored by git.
set -euo pipefail

DEST="${1:-models/multilingual-minilm}"
BASE="https://huggingface.co/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2/resolve/main"

mkdir -p "$DEST"
curl -fsSL "$BASE/onnx/model.onnx" -o "$DEST/model.onnx"
curl -fsSL "$BASE/tokenizer.json" -o "$DEST/tokenizer.json"
echo "Embedding model ready in $DEST"
