package com.lok.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Array;
import com.lok.game.ui.UIEventListener.UIEvent;

public class Touchpad extends WidgetGroup {
    public static enum TouchpadDirection {
	UP(UIEvent.UP),
	LEFT(UIEvent.LEFT),
	RIGHT(UIEvent.RIGHT),
	DOWN(UIEvent.DOWN);

	private final UIEvent uiEvent;

	private TouchpadDirection(UIEvent uiEvent) {
	    this.uiEvent = uiEvent;
	}

	public UIEvent getUIEvent() {
	    return uiEvent;
	}
    }

    private final Array<ImageButton> btn_directions;

    public Touchpad(Skin skin) {
	super();

	btn_directions = new Array<ImageButton>(TouchpadDirection.values().length);
	for (TouchpadDirection direction : TouchpadDirection.values()) {
	    final ImageButton btn = new ImageButton(skin, "move_" + direction.name().toLowerCase());
	    btn.setUserObject(direction);
	    btn_directions.add(btn);
	    addActor(btn);
	}

	btn_directions.get(TouchpadDirection.UP.ordinal()).setPosition(50, 85);
	btn_directions.get(TouchpadDirection.LEFT.ordinal()).setPosition(0, 50);
	btn_directions.get(TouchpadDirection.RIGHT.ordinal()).setPosition(85, 50);
	btn_directions.get(TouchpadDirection.DOWN.ordinal()).setPosition(50, 0);
    }

    public void uncheckAll() {
	for (ImageButton btn : btn_directions) {
	    btn.setChecked(false);
	}
    }

    public void setChecked(TouchpadDirection direction, boolean checked) {
	btn_directions.get(direction.ordinal()).setChecked(checked);
    }

    public boolean contains(ImageButton imgButton) {
	for (ImageButton btn : btn_directions) {
	    if (btn.equals(imgButton)) {
		return true;
	    }
	}

	return false;
    }

    public TouchpadDirection getCurrentDirection() {
	for (TouchpadDirection direction : TouchpadDirection.values()) {
	    if (btn_directions.get(direction.ordinal()).isChecked()) {
		return direction;
	    }
	}

	return null;
    }
}
