import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

fun main() {
	val socket = DatagramSocket()
	val addr = InetAddress.getByName("255.255.255.255")
	val port = 6264
	val data = "随便一个字符串。".toByteArray()
	val packet = DatagramPacket(data, data.size, addr, port)
	socket.send(packet)
	val buffer = ByteArray(2048)
	packet.data = buffer
	packet.length = buffer.size
	socket.receive(packet)
	println(
		"${packet.address} : ${packet.port} --- ${
			String(packet.data, packet.offset, packet.length, Charsets.UTF_8)
		}"
	)
}

/*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

fun main() {
	val socket = Socket()
	socket.connect(
		InetSocketAddress(
			InetAddress.getByName("localhost"), 6266
		)
	)
	socket.getOutputStream().write("你好！".toByteArray(Charsets.UTF_8))
	val buffer = ByteArray(2048)
	val recLen = socket.getInputStream().read(buffer)
	println(String(buffer, 0, recLen, Charsets.UTF_8))
	socket.close()
}
*/
