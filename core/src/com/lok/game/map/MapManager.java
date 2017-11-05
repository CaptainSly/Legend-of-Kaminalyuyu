package com.lok.game.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.lok.game.AssetManager;

public class MapManager {
    public enum MapID {
	DEMON_LAIR_01("maps/demon_lair_01.tmx");

	private final String mapName;

	private MapID(String mapName) {
	    this.mapName = mapName;
	}

	public String getMapName() {
	    return mapName;
	}
    }

    public static float		     WORLD_UNITS_PER_PIXEL = 1.0f / 32.0f;
    private static final String	     TAG		   = MapManager.class.getName();
    private static MapManager	     instance		   = null;

    private Array<Map>		     mapCache;
    private final Array<MapListener> listeners;

    private MapManager() {
	AssetManager.getManager().setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
	listeners = new Array<MapListener>();
	this.mapCache = null;
    }

    private void loadMaps() {
	final long start = TimeUtils.millis();
	mapCache = new Array<Map>();
	for (MapID mapID : MapID.values()) {
	    mapCache.add(new Map(mapID));
	}
	Gdx.app.debug(TAG, "Loaded all maps in " + TimeUtils.timeSinceMillis(start) / 1000.0f);
    }

    public static MapManager getManager() {
	if (instance == null) {
	    instance = new MapManager();
	}

	return instance;
    }

    public void changeMap(MapID mapID) {
	if (mapCache == null) {
	    loadMaps();
	}

	Gdx.app.debug(TAG, "Changing map to " + mapID);
	final Map map = mapCache.get(mapID.ordinal());

	for (MapListener listener : listeners) {
	    listener.onMapChange(this, map);
	}
    }

    public void addListener(MapListener listener) {
	listeners.add(listener);
    }

    public void removeListener(MapListener listener) {
	listeners.removeValue(listener, false);
    }

    public void dispose() {
	Gdx.app.debug(TAG, "Disposing mapmanager");
	for (Map map : mapCache) {
	    map.dispose();
	}
    }
}
