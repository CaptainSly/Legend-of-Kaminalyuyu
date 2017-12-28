package com.lok.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.lok.game.AnimationManager.AnimationID;
import com.lok.game.SpecialEffect;

public class AnimationComponent implements Component, Poolable {
    public AnimationID		    animationID	  = null;
    public float		    animationTime = 0.0f;
    public Animation<TextureRegion> animation	  = null;
    public Color		    color	  = new Color(1, 1, 1, 1);

    public Vector2		    originPoint	  = new Vector2(0, 0);
    public Array<SpecialEffect>	    originEffects = new Array<SpecialEffect>();

    public boolean		    playAnimation = true;

    @Override
    public void reset() {
	animationID = null;
	animation = null;
	color.set(1, 1, 1, 1);
	animationTime = 0.0f;
	playAnimation = true;
	originPoint.set(0, 0);
	originEffects.clear();
    }
}
