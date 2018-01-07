package com.lok.game.assets.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.lok.game.Animation;
import com.lok.game.Animation.AnimationID;
import com.lok.game.Utils;

public class AnimationLoader extends AsynchronousAssetLoader<Animation, AnimationLoader.AnimationParameter> {
    public static class AnimationParameter extends AssetLoaderParameters<Animation> {
    }

    public Animation	     animation;
    private Array<JsonValue> jsonFileContent;

    public AnimationLoader(FileHandleResolver resolver) {
	super(resolver);
	jsonFileContent = null;
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, AnimationParameter parameter) {
	final AnimationID idToLoad = AnimationID.valueOf(fileName);
	animation = null;
	for (JsonValue jsonVal : jsonFileContent) {
	    final AnimationID aniID = AnimationID.valueOf(jsonVal.getString("aniID"));
	    if (aniID.equals(idToLoad)) {
		final TextureAtlas textureAtlas = manager.get(jsonVal.getString("atlas"), TextureAtlas.class);
		final AtlasRegion region = textureAtlas.findRegion(jsonVal.getString("atlasKey"));
		final int frameWidth = region.getRegionWidth() / jsonVal.getInt("columns");
		final int frameHeight = region.getRegionHeight() / jsonVal.getInt("rows");

		final Array<TextureRegion> framesOfAnimation = new Array<TextureRegion>();
		final int firstFrameIndexX = jsonVal.get("firstFrame").getInt("x");
		final int firstFrameIndexY = jsonVal.get("firstFrame").getInt("y");
		final int lastFrameIndexX = jsonVal.get("lastFrame").getInt("x");
		final int lastFrameIndexY = jsonVal.get("lastFrame").getInt("y");
		final Texture texture = region.getTexture();
		final int regionX = region.getRegionX();
		final int regionY = region.getRegionY();
		for (int y = firstFrameIndexY; y <= lastFrameIndexY; ++y) {
		    for (int x = firstFrameIndexX; x <= lastFrameIndexX; ++x) {
			framesOfAnimation.add(new TextureRegion(texture, regionX + x * frameWidth, regionY + y * frameHeight, frameWidth, frameHeight));
		    }
		}
		this.animation = new Animation(jsonVal.getFloat("duration"), framesOfAnimation);
		break;
	    }
	}
    }

    @Override
    public Animation loadSync(AssetManager manager, String fileName, FileHandle file, AnimationParameter parameter) {
	Animation animation = this.animation;
	this.animation = null;
	return animation;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AnimationParameter parameter) {
	if (jsonFileContent == null) {
	    jsonFileContent = Utils.fromJson(Gdx.files.internal("json/animations.json"));
	}

	final Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
	final AnimationID idToLoad = AnimationID.valueOf(fileName);
	for (JsonValue jsonVal : jsonFileContent) {
	    final AnimationID aniID = AnimationID.valueOf(jsonVal.getString("aniID"));
	    if (aniID.equals(idToLoad)) {
		dependencies.add(new AssetDescriptor(jsonVal.getString("atlas"), TextureAtlas.class));
		break;
	    }
	}
	return dependencies;
    }
}
