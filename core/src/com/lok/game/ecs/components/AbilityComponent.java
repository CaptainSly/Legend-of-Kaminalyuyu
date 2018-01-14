package com.lok.game.ecs.components;

import com.badlogic.gdx.utils.Array;
import com.lok.game.ability.Ability;
import com.lok.game.ability.Ability.AbilityID;

public class AbilityComponent implements Component<AbilityComponent> {
    public Array<AbilityID> abilities		  = new Array<AbilityID>();
    public AbilityID	    abilityToCast	  = null;
    public Ability	    currentCastingAbility = null;

    @Override
    public void reset() {
	abilities.clear();
	this.abilityToCast = null;
	this.currentCastingAbility = null;
    }

    @Override
    public void initialize(AbilityComponent configComponent) {
	for (AbilityID abiID : configComponent.abilities) {
	    this.abilities.add(abiID);
	}
    }

}
