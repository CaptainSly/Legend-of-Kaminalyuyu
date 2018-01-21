package com.lok.game.serialization;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializer;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.ReflectionPool;
import com.lok.game.ecs.EntityEngine.EntityID;

public class MapEntityData implements Poolable {
    private static final Pool<MapEntityData> dataPool = new ReflectionPool<MapEntityData>(MapEntityData.class);

    public EntityID			     entityID = null;
    public Vector2			     position = new Vector2();

    private MapEntityData() {
    }

    public static MapEntityData newMapEntityData(EntityID id, Vector2 position) {
	final MapEntityData result = dataPool.obtain();
	result.entityID = id;
	result.position = position;
	return result;
    }

    public static void removeMapEntityData(MapEntityData data) {
	dataPool.free(data);
    }

    @Override
    public void reset() {
	entityID = null;
	position.set(0, 0);
    }

    public static class MapEntityDataSerializer implements Serializer<MapEntityData> {
	@SuppressWarnings("rawtypes")
	@Override
	public void write(Json json, MapEntityData object, Class knownType) {
	    json.writeObjectStart(MapEntityData.class, null);
	    json.writeValue("entityID", object.entityID);
	    json.writeValue("x", object.position.x);
	    json.writeValue("y", object.position.y);
	    json.writeObjectEnd();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public MapEntityData read(Json json, JsonValue jsonData, Class type) {
	    final MapEntityData result = dataPool.obtain();
	    result.entityID = json.fromJson(EntityID.class, jsonData.getString("entityID"));
	    result.position.set(jsonData.getFloat("x"), jsonData.getFloat("y"));
	    return result;
	}
    }
}
