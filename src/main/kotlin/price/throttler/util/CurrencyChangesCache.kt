package price.throttler.util

import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class CurrencyChangesCache {
    private val currencyChanges = mutableMapOf<String, ConcurrentLinkedQueue<Pair<LocalDateTime, Double>>>()
    private var lastChange = mutableMapOf<String, LocalDateTime>()

    init {
        Timer("Currency cache cleaner", true).schedule(
            TimeUnit.SECONDS.toMillis(2),
            TimeUnit.SECONDS.toMillis(2)
        )
        {
            cleanCache()
        }
    }

    fun addChanges(currencyPair: String, rate: Double) {
        if (currencyChanges.containsKey(currencyPair)) {
            val map = currencyChanges[currencyPair]
            if (map == null) {
                currencyChanges[currencyPair] = addNewKey(currencyPair, rate)
            } else {
                val date = LocalDateTime.now()
                lastChange.put(currencyPair, date)
                map.add(Pair(date, rate))
            }
        } else {
            currencyChanges[currencyPair] = addNewKey(currencyPair, rate)
        }
    }

    fun getAvailableChanges(): Map<String, Pair<LocalDateTime, Double>> {
        return currencyChanges.map { currency ->
            val key = currency.key
            val value = currency.value.last()
            key to value
        }.toMap()
    }

    fun testLastChanges(): Map<String, LocalDateTime> {
        return lastChange
    }

    private fun addNewKey(currencyPair: String, rate: Double): ConcurrentLinkedQueue<Pair<LocalDateTime, Double>> {
        val date = LocalDateTime.now()
        lastChange.put(currencyPair, date)

        val list = ConcurrentLinkedQueue<Pair<LocalDateTime, Double>>()
        list.add(Pair(date, rate))
        return list
    }

    private fun cleanCache() {
        currencyChanges.forEach { (_, value) ->
            if (value.size > 1) {
                value.poll()
            }
        }
    }
}