package com.lok.game.ability;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;

public abstract class Ability {
    public static enum TargetType {
	NoTarget,
	SingleTarget,
	MultiTarget
    }

    private final Entity  owner;
    protected float	  channelTime;
    private Array<Entity> targets;

    public Ability(Entity owner) {
	this.owner = owner;
	this.channelTime = 0;
	this.targets = null;
    }

    public Entity getOwner() {
	return owner;
    }

    public float getChannelTime() {
	return channelTime;
    }

    public Array<Entity> getTargets() {
	if (targets == null) {
	    targets = new Array<Entity>();
	}
	return targets;
    }

    public void addTarget(Entity target) {
	getTargets().add(target);
    }

    public abstract TargetType getTargetType();

    public abstract float getMaxChannelTime();

    public abstract void startCast();

    public void update(float deltaTime) {
	if (getMaxChannelTime() > channelTime) {
	    this.channelTime += deltaTime;
	}
    }

    public abstract void doEffect();

    public abstract void stopCast();
}
