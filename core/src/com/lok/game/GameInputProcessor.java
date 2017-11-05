package com.lok.game;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.lok.game.AnimationManager.AnimationType;
import com.lok.game.ecs.EntityEngine;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.ecs.components.SpeedComponent;

// replace later on with GestureAdapter
public class GameInputProcessor extends InputAdapter implements EntityListener {
    private final ComponentMapper<SpeedComponent>     speedComponentMapper;
    private final ComponentMapper<AnimationComponent> animationComponentMapper;
    private Entity				      player;

    public GameInputProcessor(ComponentMapper<SpeedComponent> speedComponentMapper, ComponentMapper<AnimationComponent> animationComponentMapper) {
	super();
	player = null;
	this.speedComponentMapper = speedComponentMapper;
	this.animationComponentMapper = animationComponentMapper;
	EntityEngine.getEngine().addEntityListener(Family.all(AnimationComponent.class).get(), this);
    }

    @Override
    public void entityAdded(Entity entity) {
	if (entity.flags == EntityID.PLAYER.ordinal()) {
	    this.player = entity;
	}
    }

    @Override
    public void entityRemoved(Entity entity) {
	if (entity.flags == EntityID.PLAYER.ordinal()) {
	    this.player = null;
	}
    }

    @Override
    public boolean keyDown(int keycode) {
	if (player == null) {
	    return false;
	}

	final SpeedComponent speedComponent = speedComponentMapper.get(player);
	final AnimationComponent animationComponent = animationComponentMapper.get(player);

	switch (keycode) {
	    case Keys.UP:
		speedComponent.speed.set(0, speedComponent.maxSpeed);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_UP);
		return true;
	    case Keys.DOWN:
		speedComponent.speed.set(0, -speedComponent.maxSpeed);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_DOWN);
		return true;
	    case Keys.LEFT:
		speedComponent.speed.set(-speedComponent.maxSpeed, 0);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_LEFT);
		return true;
	    case Keys.RIGHT:
		speedComponent.speed.set(speedComponent.maxSpeed, 0);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_RIGHT);
		return true;
	}

	return false;
    }

    @Override
    public boolean keyUp(int keycode) {
	if (player == null) {
	    return false;
	}

	final SpeedComponent speedComponent = speedComponentMapper.get(player);
	final AnimationComponent animationComponent = animationComponentMapper.get(player);

	switch (keycode) {
	    case Keys.UP:
	    case Keys.DOWN:
	    case Keys.LEFT:
	    case Keys.RIGHT:
		speedComponent.speed.set(0, 0);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.IDLE);
		return true;
	}

	return false;
    }

}
