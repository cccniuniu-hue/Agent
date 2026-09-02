#!/usr/bin/env bash
set -euo pipefail

if [ -z "${MAIL_USERNAME:-}" ]; then
  echo "MAIL_USERNAME is required, for example: mindbridge-alerts@example.com"
  exit 1
fi

if [ -z "${MAIL_PASSWORD:-}" ]; then
  echo "MAIL_PASSWORD is required. Use the 163 mail client authorization code, not the login password."
  exit 1
fi

export MCP_EMAIL_MODE="${MCP_EMAIL_MODE:-smtp}"
export MAIL_HOST="${MAIL_HOST:-smtp.163.com}"
export MAIL_PORT="${MAIL_PORT:-465}"
export MAIL_SMTP_AUTH="${MAIL_SMTP_AUTH:-true}"
export MAIL_SMTP_SSL_ENABLE="${MAIL_SMTP_SSL_ENABLE:-true}"
export MAIL_SMTP_STARTTLS_ENABLE="${MAIL_SMTP_STARTTLS_ENABLE:-false}"
export ALERT_MAIL_FROM="${ALERT_MAIL_FROM:-$MAIL_USERNAME}"
export ALERT_MAIL_RECIPIENTS="${ALERT_MAIL_RECIPIENTS:-counselor-alerts@example.com}"

exec ./scripts/run-dev.sh
