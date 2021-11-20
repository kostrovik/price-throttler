package price.throttler.processor

import price.throttler.util.CurrencyChangesCache
import price.throttler.util.NotifierCache
import java.time.LocalDateTime

class PriceThrottler : PriceProcessor {

    private val subscribers = mutableMapOf<PriceProcessor, MutableMap<String, LocalDateTime>>()
    private val changesCache = CurrencyChangesCache()
    private val notifierCache = NotifierCache()

    override fun onPrice(ccyPair: String, rate: Double) {
        changesCache.addChanges(ccyPair, rate)
        notifyProcessors()
    }

    override fun subscribe(priceProcessor: PriceProcessor) {
        subscribers[priceProcessor] = mutableMapOf()
    }

    override fun unsubscribe(priceProcessor: PriceProcessor) {
        subscribers.remove(priceProcessor)
    }

    fun isActive(): Boolean {
        return notifierCache.hasMessages()
    }

    private fun notifyProcessors() {
        val changes = changesCache.getAvailableChanges()

        subscribers.forEach { (subscriber, meta) ->
            changes.forEach { (cur, rateMeta) ->
                val updateMeta = meta[cur]
                if (updateMeta != null) {
                    if (updateMeta.isBefore(rateMeta.first)) {
                        notifierCache.send(subscriber, Pair(cur, rateMeta.second))
                        meta[cur] = rateMeta.first
                    }
                } else {
                    notifierCache.send(subscriber, Pair(cur, rateMeta.second))
                    meta[cur] = rateMeta.first
                }
            }
        }
    }
}