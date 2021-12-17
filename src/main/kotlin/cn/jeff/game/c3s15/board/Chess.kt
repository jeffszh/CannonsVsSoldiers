package cn.jeff.game.c3s15.board

import cn.jeff.game.c3s15.GlobalVars
import javafx.scene.paint.Color

enum class Chess(val text: String, val color: Color) {

	EMPTY(" ", Color.BLACK),
	SOLDIER(GlobalVars.appConf.soldierText, Color.BLUE),
	CANNON(GlobalVars.appConf.cannonText, Color.RED),

}
