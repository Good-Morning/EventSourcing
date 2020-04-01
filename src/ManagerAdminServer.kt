import java.io.InputStream
import java.net.InetSocketAddress

class ManagerAdminServer(private val eventStorage: InetSocketAddress) {

    fun payFor(id: Long, till: Long, currentTime: Long = System.currentTimeMillis()) {
        val query = event2map(EventPayed(till, currentTime, id))
        query["command"] = "PUSH"
        curl(eventStorage, query)
    }

    fun check(id: Long, currentTime: Long = System.currentTimeMillis()): Boolean {
        return Turnslite.check(id, currentTime, eventStorage)
    }
}