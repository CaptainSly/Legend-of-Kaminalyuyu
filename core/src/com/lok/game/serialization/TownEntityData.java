package com.lok.game.serialization;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializer;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.ReflectionPool;
import com.lok.game.conversation.Conversation.ConversationID;
import com.lok.game.ecs.EntityEngine.EntityID;

public class TownEntityData implements Poolable {
    private static final Pool<TownEntityData> dataPool	     = new ReflectionPool<TownEntityData>(TownEntityData.class);

    public EntityID			      entityID	     = null;
    public ConversationID		      conversationID = null;
    public Vector2			      position	     = new Vector2();

    private TownEntityData() {
    }

    public static TownEntityData newTownEntityData(EntityID entityID, ConversationID conversationID, float x, float y) {
	final TownEntityData result = dataPool.obtain();
	result.entityID = entityID;
	result.conversationID = conversationID;
	result.position.set(x, y);
	return result;
    }

    public static void removeTownEntityData(TownEntityData data) {
	dataPool.free(data);
    }

    @Override
    public void reset() {
	entityID = null;
	conversationID = null;
	position.set(0, 0);
    }

    public static class TownEntityDataSerializer implements Serializer<TownEntityData> {
	@SuppressWarnings("rawtypes")
	@Override
	public void write(Json json, TownEntityData object, Class knownType) {
	    json.writeObjectStart(TownEntityData.class, null);
	    json.writeValue("entityID", object.entityID);
	    json.writeValue("conversationID", object.conversationID);
	    json.writeValue("x", object.position.x);
	    json.writeValue("y", object.position.y);
	    json.writeObjectEnd();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public TownEntityData read(Json json, JsonValue jsonData, Class type) {
	    final TownEntityData result = dataPool.obtain();
	    result.entityID = json.fromJson(EntityID.class, jsonData.getString("entityID"));
	    result.conversationID = jsonData.getString("conversationID") == null ? null : json.fromJson(ConversationID.class, jsonData.getString("conversationID"));
	    result.position.set(jsonData.getFloat("x"), jsonData.getFloat("y"));
	    return result;
	}
    }

}
