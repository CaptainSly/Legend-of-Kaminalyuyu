package com.lok.game.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public class ScreenManager {
    private static final String	 TAG	  = ScreenManager.class.getName();
    private static ScreenManager instance = null;

    private Array<Screen>	 screenCache;

    private ScreenManager() {
	this.screenCache = null;
    }

    public static ScreenManager getManager() {
	if (instance == null) {
	    instance = new ScreenManager();
	}

	return instance;
    }

    public void setScreen(Class<? extends Screen> type) {
	if (type == null) {
	    Gdx.app.debug(TAG, "Changing to null screen");
	    ((Game) Gdx.app.getApplicationListener()).setScreen(null);
	    return;
	} else {
	    Gdx.app.debug(TAG, "Changing to screen: " + type.getName());
	}

	if (screenCache == null) {
	    screenCache = new Array<Screen>();
	}

	for (Screen screen : screenCache) {
	    if (type.isInstance(screen)) {
		((Game) Gdx.app.getApplicationListener()).setScreen(screen);
		return;
	    }
	}

	try {
	    final Screen screen = ClassReflection.newInstance(type);
	    screenCache.add(screen);
	    ((Game) Gdx.app.getApplicationListener()).setScreen(screen);
	} catch (ReflectionException e) {
	    throw new GdxRuntimeException("Could not create screen of type " + type, e);
	}
    }

    public void dispose() {
	Gdx.app.debug(TAG, "Disposing ScreenManager");
	if (screenCache != null) {
	    for (Screen screen : screenCache) {
		screen.hide();
		screen.dispose();
	    }
	}
    }
}
