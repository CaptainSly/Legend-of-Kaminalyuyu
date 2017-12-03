package com.lok.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public final class Utils {
    private static final String	TAG  = Utils.class.getName();
    private static final Json	json = new Json();

    private Utils() {
    }

    public static String getLabel(String labelKey) {
	Gdx.app.debug(TAG, "Get localized label for key " + labelKey);
	return AssetManager.getManager().getAsset("localization/Labels", I18NBundle.class).get(labelKey);
    }

    public static <T> T fromJson(FileHandle file) {
	return json.fromJson(null, file);
    }

    public static <T> T readJsonValue(Class<T> type, JsonValue jsonMap) {
	return json.readValue(type, jsonMap);
    }
}
