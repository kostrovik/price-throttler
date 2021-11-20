package price.throttler.processor

interface PriceProcessor {
    /**
     * @param ccyPair - EURUSD, EURRUB, USDJPY - up to 200 different currency pairs
     * @param rate - any double rate like 1.12, 200.23 etc
     */
    fun onPrice(ccyPair: String, rate: Double)

    /**
     * @param priceProcessor - can be up to 200 subscribers
     */
    fun subscribe(priceProcessor: PriceProcessor)

    /**
     * @param priceProcessor
     */
    fun unsubscribe(priceProcessor: PriceProcessor)
}