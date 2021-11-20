package price.throttler.processor

import java.util.concurrent.TimeUnit

class TestSlowProcessor : PriceProcessor {

    val course = mutableMapOf<String, Double>()
    var calls = 0

    override fun onPrice(ccyPair: String, rate: Double) {
        Thread.sleep(TimeUnit.SECONDS.toMillis(3))
        course[ccyPair] = rate
        calls++
    }

    override fun subscribe(priceProcessor: PriceProcessor) {
        TODO("Not yet implemented")
    }

    override fun unsubscribe(priceProcessor: PriceProcessor) {
        TODO("Not yet implemented")
    }
}