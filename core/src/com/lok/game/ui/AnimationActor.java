package com.lok.game.ui;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.lok.game.AssetManager;

public class AnimationActor extends Actor {
    private float			   animationTime;
    private final Animation<TextureRegion> animation;

    public AnimationActor(String textureAtlasPath, String atlasKey, int numColumns, int numRows, float frameDuration) {
	final TextureAtlas textureAtlas = AssetManager.getManager().getAsset(textureAtlasPath, TextureAtlas.class);
	final AtlasRegion region = textureAtlas.findRegion(atlasKey);
	final int frameWidth = region.getRegionWidth() / numColumns;
	final int frameHeight = region.getRegionHeight() / numRows;

	final TextureRegion[][] split = region.split(frameWidth, frameHeight);

	final Array<TextureRegion> framesOfAnimation = new Array<TextureRegion>();
	for (int x = 0; x < split.length; ++x) {
	    for (int y = 0; y < split[x].length; ++y) {
		framesOfAnimation.add(split[x][y]);
	    }
	}

	this.animation = new Animation<TextureRegion>(frameDuration, framesOfAnimation);
	this.animationTime = 0;
	setSize(frameWidth, frameHeight);
    }

    @Override
    protected void sizeChanged() {
	setOrigin(getWidth() * 0.5f, getHeight() * 0.5f);
    }

    @Override
    public void act(float delta) {
	this.animationTime += delta;
	super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
	final TextureRegion keyFrame = animation.getKeyFrame(animationTime, true);
	batch.setColor(getColor());
	batch.draw(keyFrame, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
    }
}
