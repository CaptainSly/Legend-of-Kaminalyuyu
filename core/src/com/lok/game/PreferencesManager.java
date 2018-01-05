package com.lok.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public class PreferencesManager {
    private static final String	      TAG      = PreferencesManager.class.getSimpleName();
    private static PreferencesManager instance = null;

    public static interface PreferencesListener {
	public void onSave(Json json, Preferences preferences);

	public void onLoad(Json json, Preferences preferences);
    }

    private final Preferences		     gameStatePreferences;
    private final Array<PreferencesListener> listeners;
    private final Json			     json;

    private PreferencesManager() {
	gameStatePreferences = Gdx.app.getPreferences("lok-gamestate");
	listeners = new Array<PreferencesListener>();
	json = new Json();
    }

    public static PreferencesManager getManager() {
	if (instance == null) {
	    instance = new PreferencesManager();
	}
	return instance;
    }

    public void addPreferencesListener(PreferencesListener listener) {
	listeners.add(listener);
    }

    public void removePreferencesListener(PreferencesListener listener) {
	listeners.removeValue(listener, false);
    }

    public void saveGameState() {
	Gdx.app.debug(TAG, "Saving gamestate preferences");

	for (PreferencesListener listener : listeners) {
	    listener.onSave(json, gameStatePreferences);
	}
	gameStatePreferences.flush();
    }

    public void loadGameState() {
	Gdx.app.debug(TAG, "Loading gamestate preferences");

	for (PreferencesListener listener : listeners) {
	    listener.onLoad(json, gameStatePreferences);
	}
    }
}
