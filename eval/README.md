# RAGAS Evaluation

This directory contains optional Python tooling for RAGAS evaluation. The Java application does not depend on RAGAS; it only exports `target/rag-eval-report.json`.

Run from the repository root:

```bash
python3 -m pip install -r eval/requirements-ragas.txt
```

OpenAI judge:

```bash
OPENAI_API_KEY=... \
python3 eval/run-ragas-eval.py \
  --provider openai \
  --input target/rag-eval-report.json \
  --output target/ragas-report.json
```

Local Ollama judge:

```bash
/Applications/Ollama.app/Contents/Resources/ollama pull nomic-embed-text

python3 eval/run-ragas-eval.py \
  --provider ollama \
  --judge-model qwen2.5:7b \
  --embedding-model nomic-embed-text \
  --input target/rag-eval-report.json \
  --output target/ragas-report.json
```

For slower local models, reduce concurrency:

```bash
python3 eval/run-ragas-eval.py \
  --provider ollama \
  --judge-model qwen2.5:7b \
  --embedding-model nomic-embed-text \
  --max-workers 1 \
  --batch-size 1
```
