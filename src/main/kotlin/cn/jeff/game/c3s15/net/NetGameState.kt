package cn.jeff.game.c3s15.net

/**
 * 網戰狀態
 */
enum class NetGameState {

	/** 離線 */
	OFFLINE,

	/** 正在邀請 */
	INVITING,

	/** 等待邀請 */
	WAIT_INV,

	/** 輪到自己走棋 */
	LOCAL_TURN,

	/** 輪到對方走棋 */
	REMOTE_TURN,

	/** 分出勝負 */
	GAME_OVER,

	/** 掉線 */
	LOST_CONN,

}
