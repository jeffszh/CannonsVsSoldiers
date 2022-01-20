import java.net.DatagramPacket
import java.net.DatagramSocket

fun main() {
	val buffer = ByteArray(2048)
	val socket = DatagramSocket(6264)
	val packet = DatagramPacket(buffer, buffer.size)
	socket.receive(packet)
	val receivedText = String(buffer, 0, packet.length)
	println(receivedText)
	val reply = "你说：“$receivedText”"
	println(reply)
	val replyData = reply.toByteArray()
	packet.setData(replyData, 0, replyData.size)
	socket.send(packet)
}

/*
import java.net.ServerSocket

fun main() {
	val serverSocket = ServerSocket(6266)
	val socket = serverSocket.accept()
	val buffer = ByteArray(2048)
	val recLen = socket.getInputStream().read(buffer)
	println(String(buffer, 0, recLen, Charsets.UTF_8))
	val reply = "收到了。".toByteArray(Charsets.UTF_8)
	socket.getOutputStream().use { out ->
		out.write(reply)
		out.flush()
	}
	socket.close()
	serverSocket.close()
}
*/
