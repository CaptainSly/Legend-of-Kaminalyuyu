package com.lok.game.screen;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.lok.game.AnimationManager;
import com.lok.game.AnimationManager.AnimationType;
import com.lok.game.GameRenderer;
import com.lok.game.ability.Ability;
import com.lok.game.ecs.EntityEngine;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.ecs.components.AbilityComponent;
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.ecs.components.IDComponent;
import com.lok.game.ecs.components.SpeedComponent;
import com.lok.game.ecs.systems.AbilitySystem;
import com.lok.game.ecs.systems.AbilitySystem.AbilityListener;
import com.lok.game.ecs.systems.CollisionSystem;
import com.lok.game.ecs.systems.CollisionSystem.CollisionListener;
import com.lok.game.map.Map;
import com.lok.game.map.Map.Portal;
import com.lok.game.map.MapListener;
import com.lok.game.map.MapManager;
import com.lok.game.map.MapManager.MapID;
import com.lok.game.ui.GameUI;
import com.lok.game.ui.UIEventListener;

public class GameScreen implements Screen, UIEventListener, EntityListener, CollisionListener, MapListener, AbilityListener {
    private float				      accumulator;
    private final float				      fixedPhysicsStep;

    private final GameRenderer			      renderer;
    private final EntityEngine			      entityEngine;
    private final GameUI			      gameUI;

    private final ComponentMapper<SpeedComponent>     speedComponentMapper;
    private final ComponentMapper<AnimationComponent> animationComponentMapper;
    private final ComponentMapper<AbilityComponent>   abilityComponentMapper;
    private Entity				      player;

    public GameScreen() {
	fixedPhysicsStep = 1.0f / 30.0f;
	accumulator = 0.0f;

	this.gameUI = new GameUI();
	this.gameUI.addUIEventListener(this);
	this.entityEngine = EntityEngine.getEngine();
	this.renderer = new GameRenderer();
	this.player = null;
	this.speedComponentMapper = ComponentMapper.getFor(SpeedComponent.class);
	this.animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);
	this.abilityComponentMapper = ComponentMapper.getFor(AbilityComponent.class);

	EntityEngine.getEngine().addEntityListener(Family.all(IDComponent.class).get(), this);
	EntityEngine.getEngine().getSystem(CollisionSystem.class).addCollisionListener(this);
	EntityEngine.getEngine().getSystem(AbilitySystem.class).addAbilityListener(this);
	MapManager.getManager().addListener(this);
    }

    @Override
    public void show() {
	MapManager.getManager().changeMap(MapID.DEMON_LAIR_01);
	gameUI.show();
    }

    @Override
    public void render(float delta) {
	if (delta > 0.25f) {
	    delta = 0.25f;
	}

	accumulator += delta;
	while (accumulator >= fixedPhysicsStep) {
	    entityEngine.update(fixedPhysicsStep);
	    accumulator -= fixedPhysicsStep;
	}

	renderer.render(accumulator / fixedPhysicsStep);
	gameUI.render(delta);
    }

    @Override
    public void resize(int width, int height) {
	gameUI.resize(width, height);
	renderer.resize(width, height);
    }

    @Override
    public void pause() {
	// TODO
    }

    @Override
    public void resume() {
	// TODO
    }

    @Override
    public void hide() {
	gameUI.hide();
    }

    @Override
    public void dispose() {
	gameUI.dispose();
	renderer.dispose();
    }

    @Override
    public void onUIEvent(Actor triggerActor, UIEvent event) {
	if (player == null) {
	    return;
	}

	final SpeedComponent speedComponent = speedComponentMapper.get(player);
	final AnimationComponent animationComponent = animationComponentMapper.get(player);
	final AbilityComponent abilityComponent = abilityComponentMapper.get(player);
	switch (event) {
	    case DOWN:
		abilityComponent.abilityToCast = null;
		speedComponent.speed.set(0, -speedComponent.maxSpeed);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_DOWN);
		animationComponent.playAnimation = true;
		break;
	    case LEFT:
		abilityComponent.abilityToCast = null;
		speedComponent.speed.set(-speedComponent.maxSpeed, 0);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_LEFT);
		animationComponent.playAnimation = true;
		break;
	    case RIGHT:
		abilityComponent.abilityToCast = null;
		speedComponent.speed.set(speedComponent.maxSpeed, 0);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_RIGHT);
		animationComponent.playAnimation = true;
		break;
	    case UP:
		abilityComponent.abilityToCast = null;
		speedComponent.speed.set(0, speedComponent.maxSpeed);
		animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.WALK_UP);
		animationComponent.playAnimation = true;
		break;
	    case STOP_MOVEMENT:
		abilityComponent.abilityToCast = null;
		speedComponent.speed.set(0, 0);
		animationComponent.animationTime = 0;
		animationComponent.playAnimation = false;
		break;
	    case CAST:
		abilityComponent.abilityToCast = abilityComponent.abilities.get(0);
		break;
	    case STOP_CAST:
		abilityComponent.abilityToCast = null;
		break;
	    default:
		break;
	}
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

    @Override
    public void onStartCast(Entity entity, Ability ability) {
	gameUI.showAbilityChannelBar("Stadtportal", ability.getMaxChannelTime());
    }

    @Override
    public void onSopCast(Entity entity, Ability ability) {
	gameUI.hideAbilityChannelBar();
    }

}
