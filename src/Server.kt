import com.sun.net.httpserver.HttpServer
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.InputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import java.net.URL
import java.nio.charset.Charset

abstract class Server {

    private val server: HttpServer = HttpServer.create(InetSocketAddress(InetAddress.getLocalHost(), 0), 0)

    protected val parser = JSONParser()

    fun shutdown() {
        server.stop(0)
    }

    fun getAddress() = server.address

    init {
        server.createContext("/") {
            val query = it.requestURI.query
            val queryMap = HashMap<String, String>()
            for (pair in query.split("&")) {
                val q = pair.split("=")
                queryMap[q[0]] = q[1]
            }
            val output = it.responseBody
            val rawResponse = process(queryMap, it.requestBody)
            val status = if (rawResponse != null) 200 else 400
            val response = rawResponse
                ?.toByteArray(Charset.defaultCharset())
                ?: ByteArray(0)
            it.sendResponseHeaders(status, response.size.toLong())
            output.write(response)
            output.close()
        }
        server.start()
        //println(server.address)
    }

    abstract fun process(query: Map<String, String>, input: InputStream): String?
}