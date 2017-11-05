package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.lok.game.ecs.components.AnimationComponent;

public class AnimationSystem extends IteratingSystem {
    private final ComponentMapper<AnimationComponent> animationComponentMapper;

    public AnimationSystem(ComponentMapper<AnimationComponent> animationComponentMapper) {
	super(Family.all(AnimationComponent.class).get());

	this.animationComponentMapper = animationComponentMapper;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	final AnimationComponent animationComponent = animationComponentMapper.get(entity);

	if (animationComponent.animation != null) {
	    animationComponent.animationTime += deltaTime;
	}
    }

}
