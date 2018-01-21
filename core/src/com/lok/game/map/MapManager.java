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
import com.lok.game.Utils;
import com.lok.game.ecs.EntityEngine;
import com.lok.game.ecs.components.IDComponent;
import com.lok.game.ecs.components.SizeComponent;
import com.lok.game.map.MapEntityData.MapEntityDataSerializer;

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

    public static float			  WORLD_UNITS_PER_PIXEL	= 1.0f / 32.0f;
    private static final String		  TAG			= MapManager.class.getName();
    private static MapManager		  instance		= null;

    private Array<Map>			  mapCache;
    private Map				  currentMap;
    private final Array<Entity>		  currentMapEntities;
    private final Array<MapListener>	  listeners;
    private final MapEntityDataSerializer serializer;

    private MapManager() {
	listeners = new Array<MapListener>();
	this.mapCache = null;
	currentMap = null;
	this.currentMapEntities = new Array<Entity>();
	this.serializer = new MapEntityDataSerializer();
    }

    public static MapManager getManager() {
	if (instance == null) {
	    instance = new MapManager();
	}

	return instance;
    }

    public void changeMap(MapID mapID) {
	Gdx.app.debug(TAG, "Changing map to " + mapID);
	final Map map = mapCache.get(mapID.ordinal());
	this.currentMap = map;
	if (map.getMusicFilePath() != null) {
	    SoundManager.getManager().playMusic(map.getMusicFilePath(), true);
	}
	removeMapEntities();
	for (MapEntityData entityData : map.getEntityData()) {
	    currentMapEntities.add(EntityEngine.getEngine().createEntity(entityData.entityID, entityData.position.x, entityData.position.y));
	}

	for (MapListener listener : listeners) {
	    listener.onMapChange(this, map);
	}
    }

    public void removeMapEntities() {
	for (Entity entity : currentMapEntities) {
	    EntityEngine.getEngine().removeEntity(entity);
	}
	currentMapEntities.clear();
    }

    public Array<Entity> getCurrentMapEntities() {
	return currentMapEntities;
    }

    public Array<Portal> getCurrentMapPortals() {
	return currentMap.getPortals();
    }

    public void addMapListener(MapListener listener) {
	listeners.add(listener);
    }

    public void removeMapListener(MapListener listener) {
	listeners.removeValue(listener, false);
    }

    @Override
    public void onSave(Json json, Preferences preferences) {
	preferences.putString("currentMap", currentMap.getMapID().name());
	for (Map map : mapCache) {
	    final MapID id = map.getMapID();
	    final Array<MapEntityData> entityDataArr = new Array<MapEntityData>();
	    for (Entity entity : currentMapEntities) {
		final SizeComponent sizeComp = entity.getComponent(SizeComponent.class);
		entityDataArr.add(
			MapEntityData.newMapEntityData(entity.getComponent(IDComponent.class).entityID, new Vector2(sizeComp.boundingRectangle.x, sizeComp.boundingRectangle.y)));
	    }
	    json.setSerializer(MapEntityData.class, serializer);
	    preferences.putString(id.name(), json.toJson(entityDataArr));
	    for (MapEntityData data : entityDataArr) {
		MapEntityData.removeMapEntityData(data);
	    }
	}
    }

    @Override
    public void onLoad(Json json, Preferences preferences) {
	if (mapCache == null) {
	    Gdx.app.debug(TAG, "Initializing map cache");
	    mapCache = new Array<Map>();
	    final AssetManager assetManager = Utils.getAssetManager();
	    for (MapID id : MapID.values()) {
		mapCache.add(assetManager.get(id.name(), Map.class));
	    }
	}

	json.setSerializer(MapEntityData.class, serializer);
	for (MapID mapID : MapID.values()) {
	    if (!preferences.contains(mapID.name())) {
		continue;
	    }

	    final Map map = mapCache.get(mapID.ordinal());
	    for (MapEntityData data : map.getEntityData()) {
		MapEntityData.removeMapEntityData(data);
	    }
	    map.getEntityData().clear();
	    @SuppressWarnings("unchecked")
	    final Array<MapEntityData> entityDataArr = json.fromJson(Array.class, preferences.getString(mapID.name()));
	    for (MapEntityData data : entityDataArr) {
		map.getEntityData().add(data);
	    }
	}

	if (!preferences.contains("currentMap")) {
	    changeMap(MapID.DEMON_LAIR_01);
	} else {
	    changeMap(MapID.valueOf(preferences.getString("currentMap")));
	}
    }
}
