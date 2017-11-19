package com.lok.game.ui;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;

public class GameUI {
    private final Table	 table;
    private final Button btn_MoveUp;
    private final Button btn_MoveLeft;
    private final Button btn_MoveRight;
    private final Button btn_MoveDown;
    private final Button btn_BackToDown;

    public GameUI(Skin skin) {
	this.table = new Table(skin);
	this.table.setFillParent(true);

	btn_MoveUp = new ImageButton(skin, "move_up");
	btn_MoveUp.setPosition(50, 85);

	btn_MoveLeft = new ImageButton(skin, "move_left");
	btn_MoveLeft.setPosition(0, 50);

	btn_MoveRight = new ImageButton(skin, "move_right");
	btn_MoveRight.setPosition(85, 50);

	btn_MoveDown = new ImageButton(skin, "move_down");
	btn_MoveDown.setPosition(50, 0);

	final WidgetGroup movementGroup = new WidgetGroup();
	movementGroup.addActor(btn_MoveUp);
	movementGroup.addActor(btn_MoveLeft);
	movementGroup.addActor(btn_MoveRight);
	movementGroup.addActor(btn_MoveDown);

	this.table.add(movementGroup).bottom().left().padLeft(20).padBottom(20);
	btn_BackToDown = new ImageButton(skin, "back-to-town");
	btn_BackToDown.setName("port");
	this.table.add(btn_BackToDown).expand().bottom().right().padRight(20).padBottom(20);

	this.table.setDebug(Gdx.app.getLogLevel() == Application.LOG_DEBUG);
    }

    public Table getTable() {
	return table;
    }

    public Button getButtonBackToDown() {
	return btn_BackToDown;
    }

    public Button getButtonMoveDown() {
	return btn_MoveDown;
    }

    public Button getButtonMoveLeft() {
	return btn_MoveLeft;
    }

    public Button getButtonMoveRight() {
	return btn_MoveRight;
    }

    public Button getButtonMoveUp() {
	return btn_MoveUp;
    }
}
