package com.lok.game.ability;

import java.util.Iterator;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.lok.game.ability.Ability.AbilityID;
import com.lok.game.ecs.components.CastComponent;
import com.lok.game.ecs.components.IDComponent;

// This is a special system not related to the EntityEngine.
// It is used for ability effects which are not immediatly finished after casting (f.e. damage over time).
// It is updated per frame and removes Ability instances if their "isCompleted" method returns true.
public class AbilityEffectSystem {
    private final static String			    TAG	= AbilityEffectSystem.class.getSimpleName();

    private final Array<Ability>		    abilityInstances;
    private final AbilityPool			    abilityPool;
    private final ComponentMapper<CastComponent> abilityComponentMapper;

    public AbilityEffectSystem(ComponentMapper<CastComponent> abilityComponentMapper) {
	abilityInstances = new Array<Ability>();
	abilityPool = new AbilityPool();
	this.abilityComponentMapper = abilityComponentMapper;
    }

    public <T extends Ability> T getAbility(Class<T> type) {
	return abilityPool.obtain(type);
    }

    public void update(float deltaTime) {
	final Iterator<Ability> iterator = abilityInstances.iterator();
	while (iterator.hasNext()) {
	    final Ability ability = iterator.next();
	    if (ability.isCompleted() || ability.isInterrupted()) {
		removeEffect(ability);
		iterator.remove();
	    } else if (ability.isEffectReady()) {
		ability.doEffect();
		// the next line triggers the stopCast event in the next update call of the CastingSystem
		abilityComponentMapper.get(ability.owner).abilityToCast = null;
	    } else {
		ability.update(deltaTime);
	    }
	}
    }

    public Ability newEffect(Entity caster, AbilityID abilityID) {
	Gdx.app.debug(TAG, "Creating new ability " + abilityID + " for entity " + caster.getComponent(IDComponent.class).entityID);
	final Ability result = abilityPool.obtain(abilityID.getAbilityClass());

	result.initialize(caster, abilityID);
	abilityInstances.add(result);
	Gdx.app.debug(TAG, "Current instances: " + abilityInstances.size);

	return result;
    }

    private void removeEffect(Ability ability) {
	Gdx.app.debug(TAG, "Removing ability " + ability.getAbilityID());
	abilityPool.free(ability);
    }
}
