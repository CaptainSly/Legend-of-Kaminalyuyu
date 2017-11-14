package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.lok.game.AnimationManager;
import com.lok.game.AnimationManager.AnimationType;
import com.lok.game.ecs.components.AIWanderComponent;
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.ecs.components.SizeComponent;
import com.lok.game.ecs.components.SpeedComponent;
import com.lok.game.map.Map;
import com.lok.game.map.MapListener;
import com.lok.game.map.MapManager;

public class AIWanderSystem extends IteratingSystem implements MapListener {
    private final ComponentMapper<SizeComponent>      sizeComponentMapper;
    private final ComponentMapper<SpeedComponent>     speedComponentMapper;
    private final ComponentMapper<AIWanderComponent>  aiWanderComponentMapper;
    private final ComponentMapper<AnimationComponent> animationComponentMapper;
    private Rectangle				      mapBoundary;
    private Array<Rectangle>			      mapCollisionAreas;

    public AIWanderSystem(ComponentMapper<SizeComponent> sizeComponentMapper, ComponentMapper<AIWanderComponent> aiWanderComponentMapper,
	    ComponentMapper<SpeedComponent> speedComponentMapper, ComponentMapper<AnimationComponent> animationComponentMapper) {
	super(Family.all(AIWanderComponent.class, SpeedComponent.class).get());

	this.sizeComponentMapper = sizeComponentMapper;
	this.aiWanderComponentMapper = aiWanderComponentMapper;
	this.speedComponentMapper = speedComponentMapper;
	this.animationComponentMapper = animationComponentMapper;
	mapBoundary = null;
	mapCollisionAreas = null;

	MapManager.getManager().addListener(this);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	final AIWanderComponent aiWanderComponent = aiWanderComponentMapper.get(entity);
	final SizeComponent sizeComponent = sizeComponentMapper.get(entity);
	final SpeedComponent speedComponent = speedComponentMapper.get(entity);
	final AnimationComponent animationComponent = animationComponentMapper.get(entity);

	if (isOutsideMapOrWithinCollisionArea(sizeComponent.boundingRectangle.x, sizeComponent.boundingRectangle.y)) {
	    // entity leaving map -> send it back
	    if (speedComponent.speed.x > 0) {
		// right -> left
		speedComponent.speed.set(-speedComponent.maxSpeed, 0);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_LEFT);
	    } else if (speedComponent.speed.x < 0) {
		// left -> right
		speedComponent.speed.set(speedComponent.maxSpeed, 0);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_RIGHT);
	    } else if (speedComponent.speed.y > 0) {
		// up -> down
		speedComponent.speed.set(0, -speedComponent.maxSpeed);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_DOWN);
	    } else if (speedComponent.speed.y < 0) {
		// down -> up
		speedComponent.speed.set(0, speedComponent.maxSpeed);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_UP);
	    }
	}

	aiWanderComponent.wanderTime -= deltaTime;
	if (aiWanderComponent.wanderTime <= 0) {
	    aiWanderComponent.wanderTime = MathUtils.random(1.5f, 7.5f);

	    switch (MathUtils.random(4)) {
		case 0:
		    // go right
		    speedComponent.speed.set(speedComponent.maxSpeed, 0);
		    animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_RIGHT);
		    break;
		case 1:
		    // go left
		    speedComponent.speed.set(-speedComponent.maxSpeed, 0);
		    animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_LEFT);
		    break;
		case 2:
		    // go up
		    speedComponent.speed.set(0, speedComponent.maxSpeed);
		    animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_UP);
		    break;
		case 3:
		    // go down
		    speedComponent.speed.set(0, -speedComponent.maxSpeed);
		    animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_DOWN);
		    break;
		case 4:
		    // idle
		    speedComponent.speed.set(0, 0);
		    animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.IDLE);
		    break;
	    }
	}
    }

    private boolean isOutsideMapOrWithinCollisionArea(float x, float y) {
	if (mapBoundary != null && !mapBoundary.contains(x, y)) {
	    return true;
	}

	if (mapCollisionAreas == null) {
	    return false;
	}

	for (Rectangle collisionArea : mapCollisionAreas) {
	    if (collisionArea.contains(x, y)) {
		return true;
	    }
	}

	return false;
    }

    @Override
    public void onMapChange(MapManager manager, Map map) {
	mapBoundary = map.getBoundary();
	mapCollisionAreas = map.getCollisionAreas();
    }
}
