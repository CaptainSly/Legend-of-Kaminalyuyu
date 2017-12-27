package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import com.lok.game.ability.Ability;
import com.lok.game.ability.AbilityEffectSystem;
import com.lok.game.ecs.components.CastComponent;

public class CastSystem extends IteratingSystem {
    public static interface CastListener {
	public void onStartCast(Entity caster, Ability ability);

	public void onSopCast(Entity caster, Ability ability);
    }

    private final ComponentMapper<CastComponent> abilityComponentMapper;
    private final Array<CastListener>	    abilityListeners;
    private final AbilityEffectSystem		    abilityEffectSystem;

    public CastSystem(ComponentMapper<CastComponent> abilityComponentMapper, AbilityEffectSystem abilityEffectSystem) {
	super(Family.all(CastComponent.class).get());
	this.abilityComponentMapper = abilityComponentMapper;
	this.abilityListeners = new Array<CastSystem.CastListener>();
	this.abilityEffectSystem = abilityEffectSystem;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	final CastComponent abilityComponent = abilityComponentMapper.get(entity);

	if (abilityComponent.abilityToCast == null && abilityComponent.currentCastingAbility == null) {
	    return;
	}

	Ability currentAbility = abilityComponent.currentCastingAbility;
	if (currentAbility != null && !currentAbility.getAbilityID().equals(abilityComponent.abilityToCast)) {
	    // stop current ability because it is a different one
	    for (CastListener listener : abilityListeners) {
		listener.onSopCast(entity, currentAbility);
	    }
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
	    currentAbility = abilityEffectSystem.newEffect(entity, abilityComponent.abilityToCast);
	    for (CastListener listener : abilityListeners) {
		listener.onStartCast(entity, currentAbility);
	    }
	    currentAbility.startCast();
	    abilityComponent.currentCastingAbility = currentAbility;
	}
    }

    public void addAbilityListener(CastListener listener) {
	abilityListeners.add(listener);
    }

    public void removeAbilityListener(CastListener listener) {
	abilityListeners.removeValue(listener, false);
    }
}
