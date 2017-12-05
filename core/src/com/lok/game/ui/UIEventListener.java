package com.lok.game.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;

public interface UIEventListener {
    public static enum UIEvent {
	UP,
	LEFT,
	RIGHT,
	DOWN,
	STOP, // no directional button pressed
	CAST,
	SELECT_ENTITY,
	CONVERSATION_CHOICE_SELECTED
    }

    public void onUIEvent(Actor triggerActor, UIEvent event);
}
