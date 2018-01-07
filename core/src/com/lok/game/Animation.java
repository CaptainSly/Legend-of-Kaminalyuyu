package com.lok.game;

import com.badlogic.ashley.utils.Bag;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Animation extends com.badlogic.gdx.graphics.g2d.Animation<TextureRegion> {
    public enum AnimationID {
	PLAYER_IDLE,
	PLAYER_WALK_LEFT,
	PLAYER_WALK_RIGHT,
	PLAYER_WALK_UP,
	PLAYER_WALK_DOWN,

	DEMON_01_IDLE,
	DEMON_01_WALK_LEFT,
	DEMON_01_WALK_RIGHT,
	DEMON_01_WALK_UP,
	DEMON_01_WALK_DOWN,

	BOSS_01_IDLE,
	BOSS_01_WALK_LEFT,
	BOSS_01_WALK_RIGHT,
	BOSS_01_WALK_UP,
	BOSS_01_WALK_DOWN,

	TOWNPORTAL,
	
	SELECTION_SPHERE;
    }

    private final static Bag<Animation> animationCache = new Bag<Animation>(AnimationID.values().length);

    public Animation(float frameDuration, Array<? extends TextureRegion> keyFrames) {
	super(frameDuration, keyFrames, PlayMode.NORMAL);
    }

    public static Animation getAnimation(AnimationID animationID) {
	Animation result = animationCache.get(animationID.ordinal());
	if (result == null) {
	    result = Utils.getAssetManager().get(animationID.name(), Animation.class);
	    animationCache.set(animationID.ordinal(), result);
	}
	return result;
    }

}
