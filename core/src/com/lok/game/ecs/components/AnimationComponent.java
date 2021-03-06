package com.lok.game.ecs.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.lok.game.ui.Animation;
import com.lok.game.ui.SpecialEffect;
import com.lok.game.ui.Animation.AnimationID;

public class AnimationComponent implements Component<AnimationComponent> {
    public AnimationID		idleAnimationID	   = null;
    public AnimationID		walkLeftAnimation  = null;
    public AnimationID		walkRightAnimation = null;
    public AnimationID		walkUpAnimation	   = null;
    public AnimationID		walkDownAnimation  = null;
    public float		animationTime	   = 0.0f;
    public Animation		animation	   = null;
    public Color		color		   = new Color(1, 1, 1, 1);

    public Vector2		originPoint	   = new Vector2(0, 0);
    public Array<SpecialEffect>	originEffects	   = new Array<SpecialEffect>();

    public boolean		playAnimation	   = true;

    @Override
    public void reset() {
	idleAnimationID = null;
	walkLeftAnimation = null;
	walkRightAnimation = null;
	walkUpAnimation = null;
	walkDownAnimation = null;
	animation = null;
	color.set(1, 1, 1, 1);
	animationTime = 0.0f;
	playAnimation = true;
	originPoint.set(0, 0);
	originEffects.clear();
    }

    @Override
    public void initialize(AnimationComponent configComponent) {
	this.idleAnimationID = configComponent.idleAnimationID;
	this.walkLeftAnimation = configComponent.walkLeftAnimation;
	this.walkRightAnimation = configComponent.walkRightAnimation;
	this.walkUpAnimation = configComponent.walkUpAnimation;
	this.walkDownAnimation = configComponent.walkDownAnimation;
	this.animation = Animation.getAnimation(idleAnimationID);
	this.color.set(configComponent.color);
	this.originPoint.set(configComponent.originPoint);
    }
}
