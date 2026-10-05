# Releasing

A release is a `v*` tag. Pushing it runs the **Publish** workflow, which signs every module and
releases it to Maven Central, under the tag's version without the `v`. Then it asks
[herald-docs](https://github.com/MkhytarMkhoian/herald-docs) to redeploy the website, which takes
its Kotlin samples, API reference and change log from the newest release tag.

## Steps

1. **Update `CHANGELOG.md`.**
    - Under `## Version X.Y.Z`, replace `_Unreleased_` with the release date, `_YYYY-MM-DD_`.
    - Check that every change a user would notice is listed, each starting with `New:`, `Fix:`,
      `Upgrade:` or `Breaking:`.
    - After the release, start the next version at the top with `_Unreleased_` under it.
2. **Set `version=X.Y.Z` in `gradle.properties`.** The tag decides the published version either
   way; this keeps local builds and source links honest about which release they describe.
3. **Update the version shown to readers.** Search `README.md` for the previous version and
   replace it in the install snippets. The website's install snippets change in herald-docs,
   in a pull request merged once the release is out.
4. **Check that everything passes:**

    ```bash
    ./gradlew build
    ```

5. **Commit, tag and push:**

    ```bash
    git commit -am "Release X.Y.Z"
    git tag vX.Y.Z
    git push origin main vX.Y.Z
    ```

6. **Verify:**
    - The Publish workflow is green on the tag, and the Docs workflow it starts in herald-docs too.
    - The new POM resolves on Maven Central. Central can take up to about 30 minutes to sync:
      `https://repo1.maven.org/maven2/io/github/mkhytarmkhoian/herald-core/X.Y.Z/`.
    - The [website](https://mkhytarmkhoian.github.io/herald-docs/) shows the release in its
      changelog.
    - [Moove](https://github.com/MkhytarMkhoian/Moove) builds against `X.Y.Z` from Central.

A version on Central is permanent: it can never be replaced or deleted. When a release is wrong,
fix it forward with the next patch version.

## One-time setup

Each of these is set once per repository:

- **Repository secrets** for publishing:
    - `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD`, a Central Portal user token;
    - `SIGNING_KEY` and `SIGNING_KEY_PASSWORD`, an ASCII-armored GPG key and its passphrase. The key
      is published to `keyserver.ubuntu.com`.
- **`DOCS_DISPATCH_TOKEN`**, a fine-grained personal access token that can start herald-docs'
  Docs workflow: repository access to herald-docs only, with the Contents permission set to read
  and write. The same token is in herald-flutter. It expires, so renew it before it does.
