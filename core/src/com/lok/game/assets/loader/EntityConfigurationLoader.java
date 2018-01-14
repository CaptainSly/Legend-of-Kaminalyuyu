package com.lok.game.assets.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import com.lok.game.Utils;
import com.lok.game.ecs.EntityConfiguration;
import com.lok.game.ecs.EntityEngine.EntityID;

public class EntityConfigurationLoader extends AsynchronousAssetLoader<EntityConfiguration, EntityConfigurationLoader.EntityConfigurationParameter> {
    private static final String TAG = EntityConfigurationLoader.class.getSimpleName();

    public static class EntityConfigurationParameter extends AssetLoaderParameters<EntityConfiguration> {
	private Array<JsonValue> jsonFileContent;

	public EntityConfigurationParameter(String jsonFilePath) {
	    jsonFileContent = Utils.fromJson(Gdx.files.internal(jsonFilePath));
	}
    }

    public EntityConfiguration entityConfiguration;

    public EntityConfigurationLoader(FileHandleResolver resolver) {
	super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, EntityConfigurationParameter parameter) {
	Gdx.app.debug(TAG, "Loading entity configuration " + fileName);
	if (parameter.jsonFileContent == null || parameter.jsonFileContent.size == 0) {
	    throw new GdxRuntimeException("EntityConfigurationParameter jsonFileContent cannot be null or empty");
	}

	final EntityID idToLoad = EntityID.valueOf(fileName);
	entityConfiguration = null;
	for (JsonValue jsonVal : parameter.jsonFileContent) {
	    final EntityID entityID = EntityID.valueOf(jsonVal.getString("entityID"));
	    if (entityID.equals(idToLoad)) {
		entityConfiguration = Utils.readJsonValue(EntityConfiguration.class, jsonVal.get("components"));
		Gdx.app.debug(TAG, "Created new entity configuration " + fileName);
		return;
	    }
	}

	throw new GdxRuntimeException("There is no entity configuration for " + fileName + " with the given parameter");
    }

    @Override
    public EntityConfiguration loadSync(AssetManager manager, String fileName, FileHandle file, EntityConfigurationParameter parameter) {
	EntityConfiguration entityConfiguration = this.entityConfiguration;
	this.entityConfiguration = null;
	return entityConfiguration;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, EntityConfigurationParameter parameter) {
	return null;
    }
}
