package com.lok.game.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.lok.game.LegendOfKaminalyuyu;
import com.lok.game.serialization.PreferencesManager;
import com.lok.game.serialization.PreferencesManager.PreferencesListener;
import com.lok.game.ui.ScreenUI;
import com.lok.game.ui.UIEventListener;

public abstract class Screen<T extends ScreenUI> implements com.badlogic.gdx.Screen, UIEventListener, PreferencesListener {
    private float			accumulator;
    private final float			fixedPhysicsStep;
    protected final LegendOfKaminalyuyu	game;
    protected final AssetManager	assetManager;
    protected final T			screenUI;

    public Screen(LegendOfKaminalyuyu game, AssetManager assetManager, Class<T> screenUIType, Skin uiSkin) {
	this.game = game;
	this.assetManager = assetManager;
	this.fixedPhysicsStep = 1.0f / 30.0f; // physics run at constant 30 fps
	this.accumulator = 0.0f;
	try {
	    this.screenUI = screenUIType.cast(ClassReflection.getConstructor(screenUIType, AssetManager.class, Skin.class).newInstance(assetManager, uiSkin));
	} catch (ReflectionException e) {
	    throw new GdxRuntimeException("Could not create ScreenUI of type " + screenUIType, e);
	}
    }

    @Override
    public void show() {
	screenUI.addUIEventListener(this);
	screenUI.show();

	PreferencesManager.getManager().addPreferencesListener(this);
	PreferencesManager.getManager().loadGameState();
    }

    @Override
    public void render(float delta) {
	if (delta > 0.25f) {
	    delta = 0.25f;
	}

	accumulator += delta;
	while (accumulator >= fixedPhysicsStep) {
	    onUpdate(fixedPhysicsStep);
	    screenUI.update(fixedPhysicsStep);
	    accumulator -= fixedPhysicsStep;
	}

	screenUI.render(accumulator / fixedPhysicsStep);
    }

    public abstract void onUpdate(float fixedPhysicsStep);

    @Override
    public void resize(int width, int height) {
	screenUI.resize(width, height);
    }

    @Override
    public void pause() {
	// not needed
    }

    @Override
    public void resume() {
	// not needed
    }

    @Override
    public void hide() {
	PreferencesManager.getManager().saveGameState();
	PreferencesManager.getManager().removePreferencesListener(this);
	screenUI.hide();
	screenUI.removeUIEventListener(this);
    }

    @Override
    public void dispose() {
	screenUI.dispose();
    }
}
