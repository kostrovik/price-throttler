package price.throttler.processor

class TestFastProcessor : PriceProcessor {

    val course = mutableMapOf<String, Double>()
    val callsList = mutableListOf<Double>()

    override fun onPrice(ccyPair: String, rate: Double) {
        course[ccyPair] = rate
        callsList.add(rate)
    }

    override fun subscribe(priceProcessor: PriceProcessor) {
        TODO("Not yet implemented")
    }

    override fun unsubscribe(priceProcessor: PriceProcessor) {
        TODO("Not yet implemented")
    }
}