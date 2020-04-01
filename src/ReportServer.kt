import org.json.simple.JSONObject
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap

class ReportServer : Server() {

    inner class Info {
        var firstVisit: Long = 0
        var enter: Long = 0
        var visitDuration: Long = 0
        val visits = ArrayList<Pair<Long, Long>>()

        fun enters(time: Long) {
            if (firstVisit == 0L) {
                firstVisit = time
            }
            enter = time
        }

        fun leaves(time: Long) {
            visitDuration += time - enter
            visits.add(enter to time)
        }
    }

    inner class Report(
        val userId: Long,
        val frequency: Double,
        val visits: Int,
        val firstVisit: Long,
        val lastVisit: Long
    )

    private val map = ConcurrentHashMap<Long, Info>()

    fun getReport(id: Long, currentTime: Long): Report {
        val info = map[id]!!
        if (info.enter > currentTime || info.visits.isNotEmpty() && info.visits.last().second > currentTime) {
            throw IllegalArgumentException("currentTime is out of date")
        }
        val freq = info.visitDuration.toDouble() / (currentTime - info.firstVisit)
        return Report(id, freq, info.visits.size, info.firstVisit, info.enter)
    }

    override fun process(query: Map<String, String>, input: InputStream): String? {
        val id = query["id"]?.toLong() ?: return "id was not provided"
        val info = map[id] ?: Info().apply {map[id] = this}

        return when(query["command"]) {
            "PUSH" -> {
                val time = query["timestamp"]?.toLong() ?: return "timestamp was not provided"
                val event = json2event(JSONObject(query)) ?: return "parsing error"
                when (event) {
                    is EventPayed -> {}
                    is EventEntered -> info.enters(time)
                    is EventLeft -> info.leaves(time)
                    else -> return "unknown event"
                }
                "pushed in ReportServer"
            }
            else -> {
                "unknown command"
            }
        }
    }
}