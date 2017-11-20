package com.lok.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.lok.game.AssetManager;

public class PlayerHUD extends InputAdapter implements EventListener {
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

    private final Stage			  stage;
    private final GameUI		  gameUI;

    private final Array<HUDEventListener> listeners;

    public PlayerHUD() {
	this.listeners = new Array<HUDEventListener>();

	this.stage = new Stage(new FitViewport(1280,720));

	Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));

	final Skin skin = AssetManager.getManager().getAsset("ui/ui.json", Skin.class, new SkinLoader.SkinParameter("ui/ui.atlas"));

	this.gameUI = new GameUI(skin);
	gameUI.getButtonBackToDown().addListener(this);
	gameUI.getButtonMoveDown().addListener(this);
	gameUI.getButtonMoveLeft().addListener(this);
	gameUI.getButtonMoveRight().addListener(this);
	gameUI.getButtonMoveUp().addListener(this);
	stage.addActor(gameUI.getTable());
    }

    public void addPlayerHUDListener(HUDEventListener listener) {
	listeners.add(listener);
    }

    public void removePlayerHUDListener(HUDEventListener listener) {
	listeners.removeValue(listener, false);
    }

    public void resize(int width, int height) {
	stage.getViewport().update(width, height, true);
    }

    public void render(float deltaTime) {
	stage.act(deltaTime);
	stage.getViewport().apply();
	stage.draw();
    }

    public void dispose() {
	stage.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
	switch (keycode) {
	    case Keys.UP:
		gameUI.getButtonMoveUp().setChecked(true);
		for (HUDEventListener listener : listeners) {
		    listener.onHUDEvent(HUDEvent.MOVE_UP);
		}
		return true;
	    case Keys.DOWN:
		gameUI.getButtonMoveDown().setChecked(true);
		for (HUDEventListener listener : listeners) {
		    listener.onHUDEvent(HUDEvent.MOVE_DOWN);
		}
		return true;
	    case Keys.LEFT:
		gameUI.getButtonMoveLeft().setChecked(true);
		for (HUDEventListener listener : listeners) {
		    listener.onHUDEvent(HUDEvent.MOVE_LEFT);
		}
		return true;
	    case Keys.RIGHT:
		gameUI.getButtonMoveRight().setChecked(true);
		for (HUDEventListener listener : listeners) {
		    listener.onHUDEvent(HUDEvent.MOVE_RIGHT);
		}
		return true;
	    case Keys.T:
		gameUI.getButtonBackToDown().toggle();
		for (HUDEventListener listener : listeners) {
		    listener.onHUDEvent(HUDEvent.PORT_TO_TOWN);
		}
		return true;
	}

	return false;
    }

    @Override
    public boolean keyUp(int keycode) {
	switch (keycode) {
	    case Keys.UP:
	    case Keys.DOWN:
	    case Keys.LEFT:
	    case Keys.RIGHT:
		gameUI.getButtonMoveUp().setChecked(false);
		gameUI.getButtonMoveDown().setChecked(false);
		gameUI.getButtonMoveLeft().setChecked(false);
		gameUI.getButtonMoveRight().setChecked(false);
		if (Gdx.input.isKeyPressed(Keys.UP)) {
		    gameUI.getButtonMoveUp().setChecked(true);
		    for (HUDEventListener listener : listeners) {
			listener.onHUDEvent(HUDEvent.MOVE_UP);
		    }
		    return true;
		}
		if (Gdx.input.isKeyPressed(Keys.DOWN)) {
		    gameUI.getButtonMoveDown().setChecked(true);
		    for (HUDEventListener listener : listeners) {
			listener.onHUDEvent(HUDEvent.MOVE_DOWN);
		    }
		    return true;
		}
		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
		    gameUI.getButtonMoveLeft().setChecked(true);
		    for (HUDEventListener listener : listeners) {
			listener.onHUDEvent(HUDEvent.MOVE_LEFT);
		    }
		    return true;
		}
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
		    gameUI.getButtonMoveRight().setChecked(true);
		    for (HUDEventListener listener : listeners) {
			listener.onHUDEvent(HUDEvent.MOVE_RIGHT);
		    }
		    return true;
		}

		for (HUDEventListener listener : listeners) {
		    listener.onHUDEvent(HUDEvent.MOVE_STOP);
		}
		return true;
	}

	return false;
    }

    @Override
    public boolean handle(Event event) {
	if (gameUI.getButtonMoveUp().equals(event.getTarget())) {
	    for (HUDEventListener listener : listeners) {
		listener.onHUDEvent(HUDEvent.MOVE_UP);
	    }
	    return true;
	} else if (gameUI.getButtonMoveDown().equals(event.getTarget())) {
	    for (HUDEventListener listener : listeners) {
		listener.onHUDEvent(HUDEvent.MOVE_DOWN);
	    }
	    return true;
	} else if (gameUI.getButtonMoveLeft().equals(event.getTarget())) {
	    for (HUDEventListener listener : listeners) {
		listener.onHUDEvent(HUDEvent.MOVE_LEFT);
	    }
	    return true;
	} else if (gameUI.getButtonMoveRight().equals(event.getTarget())) {
	    for (HUDEventListener listener : listeners) {
		listener.onHUDEvent(HUDEvent.MOVE_RIGHT);
	    }
	    return true;
	} else if (gameUI.getButtonBackToDown().equals(event.getTarget())) {
	    for (HUDEventListener listener : listeners) {
		listener.onHUDEvent(HUDEvent.PORT_TO_TOWN);
	    }
	    return true;
	}
	return false;
    }
}
