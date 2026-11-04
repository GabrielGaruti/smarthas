#!/usr/bin/env bash
# ============================================================
# Smart HAS - smoke test pos-deploy (Fase 7)
# Exercita a API implantada de ponta a ponta e grava um relatorio
# Markdown. Falha (exit 1) se qualquer verificacao falhar.
#   ci/smoke-test.sh http://localhost:8080 relatorio.md
# ============================================================
set -uo pipefail

BASE="${1:-http://localhost:8080}"
REPORT="${2:-smoke-report.md}"
FAILS=0

echo "# Smoke test - Smart HAS API" > "$REPORT"
echo "" >> "$REPORT"
echo "Alvo: \`$BASE\` - $(date -u '+%Y-%m-%d %H:%M:%S UTC')" >> "$REPORT"
echo "" >> "$REPORT"
echo "| # | Verificacao | Resultado | Detalhe |" >> "$REPORT"
echo "|---|---|---|---|" >> "$REPORT"
N=0

check() {   # check "descricao" "condicao-bash" "detalhe"
  N=$((N + 1))
  if eval "$2"; then
    echo "| $N | $1 | OK | $3 |" >> "$REPORT"; echo "[OK]   $1 - $3"
  else
    echo "| $N | $1 | FALHOU | $3 |" >> "$REPORT"; echo "[FAIL] $1 - $3"; FAILS=$((FAILS + 1))
  fi
}

login() {
  curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' \
       -d "{\"email\":\"$1\",\"password\":\"$2\"}" | jq -r '.token // empty'
}

HEALTH=$(curl -s "$BASE/health" | jq -r '.status // empty')
check "Healthcheck /health" '[ "$HEALTH" = "ok" ]' "status=$HEALTH"

DOCS=$(curl -s -o /dev/null -w '%{http_code}' "$BASE/v3/api-docs")
check "Documentacao OpenAPI" '[ "$DOCS" = "200" ]' "HTTP $DOCS"

ADMIN=$(login admin@smarthas.com admin123)
PATIENT=$(login paciente@smarthas.com 123456)
check "Login ADMIN (JWT)" '[ -n "$ADMIN" ]' "token ${#ADMIN} caracteres"
check "Login paciente (JWT)" '[ -n "$PATIENT" ]' "token ${#PATIENT} caracteres"

TODAY=$(date -u +%Y-%m-%d)
MEAS=$(curl -s -X POST "$BASE/measurements" -H "Authorization: Bearer $PATIENT" -H 'Content-Type: application/json' \
       -d "{\"systolic\":186,\"diastolic\":121,\"date\":\"$TODAY\",\"time\":\"07:30\",\"notes\":\"smoke test do pipeline\"}")
SEV=$(echo "$MEAS" | jq -r '.alertSeverity // empty')
check "Medicao critica -> PKG_SHAS_ALERTAS.PRC_REGISTRAR_ALERTA" '[ "$SEV" = "CRITICO" ]' "alertSeverity=$SEV"

REC=$(curl -s "$BASE/recommendations" -H "Authorization: Bearer $PATIENT")
ENGINE=$(echo "$REC" | jq -r '.rulesEngine // empty')
check "Motor de regras em uso" '[ "$ENGINE" = "${EXPECTED_ENGINE:-ORACLE_PLSQL}" ]' "rulesEngine=$ENGINE"

SEARCH=$(curl -s "$BASE/admin/measurements/search?classification=HYPERTENSION&orderBy=SISTOLICA&limit=5" \
         -H "Authorization: Bearer $ADMIN")
TOTAL=$(echo "$SEARCH" | jq -r '.total // 0')
QUERY=$(echo "$SEARCH" | jq -r '.generatedQuery // empty')
check "SQL dinamico (PRC_CONSULTAR_MEDICOES)" '[ "$TOTAL" -gt 0 ]' "total=$TOTAL"

STATS=$(curl -s "$BASE/admin/measurements/stats?dimension=ORIGEM" -H "Authorization: Bearer $ADMIN" | jq 'length')
check "GROUP BY dinamico (FN_ESTATISTICA_POR_DIMENSAO)" '[ "${STATS:-0}" -gt 0 ]' "grupos=$STATS"

DENIED=$(curl -s -o /dev/null -w '%{http_code}' "$BASE/admin/measurements/search" -H "Authorization: Bearer $PATIENT")
check "Rota ADMIN bloqueada para paciente" '[ "$DENIED" = "403" ]' "HTTP $DENIED"

REPORTS=$(curl -s -X POST "$BASE/admin/reports/summary?days=30" -H "Authorization: Bearer $ADMIN" | jq -r '.generated // 0')
check "Relatorio (PKG_SHAS_RELATORIOS)" '[ "$REPORTS" -gt 0 ]' "relatorios=$REPORTS"

ALERT_ID=$(curl -s "$BASE/alerts" -H "Authorization: Bearer $PATIENT" | jq -r '[.[] | select(.severity=="CRITICO")][0].id // empty')
AUDIT=$(curl -s "$BASE/admin/alerts/$ALERT_ID/audit" -H "Authorization: Bearer $ADMIN" | jq 'length')
if [ "${EXPECTED_ENGINE:-ORACLE_PLSQL}" = "ORACLE_PLSQL" ]; then
  check "Auditoria por trigger (TRG_SHAS_ALERTA_AUDITORIA)" '[ "${AUDIT:-0}" -ge 1 ]' "alerta=$ALERT_ID registros=$AUDIT"
fi

{
  echo ""
  echo "SQL gerado pela procedure de busca dinamica:"
  echo ""
  echo '```sql'
  echo "$QUERY"
  echo '```'
  echo ""
  echo "**Resultado: $((N - FAILS)) de $N verificacoes OK.**"
} >> "$REPORT"

[ "$FAILS" -eq 0 ] || { echo "$FAILS verificacao(oes) falharam"; exit 1; }
