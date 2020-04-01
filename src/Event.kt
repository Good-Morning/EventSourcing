import org.json.simple.*

open class Event(val timestamp: Long, val id: Long)


class   EventPayed(val till: Long,
                   timestamp: Long, id: Long) : Event(timestamp, id)
class EventEntered(timestamp: Long, id: Long) : Event(timestamp, id)
class    EventLeft(timestamp: Long, id: Long) : Event(timestamp, id)

fun event2map(event: Event): MutableMap<String, String> {
    val builder = HashMap<String, String>()
    builder["timestamp"] = event.timestamp.toString()
    builder["id"] = event.id.toString()
    when (event) {
        is EventPayed -> {
            builder["type"] = "payed"
            builder["till"] = event.till.toString()
        }
        is EventEntered   -> builder["type"] = "entered"
        is EventLeft      -> builder["type"] = "left"
    }
    return builder
}

fun JSONObject.getLong(id: String) = (this[id] as String).toLong()

fun json2event(obj: JSONObject): Event? {
    val timestamp = obj.getLong("timestamp")
    val id = obj.getLong("id")
    return when (obj["type"] as String) {
        "payed"   -> EventPayed(obj.getLong("till"), timestamp, id)
        "entered" -> EventEntered(timestamp, id)
        "left"    -> EventLeft(timestamp, id)
        else -> {
            println("null")
            null
        }
    }
}
