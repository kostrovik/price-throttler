package price.throttler

import price.throttler.processor.PriceThrottler

fun main() {
    val throttler = PriceThrottler()
    throttler.onPrice("EURUSD", 1.0)
}