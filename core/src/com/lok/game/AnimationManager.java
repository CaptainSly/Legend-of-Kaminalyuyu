package com.lok.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.TimeUtils;

public class AnimationManager {
    public enum AnimationID {
	HERO;

	private final int cacheIndex;

	private AnimationID() {
	    cacheIndex = ordinal() * AnimationType.values().length;
	}
    }

    public enum AnimationType {
	// it is important that IDLE is the first animationType defined in this enum
	// check loadAnimation method for details
	IDLE,
	WALK_LEFT,
	WALK_RIGHT,
	WALK_UP,
	WALK_DOWN;
    }

    private static class AnimationTypeConfgiruation {
	private AnimationType animationType;
	private GridPoint2    firstFrameIndex;
	private GridPoint2    lastFrameIndex;
    }

    private static class AnimationConfiguration {
	private AnimationID			  animationID;
	private String				  atlasName;
	private String				  atlasKey;
	private int				  numColumns;
	private int				  numRows;
	private float				  frameDuration;
	private Array<AnimationTypeConfgiruation> animations;
    }

    private static final String			  TAG	   = AnimationManager.class.getName();
    private static AnimationManager		  instance = null;

    private final Array<Animation<TextureRegion>> animationCache;

    private AnimationManager() {
	animationCache = new Array<Animation<TextureRegion>>(true, AnimationID.values().length * AnimationType.values().length);
	loadAnimations();
    }

    public static AnimationManager getManager() {
	if (instance == null) {
	    instance = new AnimationManager();
	}

	return instance;
    }

    private void loadAnimation(AnimationID animationID, AnimationConfiguration animationConfig) {
	if (!animationID.equals(animationConfig.animationID)) {
	    return;
	}

	Gdx.app.debug(TAG, "Loading animation " + animationConfig.animationID);

	final TextureAtlas textureAtlas = AssetManager.getManager().getAsset(animationConfig.atlasName, TextureAtlas.class);
	final AtlasRegion region = textureAtlas.findRegion(animationConfig.atlasKey);
	final int frameWidth = region.getRegionWidth() / animationConfig.numColumns;
	final int frameHeight = region.getRegionHeight() / animationConfig.numRows;
	final int animationArrayStartIndex = animationConfig.animationID.ordinal() * AnimationType.values().length;

	for (AnimationType aniType : AnimationType.values()) {
	    boolean foundAnimationOfType = false;

	    for (AnimationTypeConfgiruation animationTypeConfig : animationConfig.animations) {
		if (!aniType.equals(animationTypeConfig.animationType)) {
		    continue;
		}

		Gdx.app.debug(TAG, "Loading animation " + animationConfig.animationID + " of type " + animationTypeConfig.animationType);

		final Array<TextureRegion> framesOfAnimation = new Array<TextureRegion>();
		for (int y = animationTypeConfig.firstFrameIndex.y; y <= animationTypeConfig.lastFrameIndex.y; ++y) {
		    for (int x = animationTypeConfig.firstFrameIndex.x; x <= animationTypeConfig.lastFrameIndex.x; ++x) {
			framesOfAnimation.add(new TextureRegion(region.getTexture(), x * frameWidth, y * frameHeight, frameWidth, frameHeight));
		    }
		}

		animationCache.add(new Animation<TextureRegion>(animationConfig.frameDuration, framesOfAnimation)); // value
		foundAnimationOfType = true;
	    }

	    if (!foundAnimationOfType) {
		if (aniType.equals(AnimationType.IDLE)) {
		    throw new GdxRuntimeException("IDLE animation missing for animation " + animationConfig.animationID);
		}

		// if a non-idle animation is not defined then set it equal to the idle animation to avoid additional checks
		// in getAnimation method
		final Animation<TextureRegion> idleAnimation = animationCache.get(animationArrayStartIndex + AnimationType.IDLE.ordinal());
		for (AnimationType animationType : AnimationType.values()) {
		    final int animationArrayIndex = animationArrayStartIndex + animationType.ordinal();
		    if (animationCache.get(animationArrayIndex) == null) {
			Gdx.app.debug(TAG, "Animation " + animationConfig.animationID + " of type " + animationType + " missing -> using IDLE animation");
			animationCache.add(idleAnimation);
		    }
		}
	    }
	}
    }

    private void loadAnimations() {
	final long startTime = TimeUtils.millis();
	final Object fromJson = Utils.fromJson(Gdx.files.internal("json/animations.json"));
	final Array<AnimationConfiguration> configsInFile = new Array<AnimationConfiguration>();
	if (fromJson instanceof Array<?>) {
	    // multiple animations defined within file
	    for (Object val : (Array<?>) fromJson) {
		configsInFile.add(Utils.readJsonValue(AnimationConfiguration.class, (JsonValue) val));
	    }
	} else {
	    // only one animation defined -> load it
	    configsInFile.add(Utils.readJsonValue(AnimationConfiguration.class, (JsonValue) fromJson));
	}

	for (AnimationID aniID : AnimationID.values()) {
	    for (AnimationConfiguration aniCfg : configsInFile) {
		loadAnimation(aniID, aniCfg);
	    }
	}

	Gdx.app.debug(TAG, "Loaded all animations in " + TimeUtils.timeSinceMillis(startTime) / 1000.0f);
    }

    public Animation<TextureRegion> getAnimation(AnimationID id, AnimationType type) {
	return animationCache.get(id.cacheIndex + type.ordinal());
    }
}
