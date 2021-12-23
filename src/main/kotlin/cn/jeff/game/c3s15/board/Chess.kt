package cn.jeff.game.c3s15.board

import cn.jeff.game.c3s15.GlobalVars
import javafx.scene.paint.Color

enum class Chess(val text: String, val color: Color) {

	EMPTY(" ", Color.BLACK) {
		override val oppositeSide = EMPTY
	},
	SOLDIER(GlobalVars.appConf.soldierText, Color.BLUE) {
		override val oppositeSide get() = CANNON
	},
	CANNON(GlobalVars.appConf.cannonText, Color.RED) {
		override val oppositeSide = SOLDIER
	},
	;

//	fun oppositeSide() {
//		when (this) {
//			EMPTY -> EMPTY
//			SOLDIER -> CANNON
//			CANNON -> SOLDIER
//		}
//	}

	abstract val oppositeSide: Chess

}
