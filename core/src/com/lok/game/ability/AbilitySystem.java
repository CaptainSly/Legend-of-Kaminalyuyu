package com.lok.game.ability;

import java.util.Iterator;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.lok.game.ability.Ability.AbilityID;
import com.lok.game.ability.Ability.AbilityListener;
import com.lok.game.ecs.components.AbilityComponent;
import com.lok.game.ecs.components.IDComponent;

// This is a special system not related to the EntityEngine.
// It is used for ability effects which are not immediatly finished after casting (f.e. damage over time).
// It is updated per frame and removes Ability instances if their "isCompleted" method returns true.
public class AbilitySystem {
    private final static String			    TAG	= AbilitySystem.class.getSimpleName();

    private final Array<Ability>		    abilityInstances;
    private final AbilityPool			    abilityPool;
    private final ComponentMapper<AbilityComponent> abilityComponentMapper;
    private final Array<AbilityListener>	    abilityListeners;

    public AbilitySystem(ComponentMapper<AbilityComponent> abilityComponentMapper) {
	abilityInstances = new Array<Ability>();
	abilityPool = new AbilityPool();
	this.abilityComponentMapper = abilityComponentMapper;
	this.abilityListeners = new Array<AbilityListener>();
    }

    public <T extends Ability> T getAbility(Class<T> type) {
	return abilityPool.obtain(type);
    }

    public void update(float deltaTime) {
	final Iterator<Ability> iterator = abilityInstances.iterator();
	while (iterator.hasNext()) {
	    final Ability ability = iterator.next();
	    if (ability.isCompleted() || ability.isInterrupted()) {
		removeAbility(ability);
		iterator.remove();
	    } else if (ability.isEffectReady()) {
		abilityComponentMapper.get(ability.caster).abilityToCast = null;
		ability.doEffect();
	    } else {
		ability.update(deltaTime);
	    }
	}
    }

    public Ability newAbility(Entity caster, AbilityID abilityID) {
	Gdx.app.debug(TAG, "Creating new ability " + abilityID + " for entity " + caster.getComponent(IDComponent.class).entityID);
	final Ability result = abilityPool.obtain(abilityID.getAbilityClass());

	result.initialize(caster, abilityID, abilityListeners);
	abilityInstances.add(result);
	Gdx.app.debug(TAG, "Current instances: " + abilityInstances.size);

	result.startCast();
	return result;
    }

    private void removeAbility(Ability ability) {
	Gdx.app.debug(TAG, "Removing ability " + ability.getAbilityID());
	abilityPool.free(ability);
    }

    public void addAbilityListener(AbilityListener listener) {
	abilityListeners.add(listener);
    }

    public void removeAbilityListener(AbilityListener listener) {
	abilityListeners.removeValue(listener, false);
    }
}
