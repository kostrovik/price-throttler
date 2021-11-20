package price.throttler.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import price.throttler.processor.PriceProcessor
import java.util.concurrent.ConcurrentLinkedQueue

class NotifierCache {
    private val subscribersNotifications = mutableMapOf<PriceProcessor, ConcurrentLinkedQueue<Pair<String, Double>>>()
    private val subscribersProcesses = mutableMapOf<PriceProcessor, Job>()

    fun send(subscriber: PriceProcessor, message: Pair<String, Double>) {
        val notification = subscribersNotifications[subscriber]
        if (notification == null) {
            val list = ConcurrentLinkedQueue<Pair<String, Double>>()
            list.add(message)
            subscribersNotifications[subscriber] = list
        } else {
            notification.add(message)
        }

        val jobs = subscribersProcesses.get(subscriber)
        if (jobs == null || jobs.isCompleted || jobs.isCancelled) {
            poolMessages(subscriber)
        }
    }

    fun hasMessages(): Boolean {
        return subscribersNotifications.any { (_, value) -> value.isNotEmpty() } || subscribersProcesses.isNotEmpty()
    }

    private fun poolMessages(subscriber: PriceProcessor) {
        subscribersProcesses.remove(subscriber)
        val messages = subscribersNotifications[subscriber]
        if (messages?.isNotEmpty() == true) {
            val job = runProcess(subscriber)
            subscribersProcesses[subscriber] = job
            job.invokeOnCompletion { poolMessages(subscriber) }
        }
    }

    private fun runProcess(subscriber: PriceProcessor): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            val messages = subscribersNotifications[subscriber]
            messages?.runCatching {
                val msg = last()
                messages.clear()
                if (msg != null) {
                    subscriber.onPrice(msg.first, msg.second)
                }
            }
        }
    }
}