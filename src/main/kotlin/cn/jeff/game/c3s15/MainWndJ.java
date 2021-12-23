package cn.jeff.game.c3s15;

import cn.jeff.game.c3s15.board.ChessBoard;
import javafx.scene.control.Label;

public class MainWndJ {

	MainWnd k;
	public ChessBoard chessBoard;
	public Label label01;
	public Label label02;
	public Label statusLabel;

	public void btnRestartClick() {
		k.btnRestartClick();
	}

	public void btnSetupClick() {
		k.btnSetupClick();
	}

}
