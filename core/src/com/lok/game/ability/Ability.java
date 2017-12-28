package com.lok.game.ability;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;

public abstract class Ability implements Poolable {
    public static interface AbilityListener {
	public void onStartCast(Entity caster, Ability ability);

	public void onUpdateAbility(Entity caster, Ability ability);

	public void onSopCast(Entity caster, Ability ability);
    }

    public static enum AbilityID {
	TOWNPORTAL(TownPortal.class);

	private final Class<? extends Ability> abilityClass;

	private AbilityID(Class<? extends Ability> abilityClass) {
	    this.abilityClass = abilityClass;
	}

	public Class<? extends Ability> getAbilityClass() {
	    return abilityClass;
	}
    }

    public static enum TargetType {
	NoTarget,
	SingleTarget,
	MultiTarget
    }

    private AbilityID		   abilityID;
    protected Entity		   owner;

    private float		   channelTime;

    private boolean		   completed;
    private boolean		   interrupted;

    private Array<Entity>	   targets;

    private Array<AbilityListener> abilityListeners;

    public Ability() {
	reset();
    }

    public void initialize(Entity owner, AbilityID abilityID, Array<AbilityListener> abilityListeners) {
	this.owner = owner;
	this.abilityID = abilityID;
	this.abilityListeners = abilityListeners;
    }

    @Override
    public void reset() {
	this.abilityID = null;
	this.owner = null;
	this.channelTime = 0;
	this.targets = null;
	this.completed = false;
	this.interrupted = false;
	this.abilityListeners = null;
    }

    public AbilityID getAbilityID() {
	return abilityID;
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

    public boolean isEffectReady() {
	return getEffectDelayTime() <= channelTime;
    }

    public boolean isInterrupted() {
	return interrupted;
    }

    public void interrupt() {
	interrupted = true;
    }

    public boolean isCompleted() {
	return completed;
    }

    public void complete() {
	completed = true;
    }

    public void update(float deltaTime) {
	this.channelTime += deltaTime;
	for (AbilityListener listener : abilityListeners) {
	    listener.onUpdateAbility(owner, this);
	}
    }

    public abstract TargetType getTargetType();

    public abstract float getEffectDelayTime();

    public float getChannelTime() {
	return channelTime;
    }

    public void startCast() {
	for (AbilityListener listener : abilityListeners) {
	    listener.onStartCast(owner, this);
	}
	onStartCast();
    }

    protected abstract void onStartCast();

    public void doEffect() {
	completed = onEffect();
    }

    /**
     * 
     * @return <b>true</b> if ability should be cleaned up automatically afterwards <br>
     *         <b>false</b> if ability is cleaned up manually by calling {@link #complete()} later
     */
    protected abstract boolean onEffect();

    public void stopCast() {
	for (AbilityListener listener : abilityListeners) {
	    listener.onSopCast(owner, this);
	}
	onStopCast();
    }

    protected abstract void onStopCast();
}
