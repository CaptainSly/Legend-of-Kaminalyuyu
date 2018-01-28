package com.lok.game.ui;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.lok.game.ability.Ability.AbilityID;
import com.lok.game.map.Map;
import com.lok.game.ui.Touchpad.TouchpadDirection;
import com.lok.game.ui.UIEventListener.UIEvent;

public class GameUI extends ScreenUI {
    private final GameRenderer renderer;
    private final Touchpad     touchpad;
    private final Button       btn_townPortal;
    private final Bar	       abilityChannelBar;

    public GameUI(AssetManager assetManager, Skin skin) {
	super(assetManager, skin);

	renderer = new GameRenderer();

	touchpad = new Touchpad(skin);
	touchpad.setPosition(15, 15);
	touchpad.addListener(this);
	stage.addActor(touchpad);

	btn_townPortal = new Button(skin, "back-to-town");
	btn_townPortal.setPosition(1280 - 15 - btn_townPortal.getWidth(), 15);
	btn_townPortal.setUserObject(AbilityID.TOWNPORTAL);
	btn_townPortal.addListener(this);
	stage.addActor(btn_townPortal);

	abilityChannelBar = new Bar(skin, "", 300, false);
	abilityChannelBar.setPosition(500, 20);
	abilityChannelBar.setVisible(false);
	stage.addActor(abilityChannelBar);
    }

    @Override
    public void render(float alpha) {
	renderer.render(alpha);
	stage.getViewport().apply();
	stage.draw();
    }

    @Override
    public void resize(int width, int height) {
	renderer.resize(width, height);
	super.resize(width, height);
    }

    @Override
    public void hide() {
	btn_townPortal.setChecked(false);
	super.hide();
    }

    @Override
    public void dispose() {
	renderer.dispose();
	super.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
	switch (keycode) {
	    case Keys.UP:
		touchpad.setChecked(TouchpadDirection.UP, true);
		btn_townPortal.setChecked(false);
		return true;
	    case Keys.DOWN:
		touchpad.setChecked(TouchpadDirection.DOWN, true);
		btn_townPortal.setChecked(false);
		return true;
	    case Keys.LEFT:
		touchpad.setChecked(TouchpadDirection.LEFT, true);
		btn_townPortal.setChecked(false);
		return true;
	    case Keys.RIGHT:
		touchpad.setChecked(TouchpadDirection.RIGHT, true);
		btn_townPortal.setChecked(false);
		return true;
	    case Keys.ENTER:
		if (touchpad.getCurrentDirection() == null) {
		    // only cast when there is no movement
		    btn_townPortal.setChecked(true);
		}
		return true;
	    default:
		return false;
	}
    }

    public void showAbilityChannelBar(String text, float maxValue) {
	abilityChannelBar.reset(maxValue);
	abilityChannelBar.setText(text);
	abilityChannelBar.setVisible(true);
    }

    public void setAbilityChannelBarValue(float value) {
	abilityChannelBar.setValue(value);
    }

    public void hideAbilityChannelBar() {
	abilityChannelBar.setVisible(false);
    }

    @Override
    public boolean keyUp(int keycode) {
	switch (keycode) {
	    case Keys.UP:
		touchpad.setChecked(TouchpadDirection.UP, false);
		return true;
	    case Keys.DOWN:
		touchpad.setChecked(TouchpadDirection.DOWN, false);
		return true;
	    case Keys.LEFT:
		touchpad.setChecked(TouchpadDirection.LEFT, false);
		return true;
	    case Keys.RIGHT:
		touchpad.setChecked(TouchpadDirection.RIGHT, false);
		return true;
	    case Keys.ENTER:
		btn_townPortal.setChecked(false);
		return true;
	    default:
		return false;
	}
    }

    @Override
    public boolean handle(Event event) {
	if (event instanceof InputEvent) {
	    final InputEvent inputEvent = (InputEvent) event;
	    if (InputEvent.Type.exit.equals(inputEvent.getType()) || InputEvent.Type.touchUp.equals(inputEvent.getType())) {
		if (touchpad.contains(event.getTarget())) {
		    ((Button) event.getTarget()).setChecked(false);
		    return true;
		} else if (btn_townPortal.equals(event.getTarget())) {
		    btn_townPortal.setChecked(false);
		    return true;
		}
	    }
	}

	if (touchpad.contains(event.getTarget())) {
	    final Button button = (Button) event.getTarget();
	    if (button.isChecked() || button.isPressed()) {
		// change movement
		final TouchpadDirection direction = (TouchpadDirection) button.getUserObject();
		for (UIEventListener listener : uiEventListeners) {
		    listener.onUIEvent(button, direction.getUIEvent());
		}
	    } else {
		// go to previous movement
		final TouchpadDirection direction = touchpad.getCurrentDirection();
		if (direction == null) {
		    for (UIEventListener listener : uiEventListeners) {
			listener.onUIEvent(button, UIEvent.STOP_MOVEMENT);
		    }
		} else {
		    for (UIEventListener listener : uiEventListeners) {
			listener.onUIEvent(button, direction.getUIEvent());
		    }
		}
	    }

	    return true;
	}

	if (btn_townPortal.equals(event.getTarget())) {
	    if (btn_townPortal.isChecked() || btn_townPortal.isPressed()) {
		for (UIEventListener listener : uiEventListeners) {
		    listener.onUIEvent(btn_townPortal, UIEvent.CAST);
		}
	    } else {
		for (UIEventListener listener : uiEventListeners) {
		    listener.onUIEvent(btn_townPortal, UIEvent.STOP_CAST);
		}
	    }

	    return true;
	}

	return false;
    }

    public void lockCameraToEntity(Entity entity) {
	renderer.lockCameraToEntity(entity);
    }

    public void setMap(Map map) {
	renderer.setMap(map);
    }
}
