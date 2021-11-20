package price.throttler.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class CurrencyChangesCacheTest {

    @Test
    fun checkTimerNotCleanLastValue() {
        val cache = CurrencyChangesCache()
        for (i in 0..3) {
            cache.addChanges("USDRUB", Random.nextDouble(0.1, 10.0))
        }

        val results = cache.getAvailableChanges()

        Thread.sleep(TimeUnit.SECONDS.toMillis(10))

        var updated = cache.getAvailableChanges()

        assertEquals(results.get("USDRUB"), updated.get("USDRUB"))

        Thread.sleep(TimeUnit.SECONDS.toMillis(10))

        updated = cache.getAvailableChanges()

        assertEquals(results.get("USDRUB"), updated.get("USDRUB"))
    }

    @Test
    fun checkLastDate() {
        val cache = CurrencyChangesCache()
        for (i in 0..3) {
            cache.addChanges("USDRUB", Random.nextDouble(0.1, 10.0))
        }

        for (coroutines in 0..3) {
            CoroutineScope(Dispatchers.IO).launch {
                for (i in 0..3) {
                    cache.addChanges("USDRUB", Random.nextDouble(0.1, 10.0))
                }
            }
        }

        Thread.sleep(TimeUnit.SECONDS.toMillis(20))

        val result = cache.getAvailableChanges()
        val compare = cache.testLastChanges()

        assertEquals(result.get("USDRUB")!!.first, compare.get("USDRUB"))
    }
}