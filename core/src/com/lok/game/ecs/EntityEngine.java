package com.lok.game.ecs;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.lok.game.Utils;
import com.lok.game.ability.AbilitySystem;
import com.lok.game.ecs.components.AIWanderComponent;
import com.lok.game.ecs.components.AbilityComponent;
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.ecs.components.CollisionComponent;
import com.lok.game.ecs.components.Component;
import com.lok.game.ecs.components.IDComponent;
import com.lok.game.ecs.components.MapRevelationComponent;
import com.lok.game.ecs.components.SizeComponent;
import com.lok.game.ecs.components.SpeedComponent;
import com.lok.game.ecs.systems.AIWanderSystem;
import com.lok.game.ecs.systems.AnimationSystem;
import com.lok.game.ecs.systems.CastSystem;
import com.lok.game.ecs.systems.CollisionSystem;
import com.lok.game.ecs.systems.MapRevelationSystem;
import com.lok.game.ecs.systems.MovementSystem;

public class EntityEngine {
    public static enum EntityID {
	PLAYER,
	DEMON_01,
	BOSS_01,
	ELDER,
	BLACKSMITH,
	SHAMAN,
	PORTAL
    }

    private static final String	       TAG	= EntityEngine.class.getName();
    private static EntityEngine	       instance	= null;

    private final PooledEngine	       engine;
    private Array<EntityConfiguration> entityConfigurationCache;
    private final AbilitySystem	       abilitySystem;

    private EntityEngine() {
	entityConfigurationCache = null;
	engine = new PooledEngine(64, 128, 512, 1024);

	final ComponentMapper<IDComponent> idComponentMapper = ComponentMapper.getFor(IDComponent.class);
	final ComponentMapper<SpeedComponent> speedComponentMapper = ComponentMapper.getFor(SpeedComponent.class);
	final ComponentMapper<AIWanderComponent> aiWanderComponentMapper = ComponentMapper.getFor(AIWanderComponent.class);
	final ComponentMapper<AnimationComponent> animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);
	final ComponentMapper<MapRevelationComponent> mapRevelationComponentMapper = ComponentMapper.getFor(MapRevelationComponent.class);
	final ComponentMapper<CollisionComponent> collisionComponentMapper = ComponentMapper.getFor(CollisionComponent.class);
	final ComponentMapper<SizeComponent> sizeComponentMapper = ComponentMapper.getFor(SizeComponent.class);
	final ComponentMapper<AbilityComponent> abilityComponentMapper = ComponentMapper.getFor(AbilityComponent.class);

	engine.addSystem(new MovementSystem(speedComponentMapper, collisionComponentMapper, sizeComponentMapper));
	engine.addSystem(new CollisionSystem(idComponentMapper, collisionComponentMapper));
	engine.addSystem(new AnimationSystem(animationComponentMapper));
	this.abilitySystem = new AbilitySystem(abilityComponentMapper);
	engine.addSystem(new CastSystem(abilityComponentMapper, abilitySystem));
	engine.addSystem(new MapRevelationSystem(sizeComponentMapper, mapRevelationComponentMapper));
	engine.addSystem(new AIWanderSystem(aiWanderComponentMapper, speedComponentMapper, animationComponentMapper));
    }

    public static EntityEngine getEngine() {
	if (instance == null) {
	    instance = new EntityEngine();
	}

	return instance;
    }

    public void update(float deltaTime) {
	abilitySystem.update(deltaTime);
	engine.update(deltaTime);
    }

    public void addEntityListener(Family family, EntityListener listener) {
	engine.addEntityListener(family, listener);
    }

    public void removeEntityListener(EntityListener listener) {
	engine.removeEntityListener(listener);
    }

    public AbilitySystem getAbilitySystem() {
	return abilitySystem;
    }

    public <T extends EntitySystem> T getSystem(Class<T> systemType) {
	return engine.getSystem(systemType);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Entity createEntity(EntityID entityID, float x, float y) {
	if (entityConfigurationCache == null) {
	    Gdx.app.debug(TAG, "Initializing entity configuration cache");
	    entityConfigurationCache = new Array<EntityConfiguration>();
	    final AssetManager assetManager = Utils.getAssetManager();
	    for (EntityID id : EntityID.values()) {
		entityConfigurationCache.add(assetManager.get(id.name(), EntityConfiguration.class));
	    }
	}

	Gdx.app.debug(TAG, "Creating entity " + entityID + " at location (" + x + "/" + y + ")");

	final Entity entity = engine.createEntity();

	final IDComponent idComponent = engine.createComponent(IDComponent.class);
	idComponent.entityID = entityID;
	entity.add(idComponent);

	final EntityConfiguration components = entityConfigurationCache.get(entityID.ordinal());
	for (Component component : components) {
	    final Component entityComponent = engine.createComponent(component.getClass());
	    entityComponent.initialize(component);

	    if (entityComponent instanceof SizeComponent) {
		final SizeComponent sizeComp = (SizeComponent) entityComponent;
		sizeComp.boundingRectangle.setPosition(x, y);
		sizeComp.interpolatedPosition.set(x, y);
	    } else if (entityComponent instanceof CollisionComponent) {
		final CollisionComponent collisionComp = (CollisionComponent) entityComponent;
		collisionComp.collisionRectangle.setPosition(x + collisionComp.rectOffset.x, y + collisionComp.rectOffset.y);
	    }

	    entity.add(entityComponent);
	}

	engine.addEntity(entity);
	return entity;
    }

    public void removeEntity(Entity entity) {
	Gdx.app.debug(TAG, "Removing entity " + entity.getComponent(IDComponent.class).entityID);

	engine.removeEntity(entity);
    }
}
