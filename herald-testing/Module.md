# Module herald-testing

`FakeAnalyticsProvider`: a provider that records every call instead of sending it, with
assertions over what was recorded. Register it with a real `Herald` in tests, so they exercise the
same setup you ship.

See [Testing](https://mkhytarmkhoian.github.io/herald-docs/guides/testing/).
