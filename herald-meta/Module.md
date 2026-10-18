# Module herald-meta

The Meta adapter, over an `AppEventsLogger` the app creates. It sends events only, since Meta keeps
no user attributes: conversions under Meta's standard names, and purchases through
`MetaPurchaseEvent`, which the app maps its events to. Meta sends nothing from Herald until consent.

See the [Meta page](https://mkhytarmkhoian.github.io/herald-docs/vendors/meta/).
