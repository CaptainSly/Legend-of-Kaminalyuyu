package com.lok.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public final class Utils {
    private static final Json json = new Json();

    private Utils() {
    }

    public static <T> T fromJson(FileHandle file) {
	return json.fromJson(null, file);
    }

    public static <T> T readJsonValue(Class<T> type, JsonValue jsonMap) {
	return json.readValue(type, jsonMap);
    }

    public static AssetManager getAssetManager() {
	return ((LegendOfKaminalyuyu) Gdx.app.getApplicationListener()).getAssetManager();
    }

    public static String getLabel(String labelKey) {
	return ((LegendOfKaminalyuyu) Gdx.app.getApplicationListener()).getLabel(labelKey);
    }

    public static void setScreen(Class<? extends Screen> type) {
	((LegendOfKaminalyuyu) Gdx.app.getApplicationListener()).setScreen(type);
    }

    public static Skin getUISkin() {
	return ((LegendOfKaminalyuyu) Gdx.app.getApplicationListener()).getUISkin();
    }
}
