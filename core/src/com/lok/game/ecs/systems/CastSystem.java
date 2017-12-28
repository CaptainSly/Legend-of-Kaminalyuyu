package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.lok.game.ability.Ability;
import com.lok.game.ability.AbilitySystem;
import com.lok.game.ecs.components.AbilityComponent;

public class CastSystem extends IteratingSystem {
    private final ComponentMapper<AbilityComponent> abilityComponentMapper;
    private final AbilitySystem			    abilitySystem;

    public CastSystem(ComponentMapper<AbilityComponent> abilityComponentMapper, AbilitySystem abilitySystem) {
	super(Family.all(AbilityComponent.class).get());
	this.abilityComponentMapper = abilityComponentMapper;
	this.abilitySystem = abilitySystem;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	final AbilityComponent abilityComponent = abilityComponentMapper.get(entity);

	if (abilityComponent.abilityToCast == null && abilityComponent.currentCastingAbility == null) {
	    return;
	}

	Ability currentAbility = abilityComponent.currentCastingAbility;
	if (currentAbility != null && !currentAbility.getAbilityID().equals(abilityComponent.abilityToCast)) {
	    // stop current ability because it is a different one
	    currentAbility.stopCast();
	    if (!currentAbility.isEffectReady()) {
		currentAbility.interrupt();
	    }
	    abilityComponent.currentCastingAbility = currentAbility = null;

	    if (abilityComponent.abilityToCast == null) {
		// no new ability to cast
		return;
	    }
	}

	if (currentAbility == null) {
	    // start casting a new ability
	    currentAbility = abilitySystem.newAbility(entity, abilityComponent.abilityToCast);
	    abilityComponent.currentCastingAbility = currentAbility;
	}
    }
}
