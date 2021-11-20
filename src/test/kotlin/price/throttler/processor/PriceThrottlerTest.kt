package price.throttler.processor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class PriceThrottlerTest {

    @Test
    fun checkFastProcessor() {
        val throttler = PriceThrottler()
        val fastProcessor = TestFastProcessor()
        throttler.subscribe(fastProcessor)

        val course = Random.nextDouble(0.1, 10.0)
        throttler.onPrice("USDRUB", course)

        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        assertEquals(course, fastProcessor.course.get("USDRUB"))
        assertEquals(1, fastProcessor.callsList.size)
    }

    @Test
    fun checkSlowProcessor() {
        val throttler = PriceThrottler()
        val slowProcessor = TestSlowProcessor()
        throttler.subscribe(slowProcessor)

        val course = Random.nextDouble(0.1, 10.0)
        throttler.onPrice("USDRUB", course)

        while (throttler.isActive()) {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1))
        }

        assertEquals(course, slowProcessor.course.get("USDRUB"))
        assertEquals(1, slowProcessor.calls)
    }

    @Test
    fun checkSlowProcessor2() {
        val throttler = PriceThrottler()
        val slowProcessor = TestSlowProcessor()
        throttler.subscribe(slowProcessor)

        for (i in 0..5) {
            throttler.onPrice("USDRUB", Random.nextDouble(0.1, 10.0))
        }
        val course = Random.nextDouble(0.1, 10.0)
        throttler.onPrice("USDRUB", course)

        while (throttler.isActive()) {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1))
        }

        assertEquals(course, slowProcessor.course.get("USDRUB"))
        assertEquals(2, slowProcessor.calls)
    }

    @Test
    fun checkBothProcessors() {
        val throttler = PriceThrottler()
        val fastProcessor = TestFastProcessor()
        throttler.subscribe(fastProcessor)
        val slowProcessor = TestSlowProcessor()
        throttler.subscribe(slowProcessor)

        val rates = mutableListOf<Double>()
        var course = 0.0

        val start = System.nanoTime()

        val job = CoroutineScope(Dispatchers.IO).launch {
            for (i in 1..5) {
                val rate = Random.nextDouble(0.1, 10.0)
                rates.add(rate)
                throttler.onPrice("USDRUB", rate)
                delay(TimeUnit.MICROSECONDS.toMicros(100))
            }

            course = Random.nextDouble(0.1, 10.0)
            rates.add(course)
            throttler.onPrice("USDRUB", course)
        }

        while (job.isActive || throttler.isActive()) {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1))
        }

        val finish = System.nanoTime()

        val duration = TimeUnit.SECONDS.convert(finish - start, TimeUnit.NANOSECONDS)
        println("test duration $duration")

        assertEquals(course, fastProcessor.course.get("USDRUB"))
        assertEquals(6, fastProcessor.callsList.size)

        assertEquals(course, slowProcessor.course.get("USDRUB"))
        assertEquals(2, slowProcessor.calls)
    }
}