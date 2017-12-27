package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import com.lok.game.ability.Ability;
import com.lok.game.ecs.components.AbilityComponent;

public class AbilitySystem extends IteratingSystem {
    public static interface AbilityListener {
	public void onStartCast(Entity entity, Ability ability);

	public void onSopCast(Entity entity, Ability ability);
    }

    private final ComponentMapper<AbilityComponent> abilityComponentMapper;
    private final Array<AbilityListener>	    abilityListeners;

    public AbilitySystem(ComponentMapper<AbilityComponent> abilityComponentMapper) {
	super(Family.all(AbilityComponent.class).get());
	this.abilityComponentMapper = abilityComponentMapper;
	this.abilityListeners = new Array<AbilitySystem.AbilityListener>();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	final AbilityComponent abilityComponent = abilityComponentMapper.get(entity);

	if (abilityComponent.abilityToCast != abilityComponent.currentCastingAbility) {
	    // change current casting ability
	    if (abilityComponent.currentCastingAbility != null) {
		for (AbilityListener listener : abilityListeners) {
		    listener.onSopCast(entity, abilityComponent.currentCastingAbility);
		}
		abilityComponent.currentCastingAbility.stopCast();
	    }
	    abilityComponent.currentCastingAbility = abilityComponent.abilityToCast;

	    if (abilityComponent.currentCastingAbility != null) {
		for (AbilityListener listener : abilityListeners) {
		    listener.onStartCast(entity, abilityComponent.currentCastingAbility);
		}
		abilityComponent.currentCastingAbility.startCast();
	    }
	}

	if (abilityComponent.currentCastingAbility == null) {
	    return;
	}

	if (abilityComponent.currentCastingAbility.getChannelTime() >= abilityComponent.currentCastingAbility.getMaxChannelTime()) {
	    abilityComponent.currentCastingAbility.doEffect();
	    for (AbilityListener listener : abilityListeners) {
		listener.onSopCast(entity, abilityComponent.currentCastingAbility);
	    }
	    abilityComponent.currentCastingAbility.stopCast();
	    abilityComponent.abilityToCast = null;
	    abilityComponent.currentCastingAbility = null;
	} else {
	    abilityComponent.currentCastingAbility.update(deltaTime);
	}
    }

    public void addAbilityListener(AbilityListener listener) {
	abilityListeners.add(listener);
    }

    public void removeAbilityListener(AbilityListener listener) {
	abilityListeners.removeValue(listener, false);
    }
}
