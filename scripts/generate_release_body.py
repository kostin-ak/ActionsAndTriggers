#!/usr/bin/env python3
"""Builds release_body.md from changelog.yaml for a given tag."""
import os
import sys

try:
    import yaml
except ImportError:
    print("::warning::PyYAML is not installed; using fallback release body")
    yaml = None


def write_fallback(message: str) -> None:
    print(f"::warning::{message}")
    tag = os.environ.get("GITHUB_REF_NAME", "unknown")
    version = os.environ.get("VERSION", tag.lstrip("v"))
    with open("release_body.md", "w", encoding="utf-8") as handle:
        handle.write(f"Release {tag}\n\nAutomated build of `{version}`.\n")
    sys.exit(0)


def main() -> None:
    if yaml is None:
        write_fallback("PyYAML is unavailable")

    tag = os.environ.get("GITHUB_REF_NAME", "")
    version = os.environ.get("VERSION") or tag.lstrip("v")

    try:
        with open("changelog.yaml", encoding="utf-8") as handle:
            data = yaml.safe_load(handle) or {}
    except FileNotFoundError:
        write_fallback("changelog.yaml not found")
    except yaml.YAMLError as exc:
        write_fallback(f"changelog.yaml parse error: {exc}")

    releases = data.get("releases") or []
    release = next((item for item in releases if str(item.get("version")) == version), None)
    if release is None:
        write_fallback(f"no release notes for {version}")

    lines = []
    if release.get("codename"):
        lines.append(f"### 🚀 Codename: **{release['codename']}**")
    if release.get("summary"):
        lines.append(str(release["summary"]).strip() + "\n")

    changes = release.get("changes") or {}
    for key, title in (
        ("breaking", "### ⚠️ Breaking Changes"),
        ("features", "### ✨ Features"),
        ("fixes", "### 🐛 Bug Fixes"),
    ):
        items = changes.get(key) or []
        if items:
            lines.append("")
            lines.append(title)
            lines.extend(f"- {item}" for item in items)

    compatibility = release.get("compatibility") or []
    if compatibility:
        lines.append("")
        lines.append("### 🧩 Compatibility")
        lines.extend(f"- {item}" for item in compatibility)

    sha = os.environ.get("GITHUB_SHA", "")[:7]
    workflow = os.environ.get("GITHUB_WORKFLOW", "GitHub Actions")
    lines.append("")
    lines.append(f"_Built from `{tag}` ({sha}) by {workflow}._")

    with open("release_body.md", "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines) + "\n")


if __name__ == "__main__":
    main()
