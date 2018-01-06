package com.lok.game.ability;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.utils.Array;
import com.lok.game.AnimationManager.AnimationID;
import com.lok.game.AnimationManager.AnimationType;
import com.lok.game.SoundManager;
import com.lok.game.SpecialEffect;
import com.lok.game.Utils;
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.screen.TownScreen;

public class TownPortal extends Ability {
    private SpecialEffect      effect;
    private AnimationComponent animationComp;
    private float	       origR, origG, origB;
    private float	       lossPerFrameR, lossPerFrameG, lossPerFrameB;
    private long	       soundID;

    @Override
    public void initialize(Entity caster, AbilityID abilityID, Array<AbilityListener> abilityListeners) {
	super.initialize(caster, abilityID, abilityListeners);
	animationComp = caster.getComponent(AnimationComponent.class);
	origR = animationComp.color.r;
	origG = animationComp.color.g;
	origB = animationComp.color.b;
	lossPerFrameR = animationComp.color.r / 2.5f;
	lossPerFrameG = animationComp.color.g / 2.5f;
	lossPerFrameB = animationComp.color.b / 2.5f;
	soundID = 0L;
    }

    @Override
    public void reset() {
	super.reset();
	effect = null;
	animationComp = null;
	origR = origG = origB = 0;
	lossPerFrameR = lossPerFrameG = lossPerFrameB = 0;
	soundID = 0L;
    }

    @Override
    public TargetType getTargetType() {
	return TargetType.NoTarget;
    }

    @Override
    public float getEffectDelayTime() {
	return 2.5f;
    }

    @Override
    protected void onStartCast() {
	soundID = SoundManager.getManager().playSound("sounds/effects/teleport.wav", false);
	effect = SpecialEffect.newSpecialEffect(AnimationID.TOWNPORTAL, AnimationType.IDLE, PlayMode.NORMAL);
	effect.scaleBy(0.75f);
	animationComp.originEffects.add(effect);
    }

    @Override
    public void update(float deltaTime) {
	super.update(deltaTime);
	animationComp.color.r -= lossPerFrameR * deltaTime;
	animationComp.color.g -= lossPerFrameG * deltaTime;
	animationComp.color.b -= lossPerFrameB * deltaTime;
    }

    @Override
    protected boolean onEffect() {
	Utils.setScreen(TownScreen.class);
	return true;
    }

    @Override
    protected void onStopCast() {
	SoundManager.getManager().stopSound("sounds/effects/teleport.wav", soundID);
	animationComp.color.set(origR, origG, origB, animationComp.color.a);
	animationComp.originEffects.removeValue(effect, false);
	SpecialEffect.removeSpecialEffect(effect);
    }

}
