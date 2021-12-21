package cn.jeff.game.c3s15;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class SetupDialogJ {

	public SetupDialog k;

	public ToggleGroup tgCannon;
	public ToggleGroup tgSoldier;

	public ToggleButton tb01;
	public ToggleButton tb02;
	public ToggleButton tb03;
	public ToggleButton tb04;

	public void confirmBtnClicked() {
		k.confirmBtnClicked();
	}

	public void cancelBtnClicked() {
		k.cancelBtnClicked();
	}

}
