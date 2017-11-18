package com.lok.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.lok.game.ecs.EntityEngine;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.ecs.components.IDComponent;
import com.lok.game.ecs.systems.CollisionSystem;
import com.lok.game.ecs.systems.CollisionSystem.CollisionListener;
import com.lok.game.map.Map;
import com.lok.game.map.Map.Portal;
import com.lok.game.map.MapListener;
import com.lok.game.map.MapManager;
import com.lok.game.map.MapManager.MapID;

public class GameLogic implements EntityListener, CollisionListener, MapListener {
    private final GameRenderer	     renderer;
    private final GameInputProcessor inputProcessor;

    public GameLogic(GameRenderer gameRenderer) {
	this.renderer = gameRenderer;
	inputProcessor = new GameInputProcessor();

	EntityEngine.getEngine().addEntityListener(Family.all(IDComponent.class).get(), this);
	EntityEngine.getEngine().getSystem(CollisionSystem.class).addCollisionListener(this);
	MapManager.getManager().addListener(this);
    }

    public void show() {
	Gdx.input.setInputProcessor(inputProcessor);
	MapManager.getManager().changeMap(MapID.DEMON_LAIR_01);

    }

    public void hide() {
	Gdx.input.setInputProcessor(null);
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
		renderer.lockCameraToEntity(entity);
		inputProcessor.setPlayer(entity);
	    }
	}
    }

    @Override
    public void entityRemoved(Entity entity) {
	if (entity.getComponent(IDComponent.class).entityID == EntityID.PLAYER) {
	    if (entity.getComponent(IDComponent.class).entityID == EntityID.PLAYER) {
		renderer.lockCameraToEntity(null);
		inputProcessor.setPlayer(null);
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

}
