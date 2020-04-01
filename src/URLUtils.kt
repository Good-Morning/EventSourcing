import java.io.InputStream
import java.net.InetSocketAddress
import java.net.URI
import java.net.URL
import java.nio.charset.Charset

fun queriedURI(address: InetSocketAddress, query: Map<String, String>): URI {
    val uri = URL("http", address.hostString, address.port, "/").toURI()
    val queryString = query.toList().joinToString(separator = "&") { it.first + "=" + it.second }
    return URI(uri.scheme, uri.userInfo, uri.host, uri.port, uri.path, queryString, uri.fragment)
}

fun fullyReadStream(stream: InputStream): String {
    val stringBuilder = StringBuilder()
    val buffer = ByteArray(4096)
    var read = stream.read(buffer)
    while (read > 0) {
        stringBuilder.append(String(buffer, 0, read, Charset.defaultCharset()))
        read = stream.read(buffer)
    }
    return stringBuilder.toString()
}

fun curl(address: InetSocketAddress, query: Map<String, String>): String {
    return fullyReadStream(queriedURI(address, query).toURL().openStream())
}
