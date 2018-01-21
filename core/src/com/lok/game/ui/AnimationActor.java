package com.lok.game.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.lok.game.ui.Animation.AnimationID;

public class AnimationActor extends Actor {
    private float	    animationTime;
    private final Animation animation;

    public AnimationActor(AnimationID animationID) {
	this.animation = Animation.getAnimation(animationID);
	this.animationTime = 0;
	setSize(animation.getKeyFrame(0).getRegionWidth(), animation.getKeyFrame(0).getRegionHeight());
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
