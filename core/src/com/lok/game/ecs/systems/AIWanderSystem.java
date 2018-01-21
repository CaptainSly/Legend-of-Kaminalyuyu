package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.lok.game.ecs.components.AIWanderComponent;
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.ecs.components.SpeedComponent;
import com.lok.game.ui.Animation;

public class AIWanderSystem extends IteratingSystem {
    private final ComponentMapper<SpeedComponent>     speedComponentMapper;
    private final ComponentMapper<AIWanderComponent>  aiWanderComponentMapper;
    private final ComponentMapper<AnimationComponent> animationComponentMapper;

    public AIWanderSystem(ComponentMapper<AIWanderComponent> aiWanderComponentMapper, ComponentMapper<SpeedComponent> speedComponentMapper,
	    ComponentMapper<AnimationComponent> animationComponentMapper) {
	super(Family.all(AIWanderComponent.class, SpeedComponent.class, AnimationComponent.class).get());

	this.aiWanderComponentMapper = aiWanderComponentMapper;
	this.speedComponentMapper = speedComponentMapper;
	this.animationComponentMapper = animationComponentMapper;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	final AIWanderComponent aiWanderComponent = aiWanderComponentMapper.get(entity);
	final SpeedComponent speedComponent = speedComponentMapper.get(entity);
	final AnimationComponent animationComponent = animationComponentMapper.get(entity);

	aiWanderComponent.wanderTime -= deltaTime;
	if (aiWanderComponent.wanderTime <= 0) {
	    aiWanderComponent.wanderTime = MathUtils.random(1.5f, 7.5f);

	    switch (MathUtils.random(4)) {
		case 0:
		    // go right
		    speedComponent.speed.set(speedComponent.maxSpeed, 0);
		    animationComponent.animation = Animation.getAnimation(animationComponent.walkRightAnimation);
		    animationComponent.playAnimation = true;
		    break;
		case 1:
		    // go left
		    speedComponent.speed.set(-speedComponent.maxSpeed, 0);
		    animationComponent.animation = Animation.getAnimation(animationComponent.walkLeftAnimation);
		    animationComponent.playAnimation = true;
		    break;
		case 2:
		    // go up
		    speedComponent.speed.set(0, speedComponent.maxSpeed);
		    animationComponent.animation = Animation.getAnimation(animationComponent.walkUpAnimation);
		    animationComponent.playAnimation = true;
		    break;
		case 3:
		    // go down
		    speedComponent.speed.set(0, -speedComponent.maxSpeed);
		    animationComponent.animation = Animation.getAnimation(animationComponent.walkDownAnimation);
		    animationComponent.playAnimation = true;
		    break;
		case 4:
		    // idle
		    speedComponent.speed.set(0, 0);
		    animationComponent.animationTime = 0;
		    animationComponent.playAnimation = false;
		    break;
	    }
	}
    }
}
