package com.lok.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.lok.game.AssetManager;
import com.lok.game.Utils;
import com.lok.game.ui.Touchpad.TouchpadDirection;
import com.lok.game.ui.UIEventListener.UIEvent;

public class GameUI implements EventListener {
    private final Stage			 stage;
    private final Skin			 skin;

    private final Touchpad		 touchpad;
    private final ImageButton		 btn_townPortal;

    private final Array<UIEventListener> uiEventListeners;

    public GameUI() {
	this.stage = new Stage(new FitViewport(1280, 720));
	this.skin = Utils.getUISkin();
	this.uiEventListeners = new Array<UIEventListener>();

	touchpad = new Touchpad(skin);
	touchpad.setPosition(15, 15);
	touchpad.addListener(this);
	stage.addActor(touchpad);

	btn_townPortal = new ImageButton(skin, "back-to-town");
	btn_townPortal.setPosition(1217, 15);
	btn_townPortal.addListener(this);
	stage.addActor(btn_townPortal);

	stage.setKeyboardFocus(touchpad);
    }

    public void addUIEventListener(UIEventListener listener) {
	this.uiEventListeners.add(listener);
    }

    public void removeUIEventListener(UIEventListener listener) {
	this.uiEventListeners.removeValue(listener, false);
    }

    public void show() {
	Gdx.input.setInputProcessor(stage);
	touchpad.uncheckAll();
	btn_townPortal.setChecked(false);
    }

    public void render(float delta) {
	stage.act(delta);
	stage.getViewport().apply();
	stage.draw();
    }

    public void resize(int width, int height) {
	stage.getViewport().update(width, height, true);
    }

    public void hide() {
	Gdx.input.setInputProcessor(null);
    }

    public void dispose() {
	stage.dispose();
	skin.dispose();
	AssetManager.getManager().dispose();
    }

    @Override
    public boolean handle(Event event) {
	if (event instanceof InputEvent) {
	    final InputEvent inputevent = (InputEvent) event;

	    switch (inputevent.getKeyCode()) {
		case Keys.UP:
		    touchpad.setChecked(TouchpadDirection.UP, Gdx.input.isKeyJustPressed(Keys.UP));
		    break;
		case Keys.DOWN:
		    touchpad.setChecked(TouchpadDirection.DOWN, Gdx.input.isKeyJustPressed(Keys.DOWN));
		    break;
		case Keys.LEFT:
		    touchpad.setChecked(TouchpadDirection.LEFT, Gdx.input.isKeyJustPressed(Keys.LEFT));
		    break;
		case Keys.RIGHT:
		    touchpad.setChecked(TouchpadDirection.RIGHT, Gdx.input.isKeyJustPressed(Keys.RIGHT));
		    break;
		case Keys.T:
		    btn_townPortal.setChecked(Gdx.input.isKeyJustPressed(Keys.T));
		    break;
		default:
		    return false;
	    }

	    return true;
	}

	if (event.getTarget() instanceof ImageButton) {
	    final ImageButton imgButton = (ImageButton) event.getTarget();

	    if (touchpad.contains(imgButton)) {
		if (imgButton.isChecked()) {
		    // change movement
		    final TouchpadDirection direction = (TouchpadDirection) imgButton.getUserObject();
		    for (UIEventListener listener : uiEventListeners) {
			listener.onUIEvent(imgButton, direction.getUIEvent());
		    }
		} else {
		    // go to previous movement
		    final TouchpadDirection direction = touchpad.getCurrentDirection();
		    if (direction == null) {
			for (UIEventListener listener : uiEventListeners) {
			    listener.onUIEvent(imgButton, UIEvent.STOP_MOVEMENT);
			}
		    } else {
			for (UIEventListener listener : uiEventListeners) {
			    listener.onUIEvent(imgButton, direction.getUIEvent());
			}
		    }
		}

		return true;
	    }

	    // townportal
	    if (imgButton.isChecked()) {
		for (UIEventListener listener : uiEventListeners) {
		    listener.onUIEvent(imgButton, UIEvent.CAST);
		}
	    } else {
		for (UIEventListener listener : uiEventListeners) {
		    listener.onUIEvent(imgButton, UIEvent.STOP_CAST);
		}
	    }

	    return true;
	}

	return false;
    }
}
