package com.lok.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.lok.game.AssetManager;

public class PlayerHUD extends Stage implements EventListener {
    public static enum HUDEvent {
	MOVE_UP,
	MOVE_LEFT,
	MOVE_RIGHT,
	MOVE_DOWN,
	MOVE_STOP,
	PORT_TO_TOWN
    }

    public static interface HUDEventListener {
	public void onHUDEvent(HUDEvent event);
    }

    private final GameUI		  gameUI;

    private final Array<HUDEventListener> listeners;

    public PlayerHUD() {
	super(new FitViewport(1280, 720));

	this.listeners = new Array<HUDEventListener>();

	final Skin skin = AssetManager.getManager().getAsset("ui/ui.json", Skin.class, new SkinLoader.SkinParameter("ui/ui.atlas"));

	this.gameUI = new GameUI(skin);
	gameUI.getButtonBackToDown().addListener(this);
	gameUI.getButtonMoveDown().addListener(this);
	gameUI.getButtonMoveLeft().addListener(this);
	gameUI.getButtonMoveRight().addListener(this);
	gameUI.getButtonMoveUp().addListener(this);
	addActor(gameUI.getTable());
    }

    public void show() {
	gameUI.getButtonMoveUp().setChecked(false);
	gameUI.getButtonMoveDown().setChecked(false);
	gameUI.getButtonMoveLeft().setChecked(false);
	gameUI.getButtonMoveRight().setChecked(false);
	gameUI.getButtonBackToDown().setChecked(false);
	Gdx.input.setInputProcessor(this);
    }

    public void hide() {
	Gdx.input.setInputProcessor(null);
    }

    public void addPlayerHUDListener(HUDEventListener listener) {
	listeners.add(listener);
    }

    public void removePlayerHUDListener(HUDEventListener listener) {
	listeners.removeValue(listener, false);
    }

    public void resize(int width, int height) {
	getViewport().update(width, height, true);
    }

    public void render(float deltaTime) {
	act(deltaTime);
	getViewport().apply();
	draw();
    }

    @Override
    public void dispose() {
	super.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
	switch (keycode) {
	    case Keys.UP:
		gameUI.getButtonMoveUp().setChecked(true);
		return true;
	    case Keys.DOWN:
		gameUI.getButtonMoveDown().setChecked(true);
		return true;
	    case Keys.LEFT:
		gameUI.getButtonMoveLeft().setChecked(true);
		return true;
	    case Keys.RIGHT:
		gameUI.getButtonMoveRight().setChecked(true);
		return true;
	    case Keys.T:
		gameUI.getButtonBackToDown().setChecked(true);
		return true;
	}

	return false;
    }

    @Override
    public boolean keyUp(int keycode) {
	switch (keycode) {
	    case Keys.UP:
		gameUI.getButtonMoveUp().setChecked(false);
		return true;
	    case Keys.DOWN:
		gameUI.getButtonMoveDown().setChecked(false);
		return true;
	    case Keys.LEFT:
		gameUI.getButtonMoveLeft().setChecked(false);
		return true;
	    case Keys.RIGHT:
		gameUI.getButtonMoveRight().setChecked(false);
		return true;
	    case Keys.T:
		gameUI.getButtonBackToDown().setChecked(false);
		return true;
	}

	return false;
    }

    private void handleMovementReleaseEvent() {
	final HUDEvent eventToSend;

	if (gameUI.getButtonMoveUp().isChecked()) {
	    eventToSend = HUDEvent.MOVE_UP;
	} else if (gameUI.getButtonMoveDown().isChecked()) {
	    eventToSend = HUDEvent.MOVE_DOWN;
	} else if (gameUI.getButtonMoveLeft().isChecked()) {
	    eventToSend = HUDEvent.MOVE_LEFT;
	} else if (gameUI.getButtonMoveRight().isChecked()) {
	    eventToSend = HUDEvent.MOVE_RIGHT;
	} else {
	    eventToSend = HUDEvent.MOVE_STOP;
	}

	for (HUDEventListener listener : listeners) {
	    listener.onHUDEvent(eventToSend);
	}
    }

    @Override
    public boolean handle(Event event) {
	final Actor target = event.getTarget();
	if (gameUI.getButtonMoveUp().equals(target) && gameUI.getButtonMoveUp().isChecked()) {
	    for (HUDEventListener listener : listeners) {
		listener.onHUDEvent(HUDEvent.MOVE_UP);
	    }
	    return true;
	} else if (gameUI.getButtonMoveDown().equals(target) && gameUI.getButtonMoveDown().isChecked()) {
	    for (HUDEventListener listener : listeners) {
		listener.onHUDEvent(HUDEvent.MOVE_DOWN);
	    }
	    return true;
	} else if (gameUI.getButtonMoveLeft().equals(target) && gameUI.getButtonMoveLeft().isChecked()) {
	    for (HUDEventListener listener : listeners) {
		listener.onHUDEvent(HUDEvent.MOVE_LEFT);
	    }
	    return true;
	} else if (gameUI.getButtonMoveRight().equals(target) && gameUI.getButtonMoveRight().isChecked()) {
	    for (HUDEventListener listener : listeners) {
		listener.onHUDEvent(HUDEvent.MOVE_RIGHT);
	    }
	    return true;
	} else if (gameUI.getButtonMoveUp().equals(target) || gameUI.getButtonMoveDown().equals(target) || gameUI.getButtonMoveLeft().equals(target)
		|| gameUI.getButtonMoveRight().equals(target)) {
	    handleMovementReleaseEvent();
	    return true;
	}

	if (gameUI.getButtonBackToDown().equals(target) && gameUI.getButtonBackToDown().isChecked()) {
	    for (HUDEventListener listener : listeners) {
		listener.onHUDEvent(HUDEvent.MOVE_STOP);
		listener.onHUDEvent(HUDEvent.PORT_TO_TOWN);
	    }
	    return true;
	}

	return false;
    }
}
