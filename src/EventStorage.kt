import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.io.InputStream
import java.net.InetSocketAddress

class EventStorage(private val reportServer: InetSocketAddress) : Server() {

    private val storage = ArrayList<Event>()

    override fun process(query: Map<String, String>, input: InputStream): String? {
        return when (query["command"]) {
            "PUSH" -> {
                storage.add(json2event(JSONObject(query))!!)
                "pushed in EventStorage & " + curl(reportServer, query)
            }
            "FILTER" -> {
                val res = JSONObject()
                val arr = JSONArray()
                val id = query["id"]?.toLong() ?: return "id was not provided"
                for (event in storage) {
                    if (id == event.id) {
                        arr.add(JSONObject(event2map(event)))
                    }
                }
                res["answer"] = arr
                res.toString()
            }
            else -> {
                "unknown command"
            }
        }
    }
}