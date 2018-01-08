package com.lok.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.lok.game.Animation;
import com.lok.game.Animation.AnimationID;
import com.lok.game.LegendOfKaminalyuyu;
import com.lok.game.Utils;
import com.lok.game.assets.loader.AnimationLoader;
import com.lok.game.assets.loader.AnimationLoader.AnimationParameter;
import com.lok.game.conversation.Conversation;
import com.lok.game.conversation.Conversation.ConversationID;
import com.lok.game.map.Map;
import com.lok.game.map.MapManager;
import com.lok.game.map.MapManager.MapID;
import com.lok.game.ui.Bar;

public class AssetsLoadingScreen implements Screen {
    private final static String	TAG = AssetsLoadingScreen.class.getSimpleName();

    private AssetManager	assetManager;
    private long		startTime;

    private int			loadingProgress;

    private final Stage		stage;
    private final Bar		loadingBar;

    public AssetsLoadingScreen() {
	loadingProgress = 0;
	stage = new Stage(new FitViewport(1280, 720));
	final Skin skin = Utils.getUISkin();

	loadingBar = new Bar(skin, Utils.getLabel("Label.LoadingAssets"), 1080, false);
	loadingBar.setPosition(100, 50);
	loadingBar.reset(1.2f);

	stage.addActor(loadingBar);
    }

    @Override
    public void show() {
	startTime = TimeUtils.millis();
	Gdx.app.debug(TAG, "Start loading of assets");
	assetManager = Utils.getAssetManager();

	// load sounds
	assetManager.load("sounds/music/town.ogg", Music.class);
	assetManager.load("sounds/music/demon_lair_01.ogg", Music.class);
	assetManager.load("sounds/effects/menu_selection.wav", Sound.class);
	assetManager.load("sounds/effects/teleport.wav", Sound.class);

	// load texture atlas
	assetManager.load("effects/effects.atlas", TextureAtlas.class);
	assetManager.load("units/units.atlas", TextureAtlas.class);
	assetManager.load("lights/lights.atlas", TextureAtlas.class);

	// load animations
	final AnimationLoader.AnimationParameter aniParam = new AnimationParameter("json/animations.json");
	for (AnimationID aniID : AnimationID.values()) {
	    assetManager.load(aniID.name(), Animation.class, aniParam);
	}

	// load maps
	for (MapID mapID : MapID.values()) {
	    assetManager.load(mapID.name(), Map.class);
	}

	// load conversations
	for (ConversationID convID : ConversationID.values()) {
	    assetManager.load(convID.name(), Conversation.class);
	}
    }

    @Override
    public void render(float delta) {
	if (loadingProgress == 0) {
	    loadingBar.setValue(assetManager.getProgress());
	    if (assetManager.update()) {
		Gdx.app.debug(TAG, "Finished loading of assets in " + TimeUtils.timeSinceMillis(startTime) / 1000.0f + " seconds");
		startTime = TimeUtils.millis();
		loadingBar.setText(Utils.getLabel("Label.PreparingCaches"));
		loadingProgress = 1;
	    }
	} else if (loadingProgress == 1) {
	    // prefill caches
	    Animation.initializeAnimationCache(assetManager);
	    ((LegendOfKaminalyuyu) Gdx.app.getApplicationListener()).initializeScreenCache();
	    MapManager.getManager().initializeMapCache(assetManager);
	    Conversation.initializeConversationCache(assetManager);
	    Gdx.app.debug(TAG, "Finished prefilling caches in " + TimeUtils.timeSinceMillis(startTime) / 1000.0f + " seconds");
	    loadingBar.setValue(1.1f);

	    startTime = TimeUtils.millis();
	    loadingBar.setText(Utils.getLabel("Label.LoadingMapEntities"));
	    loadingProgress = 2;

	} else if (loadingProgress == 2) {
	    MapManager.getManager().loadAllMapEntities();
	    Gdx.app.debug(TAG, "Finished loading of all map entities in " + TimeUtils.timeSinceMillis(startTime) / 1000.0f + " seconds");
	    loadingBar.setValue(1.2f);
	    Utils.setScreen(TownScreen.class);
	}

	Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

	stage.act(delta);
	stage.getViewport().apply();
	stage.draw();
    }

    @Override
    public void resize(int width, int height) {
	stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
	// TODO Auto-generated method stub

    }

    @Override
    public void resume() {
	// TODO Auto-generated method stub

    }

    @Override
    public void hide() {
	dispose();
    }

    @Override
    public void dispose() {
	stage.dispose();
    }

}
