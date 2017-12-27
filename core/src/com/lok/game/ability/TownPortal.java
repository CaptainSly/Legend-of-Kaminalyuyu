package com.lok.game.ability;

import com.lok.game.screen.ScreenManager;
import com.lok.game.screen.TownScreen;

public class TownPortal extends Ability {

    @Override
    public TargetType getTargetType() {
	return TargetType.NoTarget;
    }

    @Override
    public float getEffectDelayTime() {
	return 2.5f;
    }

    @Override
    public void startCast() {
	// TODO create effect

    }

    @Override
    public boolean onEffect() {
	ScreenManager.getManager().setScreen(TownScreen.class);
	return true;
    }

    @Override
    public void stopCast() {
	// TODO remove effect
    }

}
