package com.lok.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.TimeUtils;
import com.lok.game.Utils;
import com.lok.game.map.MapManager;

public class AssetsLoadingScreen implements Screen {
    private final static String	TAG = AssetsLoadingScreen.class.getSimpleName();

    private AssetManager	assetManager;
    private long		startTime;

    @Override
    public void show() {
	startTime = TimeUtils.millis();
	Gdx.app.debug(TAG, "Start loading of assets");
	assetManager = Utils.getAssetManager();

	// load GameScreen assets
	for (MapManager.MapID mapID : MapManager.MapID.values()) {
	    assetManager.load(mapID.getMapName(), TiledMap.class);
	}
	assetManager.load("effects/effects.atlas", TextureAtlas.class);
	assetManager.load("units/units.atlas", TextureAtlas.class);
	assetManager.load("lights/lights.atlas", TextureAtlas.class);
    }

    @Override
    public void render(float delta) {
	if (assetManager.update()) {
	    Gdx.app.debug(TAG, "Finished loading of assets in " + TimeUtils.timeSinceMillis(startTime) / 1000.0f + " seconds");
	    Utils.setScreen(TownScreen.class);
	}
    }

    @Override
    public void resize(int width, int height) {
	// TODO Auto-generated method stub

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
	// TODO Auto-generated method stub

    }

}
