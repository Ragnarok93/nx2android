"""Build a compact, title-specific Corresponding Source archive for per-game APKs."""
from pathlib import Path
import argparse
import shutil
import subprocess
import zipfile


_SKIP_DIRS = {".git", ".venv", "venv", "__pycache__", ".pytest_cache", ".mypy_cache",
              ".ruff_cache", "node_modules", "dist", "build", "out", ".idea", ".vscode"}
_SKIP_SUFFIXES = {".pyc", ".nsp", ".xci", ".nsz", ".xcz", ".apk", ".ipa", ".keystore",
                  ".jks", ".pem", ".p12", ".pfx"}


def normalize_title_id(value):
    try:
        return f"{int(value, 16) if isinstance(value, str) else int(value):016X}"
    except (TypeError, ValueError):
        return str(value).upper().removeprefix("0X").zfill(16)


def include_path(name, title):
    parts = name.split("/")
    base = parts[-1].lower()
    if base in {"prod.keys", "title.keys", ".env"} or base.startswith(".env."):
        return False
    if any(part in _SKIP_DIRS for part in parts):
        return False
    if name.startswith(("docs/images/", "nxnative/docs/images/")):
        return False
    if name.startswith("nxnative/dm/games/") and len(parts) > 4 and parts[3].upper() != title:
        return False
    if Path(name).suffix.lower() in _SKIP_SUFFIXES:
        return False
    return True


def build_source_archive(root, destination, title_id):
    root = Path(root).resolve()
    destination = Path(destination).resolve()
    title = normalize_title_id(title_id)
    destination.parent.mkdir(parents=True, exist_ok=True)
    git = shutil.which("git")
    temp = destination.with_name(destination.stem + ".unfiltered.zip")
    try:
        if git:
            try:
                subprocess.run([git, "-C", str(root), "archive", "--format=zip", "--output", str(temp), "HEAD"],
                               check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                with zipfile.ZipFile(temp) as source, zipfile.ZipFile(destination, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as out:
                    for info in source.infolist():
                        if info.is_dir() or not include_path(info.filename, title):
                            continue
                        out.writestr(info.filename, source.read(info.filename), compress_type=zipfile.ZIP_DEFLATED, compresslevel=6)
                return destination
            except (OSError, subprocess.CalledProcessError, zipfile.BadZipFile):
                temp.unlink(missing_ok=True)
        with zipfile.ZipFile(destination, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as out:
            for source in sorted(root.rglob("*")):
                if not source.is_file():
                    continue
                name = source.relative_to(root).as_posix()
                if include_path(name, title):
                    out.write(source, name)
        return destination
    finally:
        temp.unlink(missing_ok=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", default=".")
    parser.add_argument("--title-id", required=True)
    parser.add_argument("--out", required=True)
    args = parser.parse_args()
    result = build_source_archive(args.root, args.out, args.title_id)
    print(f"Corresponding source for {normalize_title_id(args.title_id)}: {result} ({result.stat().st_size:,} bytes)")


if __name__ == "__main__":
    main()
