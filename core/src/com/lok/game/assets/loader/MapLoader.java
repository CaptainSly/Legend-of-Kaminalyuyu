package com.lok.game.assets.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;
import com.lok.game.map.Map;
import com.lok.game.map.MapManager.MapID;

public class MapLoader extends AsynchronousAssetLoader<Map, MapLoader.MapParameter> {
    private final static String TAG = MapLoader.class.getSimpleName();

    public static class MapParameter extends AssetLoaderParameters<Map> {

    }

    public Map map;

    public MapLoader(FileHandleResolver resolver) {
	super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, MapParameter parameter) {
	Gdx.app.debug(TAG, "Loading map " + fileName);
	this.map = null;
	final MapID idToLoad = MapID.valueOf(fileName);
	final TiledMap tiledMap = manager.get(idToLoad.getMapName(), TiledMap.class);
	this.map = new Map(idToLoad, tiledMap);
    }

    @Override
    public Map loadSync(AssetManager manager, String fileName, FileHandle file, MapParameter parameter) {
	Map map = this.map;
	this.map = null;
	return map;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, MapParameter parameter) {
	final Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
	final MapID idToLoad = MapID.valueOf(fileName);
	dependencies.add(new AssetDescriptor<TiledMap>(idToLoad.getMapName(), TiledMap.class));
	return dependencies;
    }

}
