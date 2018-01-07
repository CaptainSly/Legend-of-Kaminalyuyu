package com.lok.game;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.ReflectionPool;
import com.lok.game.Animation.AnimationID;
import com.lok.game.map.MapManager;

public class SpecialEffect implements Poolable {
    private static Pool<SpecialEffect> effectPool = new ReflectionPool<SpecialEffect>(SpecialEffect.class);

    private Animation		       animation;
    private float		       animationTime;
    private Vector2		       size;

    private SpecialEffect() {
	this.size = new Vector2(0, 0);
	reset();
    }

    @Override
    public void reset() {
	this.animation = null;
	this.animationTime = 0;
	this.size.set(0, 0);
    }

    public static SpecialEffect newSpecialEffect(AnimationID animationID, PlayMode playMode) {
	final SpecialEffect result = effectPool.obtain();

	result.animation = Animation.getAnimation(animationID);
	result.animation.setPlayMode(playMode);
	final TextureRegion firstFrame = result.getCurrentKeyFrame();
	result.size.x = firstFrame.getRegionWidth() * MapManager.WORLD_UNITS_PER_PIXEL;
	result.size.y = firstFrame.getRegionHeight() * MapManager.WORLD_UNITS_PER_PIXEL;

	return result;
    }

    public static void removeSpecialEffect(SpecialEffect effect) {
	effectPool.free(effect);
    }

    public void update(float deltaTime) {
	this.animationTime += deltaTime;
    }

    public TextureRegion getCurrentKeyFrame() {
	return animation.getKeyFrame(animationTime);
    }

    public void scaleBy(float scalar) {
	size.scl(scalar);
    }

    public float getWidth() {
	return size.x;
    }

    public float getHeight() {
	return size.y;
    }
}
