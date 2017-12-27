package com.lok.game.ability;

import com.badlogic.ashley.core.Entity;
import com.lok.game.screen.ScreenManager;
import com.lok.game.screen.TownScreen;

public class TownPortal extends Ability {

    public TownPortal(Entity owner) {
	super(owner);
    }

    @Override
    public TargetType getTargetType() {
	return TargetType.NoTarget;
    }

    @Override
    public float getMaxChannelTime() {
	return 2.5f;
    }

    @Override
    public void startCast() {
	// TODO Auto-generated method stub

    }

    @Override
    public void doEffect() {
	ScreenManager.getManager().setScreen(TownScreen.class);
    }

    @Override
    public void stopCast() {
	channelTime = 0;
    }

}
