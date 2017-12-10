package com.lok.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public final class Utils {
    private static final String	TAG  = Utils.class.getName();
    private static final Json	json = new Json();

    private Utils() {
    }

    public static void initializeCustomColors() {
	// rgba
	Colors.put("Highlight", new Color(0x4250f4ff));
	Colors.put("Thought", new Color(0x9fa2a3ff));
    }

    public static String getLabel(String labelKey) {
	Gdx.app.debug(TAG, "Get localized label for key " + labelKey);
	return AssetManager.getManager().getAsset("localization/Labels", I18NBundle.class).format(labelKey);
    }

    public static <T> T fromJson(FileHandle file) {
	return json.fromJson(null, file);
    }

    public static <T> T readJsonValue(Class<T> type, JsonValue jsonMap) {
	return json.readValue(type, jsonMap);
    }

    public static Skin getUISkin() {
	return AssetManager.getManager().getAsset("ui/ui.json", Skin.class, new SkinLoader.SkinParameter("ui/ui.atlas"));
    }
}
