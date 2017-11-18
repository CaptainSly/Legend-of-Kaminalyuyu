package com.lok.game;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.lok.game.AnimationManager.AnimationType;
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.ecs.components.SpeedComponent;

// replace later on with GestureAdapter
public class GameInputProcessor extends InputAdapter {
    private final ComponentMapper<SpeedComponent>     speedComponentMapper;
    private final ComponentMapper<AnimationComponent> animationComponentMapper;
    private Entity				      player;

    public GameInputProcessor() {
	super();

	player = null;
	this.speedComponentMapper = ComponentMapper.getFor(SpeedComponent.class);
	this.animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);
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
		animationComponent.playAnimation = true;
		return true;
	    case Keys.DOWN:
		speedComponent.speed.set(0, -speedComponent.maxSpeed);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_DOWN);
		animationComponent.playAnimation = true;
		return true;
	    case Keys.LEFT:
		speedComponent.speed.set(-speedComponent.maxSpeed, 0);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_LEFT);
		animationComponent.playAnimation = true;
		return true;
	    case Keys.RIGHT:
		speedComponent.speed.set(speedComponent.maxSpeed, 0);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_RIGHT);
		animationComponent.playAnimation = true;
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
		if (Gdx.input.isKeyPressed(Keys.UP)) {
		    speedComponent.speed.set(0, speedComponent.maxSpeed);
		    animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_UP);
		    return true;
		}
		if (Gdx.input.isKeyPressed(Keys.DOWN)) {
		    speedComponent.speed.set(0, -speedComponent.maxSpeed);
		    animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_DOWN);
		    return true;
		}
		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
		    speedComponent.speed.set(-speedComponent.maxSpeed, 0);
		    animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_LEFT);
		    return true;
		}
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
		    speedComponent.speed.set(speedComponent.maxSpeed, 0);
		    animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_RIGHT);
		    return true;
		}
		if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.RIGHT)) {
		    return true;
		}

		speedComponent.speed.set(0, 0);
		animationComponent.animationTime = 0;
		animationComponent.playAnimation = false;
		return true;
	}

	return false;
    }

    public void setPlayer(Entity entity) {
	this.player = entity;
    }

}
