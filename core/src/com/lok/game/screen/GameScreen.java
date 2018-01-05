package com.lok.game.screen;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.lok.game.AnimationManager;
import com.lok.game.AnimationManager.AnimationType;
import com.lok.game.GameRenderer;
import com.lok.game.PreferencesManager;
import com.lok.game.PreferencesManager.PreferencesListener;
import com.lok.game.Utils;
import com.lok.game.ability.Ability;
import com.lok.game.ability.Ability.AbilityID;
import com.lok.game.ability.Ability.AbilityListener;
import com.lok.game.ecs.EntityEngine;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.ecs.components.AbilityComponent;
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.ecs.components.IDComponent;
import com.lok.game.ecs.components.SpeedComponent;
import com.lok.game.ecs.systems.CollisionSystem;
import com.lok.game.ecs.systems.CollisionSystem.CollisionListener;
import com.lok.game.map.Map;
import com.lok.game.map.Map.Portal;
import com.lok.game.map.MapListener;
import com.lok.game.map.MapManager;
import com.lok.game.ui.GameUI;
import com.lok.game.ui.UIEventListener;

public class GameScreen implements Screen, UIEventListener, EntityListener, CollisionListener, MapListener, AbilityListener, PreferencesListener {
    private float				      accumulator;
    private final float				      fixedPhysicsStep;

    private GameRenderer			      renderer;
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
	this.entityEngine = EntityEngine.getEngine();
	this.renderer = null;
	this.player = null;
	this.speedComponentMapper = ComponentMapper.getFor(SpeedComponent.class);
	this.animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);
	this.abilityComponentMapper = ComponentMapper.getFor(AbilityComponent.class);
    }

    @Override
    public void show() {
	if (renderer == null) {
	    renderer = new GameRenderer();
	}

	entityEngine.removeAllEntities();
	this.gameUI.addUIEventListener(this);
	entityEngine.addEntityListener(Family.all(IDComponent.class).get(), this);
	entityEngine.getSystem(CollisionSystem.class).addCollisionListener(this);
	entityEngine.getAbilitySystem().addAbilityListener(this);
	MapManager.getManager().addMapListener(this);
	PreferencesManager.getManager().addPreferencesListener(MapManager.getManager());
	PreferencesManager.getManager().addPreferencesListener(this);

	PreferencesManager.getManager().loadGameState();
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
	PreferencesManager.getManager().saveGameState();

	entityEngine.removeEntityListener(this);
	entityEngine.getSystem(CollisionSystem.class).removeCollisionListener(this);
	entityEngine.getAbilitySystem().removeAbilityListener(this);
	MapManager.getManager().removeMapListener(this);
	PreferencesManager.getManager().removePreferencesListener(this);
	PreferencesManager.getManager().removePreferencesListener(MapManager.getManager());

	gameUI.hide();
	this.gameUI.removeUIEventListener(this);
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
		abilityComponent.abilityToCast = (AbilityID) triggerActor.getUserObject();
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
    public void onStartCast(Entity caster, Ability ability) {
	gameUI.showAbilityChannelBar(Utils.getLabel("Ability." + ability.getAbilityID().name() + ".name"), ability.getEffectDelayTime());
    }

    @Override
    public void onUpdateAbility(Entity caster, Ability ability) {
	gameUI.setAbilityChannelBarValue(ability.getChannelTime());
    }

    @Override
    public void onSopCast(Entity caster, Ability ability) {
	gameUI.hideAbilityChannelBar();
    }

    @Override
    public void onSave(Json json, Preferences preferences) {
	preferences.putString("GameScreen-playerAbilities", json.toJson(abilityComponentMapper.get(player).abilities));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onLoad(Json json, Preferences preferences) {
	if (preferences.contains("GameScreen-playerAbilities")) {
	    abilityComponentMapper.get(player).abilities = json.fromJson(Array.class, preferences.getString("GameScreen-playerAbilities"));
	}
    }

}
