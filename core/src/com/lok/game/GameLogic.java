package com.lok.game;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.lok.game.AnimationManager.AnimationType;
import com.lok.game.ecs.EntityEngine;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.ecs.components.IDComponent;
import com.lok.game.ecs.components.SpeedComponent;
import com.lok.game.ecs.systems.CollisionSystem;
import com.lok.game.ecs.systems.CollisionSystem.CollisionListener;
import com.lok.game.map.Map;
import com.lok.game.map.Map.Portal;
import com.lok.game.map.MapListener;
import com.lok.game.map.MapManager;
import com.lok.game.map.MapManager.MapID;
import com.lok.game.screens.ScreenManager;
import com.lok.game.screens.TownScreen;
import com.lok.game.ui.PlayerHUD.HUDEvent;
import com.lok.game.ui.PlayerHUD.HUDEventListener;

public class GameLogic implements EntityListener, CollisionListener, MapListener, HUDEventListener {
    private final GameRenderer			      renderer;
    private final ComponentMapper<SpeedComponent>     speedComponentMapper;
    private final ComponentMapper<AnimationComponent> animationComponentMapper;
    private Entity				      player;

    public GameLogic(GameRenderer gameRenderer) {
	this.renderer = gameRenderer;
	this.player = null;
	this.speedComponentMapper = ComponentMapper.getFor(SpeedComponent.class);
	this.animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);

	EntityEngine.getEngine().addEntityListener(Family.all(IDComponent.class).get(), this);
	EntityEngine.getEngine().getSystem(CollisionSystem.class).addCollisionListener(this);
	MapManager.getManager().addListener(this);
    }

    public void show() {
	MapManager.getManager().changeMap(MapID.DEMON_LAIR_01);

    }

    public void hide() {
    }

    @Override
    public void onEntityCollision(EntityID entityIDA, Entity entityA, EntityID entityIDB, Entity entityB) {
	if (entityIDA == EntityID.PLAYER || entityIDB == EntityID.PLAYER) {
	    Gdx.app.log("DEBUG", "Combat between " + entityIDA + " and " + entityIDB);
	}
    }

    @Override
    public void onPortalCollision(EntityID entityID, Entity entity, Portal portal) {
	if (entityID == EntityID.PLAYER) {
	    portal.activate(entity);
	}
    }

    @Override
    public void entityAdded(Entity entity) {
	if (entity.getComponent(IDComponent.class).entityID == EntityID.PLAYER) {
	    if (entity.getComponent(IDComponent.class).entityID == EntityID.PLAYER) {
		this.player = entity;
		renderer.lockCameraToEntity(entity);
	    }
	}
    }

    @Override
    public void entityRemoved(Entity entity) {
	if (entity.getComponent(IDComponent.class).entityID == EntityID.PLAYER) {
	    if (entity.getComponent(IDComponent.class).entityID == EntityID.PLAYER) {
		this.player = null;
		renderer.lockCameraToEntity(null);
	    }
	}
    }

    @Override
    public void onMapChange(MapManager manager, Map map) {
	renderer.setMap(map);
    }

    public void pause() {
	// TODO Auto-generated method stub

    }

    public void resume() {
	// TODO Auto-generated method stub

    }

    @Override
    public void onHUDEvent(HUDEvent event) {
	if (player == null) {
	    return;
	}

	final SpeedComponent speedComponent = speedComponentMapper.get(player);
	final AnimationComponent animationComponent = animationComponentMapper.get(player);
	switch (event) {
	    case MOVE_DOWN:
		speedComponent.speed.set(0, -speedComponent.maxSpeed);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_DOWN);
		animationComponent.playAnimation = true;
		break;
	    case MOVE_LEFT:
		speedComponent.speed.set(-speedComponent.maxSpeed, 0);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_LEFT);
		animationComponent.playAnimation = true;
		break;
	    case MOVE_RIGHT:
		speedComponent.speed.set(speedComponent.maxSpeed, 0);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_RIGHT);
		animationComponent.playAnimation = true;
		break;
	    case MOVE_UP:
		speedComponent.speed.set(0, speedComponent.maxSpeed);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_UP);
		animationComponent.playAnimation = true;
		break;
	    case MOVE_STOP:
		speedComponent.speed.set(0, 0);
		animationComponent.animationTime = 0;
		animationComponent.playAnimation = false;
		break;
	    case PORT_TO_TOWN:
		ScreenManager.getManager().setScreen(TownScreen.class);
		break;
	}
    }

}
