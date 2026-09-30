# Releasing

A release is a `v*` tag. Pushing it runs two workflows:

- **Publish** signs every module and releases it to Maven Central, under the tag's version
  without the `v`.
- **Docs** builds the website for that tag and deploys it to GitHub Pages, so the site always
  documents what Central serves.

## Steps

1. **Update `CHANGELOG.md`.**
    - Under `## Version X.Y.Z`, replace `_Unreleased_` with the release date, `_YYYY-MM-DD_`.
    - Check that every change a user would notice is listed, each starting with `New:`, `Fix:`,
      `Upgrade:` or `Breaking:`.
    - After the release, start the next version at the top with `_Unreleased_` under it.
2. **Set `version=X.Y.Z` in `gradle.properties`.** The tag decides the published version either
   way; this keeps local builds and source links honest about which release they describe.
3. **Update the version shown to readers.** Search `README.md` and `docs/` for the previous version
   and replace it in the install snippets.
4. **Check that everything passes:**

    ```bash
    ./gradlew build
    scripts/build_docs.sh
    ```

5. **Commit, tag and push:**

    ```bash
    git commit -am "Release X.Y.Z"
    git tag vX.Y.Z
    git push origin main vX.Y.Z
    ```

6. **Verify:**
    - Both workflows are green on the tag.
    - The new POM resolves on Maven Central. Central can take up to about 30 minutes to sync:
      `https://repo1.maven.org/maven2/io/github/mkhytarmkhoian/herald-core/X.Y.Z/`.
    - The [website](https://mkhytarmkhoian.github.io/herald/) shows the release in its changelog.
    - [Moove](https://github.com/MkhytarMkhoian/Moove) builds against `X.Y.Z` from Central.

A version on Central is permanent: it can never be replaced or deleted. When a release is wrong,
fix it forward with the next patch version.

## Fixing the docs between releases

The site deploys only from tags, so a fix merged to `main` waits for the next release. To publish a
docs-only fix sooner, run the **Docs** workflow manually on `main` (Actions → Docs → Run workflow).
Do this only while `main` documents nothing unreleased. Otherwise the site would describe features
that Central does not have yet.

## One-time setup

Each of these is set once per repository:

- **Repository secrets** for publishing:
    - `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD`, a Central Portal user token;
    - `SIGNING_KEY` and `SIGNING_KEY_PASSWORD`, an ASCII-armored GPG key and its passphrase. The key
      is published to `keyserver.ubuntu.com`.
- **Settings → Pages → Source: GitHub Actions**, so the Docs workflow can deploy.
