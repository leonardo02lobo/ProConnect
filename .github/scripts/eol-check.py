#!/usr/bin/env python3
"""Alerta temprana de fin de soporte OSS de Spring Boot (upgrade-spring-boot-4, spec platform-support).

Lee la versión de spring-boot-starter-parent de cada pom, consulta endoflife.date y falla si alguna línea
(p. ej. 4.1) termina su soporte OSS en menos de EOL_MIN_DAYS días (60 por defecto) o ya terminó.

  python3 .github/scripts/eol-check.py [--today YYYY-MM-DD] [--min-days N] [--data releases.json] pom.xml...

--data permite probarlo sin red con un JSON guardado de https://endoflife.date/api/v1/products/spring-boot/.
"""
import argparse
import datetime as dt
import json
import os
import re
import sys
import urllib.request

API = "https://endoflife.date/api/v1/products/spring-boot/"
PARENT = re.compile(
    r"<parent>\s*<groupId>org\.springframework\.boot</groupId>\s*"
    r"<artifactId>spring-boot-starter-parent</artifactId>\s*<version>([^<]+)</version>",
    re.S,
)


def boot_version(pom):
    with open(pom, encoding="utf-8") as f:
        m = PARENT.search(f.read())
    if not m:
        sys.exit(f"::error file={pom}::no se encontró spring-boot-starter-parent")
    return m.group(1).strip()


def load_releases(data_file):
    if data_file:
        with open(data_file, encoding="utf-8") as f:
            body = json.load(f)
    else:
        req = urllib.request.Request(API, headers={"User-Agent": "proconnect-eol-check"})
        with urllib.request.urlopen(req, timeout=30) as res:
            body = json.load(res)
    return body["result"]["releases"]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("poms", nargs="+")
    ap.add_argument("--today", default=None)
    ap.add_argument("--min-days", type=int, default=int(os.environ.get("EOL_MIN_DAYS", "60")))
    ap.add_argument("--data", default=None)
    args = ap.parse_args()

    today = dt.date.fromisoformat(args.today) if args.today else dt.date.today()
    releases = {r["name"]: r for r in load_releases(args.data)}
    # Destino recomendado: la línea publicada con el fin de soporte OSS más lejano, si supera el umbral.
    horizon = today + dt.timedelta(days=args.min_days)
    supported = [
        r
        for r in releases.values()
        if r.get("eolFrom") and dt.date.fromisoformat(r["eolFrom"]) >= horizon
    ]
    target = max(supported, key=lambda r: r["eolFrom"]) if supported else None
    target_txt = (
        f"{target['name']} (última {target['latest']['name']}, soporte OSS hasta {target['eolFrom']})"
        if target
        else "la siguiente línea GA (aún no publicada en endoflife.date: revisa spring.io/projects/spring-boot)"
    )

    failed = False
    rows = []
    for pom in args.poms:
        version = boot_version(pom)
        line = ".".join(version.split(".")[:2])
        rel = releases.get(line)
        if rel is None or not rel.get("eolFrom"):
            print(f"::error file={pom}::Spring Boot {version}: la línea {line} no aparece en endoflife.date")
            failed = True
            continue
        eol = dt.date.fromisoformat(rel["eolFrom"])
        days = (eol - today).days
        status = "OK"
        if days < args.min_days:
            failed = True
            status = "FALLA"
            when = "terminó" if days < 0 else "termina"
            print(
                f"::error file={pom}::Spring Boot {line} ({version}) {when} su soporte OSS el {eol} "
                f"({days} días; umbral {args.min_days}). Migra a Spring Boot {target_txt}."
            )
        rows.append(f"| `{pom}` | {version} | {line} | {eol} | {days} | {status} |")
        print(f"{pom}: Spring Boot {version} → línea {line}, fin de soporte OSS {eol} ({days} días): {status}")

    summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a", encoding="utf-8") as f:
            f.write("## Fin de soporte de Spring Boot\n\n")
            f.write(f"Umbral: {args.min_days} días · fecha: {today} · destino recomendado: {target_txt}\n\n")
            f.write("| pom | versión | línea | fin OSS | días | estado |\n|---|---|---|---|---|---|\n")
            f.write("\n".join(rows) + "\n")
    print(f"Destino recomendado: Spring Boot {target_txt}")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
