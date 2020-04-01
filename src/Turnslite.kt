import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.lang.Long.max
import java.net.InetSocketAddress

class Turnslite(private val eventStorage: InetSocketAddress) {

    companion object {
        fun check(id: Long, currentTime: Long, eventStorage: InetSocketAddress): Boolean {
            val query = HashMap<String, String>()
            query["command"] = "FILTER"
            query["id"] = id.toString()
            val parser = JSONParser()
            val res = curl(eventStorage, query)
            val array = (parser.parse(res) as JSONObject)["answer"] as JSONArray
            var maxPayedTill: Long = 0
            for (event in array) {
                if (event is JSONObject && event["type"] == "payed") {
                    maxPayedTill = max(maxPayedTill, event["till"].toString().toLong())
                }
            }
            return maxPayedTill >= currentTime
        }
    }

    fun check(id: Long, currentTime: Long = System.currentTimeMillis()): Boolean {
        return check(id, currentTime, eventStorage)
    }

    fun attemptToEnter(id: Long, currentTime: Long = System.currentTimeMillis()): Boolean {
        if (check(id, currentTime)) {
            val query = event2map(EventEntered(currentTime, id))
            query["command"] = "PUSH"
            curl(eventStorage, query)
            return true
        }
        return false
    }

    fun attemptToLeave(id: Long, currentTime: Long = System.currentTimeMillis()) {
        val query = event2map(EventLeft(currentTime, id))
        query["command"] = "PUSH"
        curl(eventStorage, query)
    }
}