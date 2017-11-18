package com.lok.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.lok.game.AnimationManager.AnimationID;

public class AnimationComponent implements Component, Poolable {
    public AnimationID		    animationID	  = null;
    public float		    animationTime = 0.0f;
    public Animation<TextureRegion> animation	  = null;
    public boolean		    playAnimation = true;

    @Override
    public void reset() {
	animationID = null;
	animation = null;
	animationTime = 0.0f;
	playAnimation = true;
    }
}
