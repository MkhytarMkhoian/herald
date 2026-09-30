#!/usr/bin/env bash
#
# Builds the Herald website into site/, or serves it locally with live reload.
#
#   scripts/build_docs.sh          # build, failing on any broken link or missing snippet
#   scripts/build_docs.sh serve    # build the API reference once, then preview at localhost:8000
#
# The site is MkDocs with the Material theme; the API reference is Dokka. Pass extra Gradle
# arguments through GRADLE_ARGS, e.g. GRADLE_ARGS="-Pversion=1.1.0" so source links point at that
# release tag. CI installs the Python packages itself; locally they go into build/docs-venv.

set -euo pipefail

cd "$(dirname "$0")/.."

# The Android API reference, from every published module. The Flutter SDK lives in its own
# repository; its dartdoc output will sit next to this one under api/flutter/.
./gradlew :dokkaGeneratePublicationHtml ${GRADLE_ARGS:-}
rm -rf docs/api
mkdir -p docs/api
cp -R build/dokka/html docs/api/android

# Files GitHub wants at the repository root, published on the site too. CHANGELOG.md is the Android
# SDK's change log, under the site's Changelog tab. Links between these files become links between site pages; LICENSE stays on
# GitHub.
rewrite_links() {
  sed -e 's|](CHANGELOG\.md)|](changelog/android.md)|g' \
      -e 's|](CONTRIBUTING\.md)|](contributing.md)|g' \
      -e 's|](RELEASING\.md)|](https://github.com/MkhytarMkhoian/herald/blob/main/RELEASING.md)|g' \
      -e 's|](LICENSE)|](https://github.com/MkhytarMkhoian/herald/blob/main/LICENSE)|g'
}
mkdir -p docs/changelog
rewrite_links < CHANGELOG.md > docs/changelog/android.md
rewrite_links < CONTRIBUTING.md > docs/contributing.md

if ! command -v mkdocs > /dev/null; then
  if [ ! -x build/docs-venv/bin/mkdocs ]; then
    python3 -m venv build/docs-venv
    build/docs-venv/bin/pip install --quiet -r docs/requirements.txt
  fi
  PATH="$PWD/build/docs-venv/bin:$PATH"
fi

if [ "${1:-}" = "serve" ]; then
  mkdocs serve
else
  mkdocs build --strict
fi
