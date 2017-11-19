package com.lok.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.AssetLoader;

public class AssetManager {
    private static final String			       TAG	= AssetManager.class.getName();
    private static AssetManager			       instance	= null;

    private final com.badlogic.gdx.assets.AssetManager assetManager;

    private AssetManager() {
	this.assetManager = new com.badlogic.gdx.assets.AssetManager();
    }

    public static AssetManager getManager() {
	if (instance == null) {
	    instance = new AssetManager();
	}

	return instance;
    }

    public <T, P extends AssetLoaderParameters<T>> void setLoader(Class<T> type, AssetLoader<T, P> loader) {
	assetManager.setLoader(type, loader);
    }

    public <T> T getAsset(String fileName, Class<T> type, AssetLoaderParameters<T> parameter) {
	if (!assetManager.isLoaded(fileName, type)) {
	    Gdx.app.debug(TAG, "Loading asset " + fileName + " of type " + type);
	    assetManager.load(fileName, type, parameter);
	    assetManager.finishLoading();
	}

	return assetManager.get(fileName, type);
    }

    public <T> T getAsset(String fileName, Class<T> type) {
	return getAsset(fileName, type, null);
    }

    public void dispose() {
	Gdx.app.debug(TAG, "Disposing assetmanager");
	assetManager.dispose();
    }
}
