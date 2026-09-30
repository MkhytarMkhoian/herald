# Module herald-appsflyer

The AppsFlyer adapter, over an `AppsFlyerLib` the app initialises. It sends events only, since
AppsFlyer keeps no user attributes: conversions under AppsFlyer's predefined names, and revenue
through `AppsFlyerPurchaseEvent`, `AppsFlyerSubscribeEvent` and `AppsFlyerAdRevenueEvent`, which
the app maps its events to. AppsFlyer starts on consent, not on start-up.

See the [AppsFlyer page](https://mkhytarmkhoian.github.io/herald/vendors/appsflyer/).
