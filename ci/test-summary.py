"""Smart HAS - resumo dos testes para o GitHub Actions (Fase 7).

Le os relatorios JUnit do Surefire e o CSV do JaCoCo e imprime uma tabela Markdown,
que o pipeline grava em $GITHUB_STEP_SUMMARY (aba "Summary" da execucao).

    python ci/test-summary.py "Titulo" backend-springboot/target
"""
import csv
import glob
import os
import sys
import xml.etree.ElementTree as ET

title = sys.argv[1] if len(sys.argv) > 1 else "Testes"
target = sys.argv[2] if len(sys.argv) > 2 else "target"

rows, totals = [], {"tests": 0, "failures": 0, "errors": 0, "skipped": 0, "time": 0.0}
for path in sorted(glob.glob(os.path.join(target, "surefire-reports", "TEST-*.xml"))):
    suite = ET.parse(path).getroot()
    data = {k: int(suite.get(k, 0)) for k in ("tests", "failures", "errors", "skipped")}
    data["time"] = float(suite.get("time", 0))
    for k in totals:
        totals[k] += data[k]
    status = "OK" if data["failures"] == 0 and data["errors"] == 0 else "FALHOU"
    rows.append((suite.get("name").rsplit(".", 1)[-1], data, status))

print(f"## {title}\n")
if not rows:
    print("Nenhum relatorio de teste encontrado.")
    sys.exit(0)

print("| Classe de teste | Testes | Falhas | Erros | Ignorados | Tempo (s) | Status |")
print("|---|---:|---:|---:|---:|---:|---|")
for name, d, status in rows:
    print(f"| {name} | {d['tests']} | {d['failures']} | {d['errors']} | {d['skipped']} | {d['time']:.1f} | {status} |")
t = totals
print(f"| **Total** | **{t['tests']}** | **{t['failures']}** | **{t['errors']}** | **{t['skipped']}** | **{t['time']:.1f}** | |")

jacoco = os.path.join(target, "site", "jacoco", "jacoco.csv")
if os.path.exists(jacoco):
    missed = covered = 0
    with open(jacoco, newline="") as f:
        for r in csv.DictReader(f):
            missed += int(r["LINE_MISSED"])
            covered += int(r["LINE_COVERED"])
    if missed + covered:
        print(f"\nCobertura de linhas (JaCoCo): **{covered * 100 / (missed + covered):.1f}%** "
              f"({covered} de {missed + covered} linhas)")
