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
    protected void onStartCast() {
	// TODO create effect

    }

    @Override
    protected boolean onEffect() {
	ScreenManager.getManager().setScreen(TownScreen.class);
	return true;
    }

    @Override
    protected void onStopCast() {
	// TODO remove effect
    }

}
