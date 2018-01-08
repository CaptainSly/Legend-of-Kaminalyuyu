package com.lok.game.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.lok.game.PreferencesManager.PreferencesListener;
import com.lok.game.SoundManager;
import com.lok.game.ecs.EntityEngine;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.ecs.components.IDComponent;
import com.lok.game.ecs.components.SizeComponent;

public class MapManager implements PreferencesListener {
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
    private MapID		     currentMapID;
    private final Array<MapListener> listeners;

    private MapManager() {
	listeners = new Array<MapListener>();
	this.mapCache = null;
	currentMapID = null;
    }

    public static MapManager getManager() {
	if (instance == null) {
	    instance = new MapManager();
	}

	return instance;
    }

    public void initializeMapCache(AssetManager assetManager) {
	if (mapCache == null) {
	    Gdx.app.debug(TAG, "Initializing map cache");
	    mapCache = new Array<Map>();
	    for (MapID mapID : MapID.values()) {
		mapCache.add(assetManager.get(mapID.name(), Map.class));
	    }
	} else {
	    Gdx.app.error(TAG, "Map cache is initialized multiple times");
	}
    }

    public void changeMap(MapID mapID) {
	Gdx.app.debug(TAG, "Changing map to " + mapID);
	this.currentMapID = mapID;
	final Map map = mapCache.get(mapID.ordinal());
	if (map.getMusicFilePath() != null) {
	    SoundManager.getManager().playMusic(map.getMusicFilePath(), true);
	}

	for (MapListener listener : listeners) {
	    listener.onMapChange(this, map);
	}
    }

    public void loadAllMapEntities() {
	for (Map map : mapCache) {
	    map.loadEntities();
	}
    }

    public void addMapListener(MapListener listener) {
	listeners.add(listener);
    }

    public void removeMapListener(MapListener listener) {
	listeners.removeValue(listener, false);
    }

    private static class EntityData {
	private EntityID id	  = null;
	private Vector2	 position = new Vector2();
    }

    @Override
    public void onSave(Json json, Preferences preferences) {
	for (Map map : mapCache) {
	    final MapID id = map.getMapID();
	    final Array<EntityData> entityData = new Array<EntityData>();
	    for (Entity entity : map.getEntities()) {
		final EntityData data = new EntityData();
		data.id = entity.getComponent(IDComponent.class).entityID;
		entity.getComponent(SizeComponent.class).boundingRectangle.getPosition(data.position);
		entityData.add(data);
	    }
	    preferences.putString(id.name(), json.toJson(entityData));
	}
	preferences.putString("currentMap", currentMapID.name());
    }

    @Override
    public void onLoad(Json json, Preferences preferences) {
	if (!preferences.contains("currentMap")) {
	    changeMap(MapID.DEMON_LAIR_01);
	} else {
	    changeMap(MapID.valueOf(preferences.getString("currentMap")));
	}

	for (MapID mapID : MapID.values()) {
	    if (!preferences.contains(mapID.name())) {
		continue;
	    }

	    final Map map = mapCache.get(mapID.ordinal());
	    for (Entity entity : map.getEntities()) {
		EntityEngine.getEngine().removeEntity(entity);
	    }
	    @SuppressWarnings("unchecked")
	    final Array<EntityData> entityData = json.fromJson(Array.class, preferences.getString(mapID.name()));
	    for (EntityData data : entityData) {
		EntityEngine.getEngine().createEntity(data.id, data.position.x, data.position.y);
	    }
	}
    }
}
