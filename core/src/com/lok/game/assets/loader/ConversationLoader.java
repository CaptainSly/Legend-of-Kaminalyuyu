package com.lok.game.assets.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.lok.game.Utils;
import com.lok.game.conversation.Conversation;
import com.lok.game.conversation.Conversation.ConversationID;
import com.lok.game.conversation.ConversationNode;

public class ConversationLoader extends AsynchronousAssetLoader<Conversation, ConversationLoader.ConversationParameter> {
    private static final String TAG = ConversationLoader.class.getSimpleName();

    public static class ConversationParameter extends AssetLoaderParameters<Conversation> {
    }

    public Conversation conversation;

    public ConversationLoader(FileHandleResolver resolver) {
	super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, ConversationParameter parameter) {
	Gdx.app.debug(TAG, "Loading conversation " + fileName);

	final ConversationID idToLoad = ConversationID.valueOf(fileName);
	conversation = null;
	final Array<JsonValue> jsonFileContent = Utils.fromJson(Gdx.files.internal(idToLoad.getFilePath()));
	this.conversation = new Conversation(idToLoad, Utils.readJsonValue(ConversationNode.class, jsonFileContent.get(0)));
	for (int i = 1; i < jsonFileContent.size; ++i) {
	    conversation.addNode(Utils.readJsonValue(ConversationNode.class, jsonFileContent.get(i)));
	}
    }

    @Override
    public Conversation loadSync(AssetManager manager, String fileName, FileHandle file, ConversationParameter parameter) {
	Conversation conversation = this.conversation;
	this.conversation = null;
	return conversation;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, ConversationParameter parameter) {
	return null;
    }
}
