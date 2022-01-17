package cn.jeff.game.c3s15.net

/**
 * # 链路数据包
 *
 * 由[MqttLink]封装一层数据包，目的是要达到可靠传输，并维护连接的有效性。
 */
data class LinkPacket(
	val packetType: PacketType,
	val localId: String,
	val remoteId: String,
	val data: String
) {
	enum class PacketType {
		CONNECT,
		CONN_ACK,
		CONNECTED,
		HEARTBEAT,
		DATA,
	}
}
