package com.lok.game.ecs;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.TimeUtils;
import com.lok.game.AnimationManager.AnimationID;
import com.lok.game.Utils;
import com.lok.game.ecs.components.AIWanderComponent;
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.ecs.components.PositionComponent;
import com.lok.game.ecs.components.SpeedComponent;
import com.lok.game.ecs.systems.AIWanderSystem;
import com.lok.game.ecs.systems.AnimationSystem;
import com.lok.game.ecs.systems.MovementSystem;

public class EntityEngine {
    public enum EntityID {
	HERO
    }

    private static class EntityConfiguration {
	private EntityID    entityID;
	private AnimationID animationID;
	private float	    speed;
    }

    private static final String		     TAG      = EntityEngine.class.getName();
    private static EntityEngine		     instance = null;

    private final PooledEngine		     engine;
    private final Array<EntityConfiguration> entityConfigurationCache;

    private EntityEngine() {
	entityConfigurationCache = new Array<EntityConfiguration>(EntityID.values().length);
	loadEntityConfigurations();

	engine = new PooledEngine(128, 512, 512, 2048);

	final ComponentMapper<PositionComponent> positionComponentMapper = ComponentMapper.getFor(PositionComponent.class);
	final ComponentMapper<SpeedComponent> speedComponentMapper = ComponentMapper.getFor(SpeedComponent.class);
	final ComponentMapper<AIWanderComponent> aiWanderComponentMapper = ComponentMapper.getFor(AIWanderComponent.class);
	final ComponentMapper<AnimationComponent> animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);

	engine.addSystem(new MovementSystem(positionComponentMapper, speedComponentMapper));
	engine.addSystem(new AIWanderSystem(positionComponentMapper, aiWanderComponentMapper, speedComponentMapper, animationComponentMapper));
	engine.addSystem(new AnimationSystem(animationComponentMapper));
    }

    public static EntityEngine getEngine() {
	if (instance == null) {
	    instance = new EntityEngine();
	}

	return instance;
    }

    private void loadEntityConfigurations() {
	final long startTime = TimeUtils.millis();
	final Object fromJson = Utils.fromJson(Gdx.files.internal("json/hero.json"));
	final Array<EntityConfiguration> configsInFile = new Array<EntityConfiguration>();
	if (fromJson instanceof Array<?>) {
	    // multiple entity types defined within file
	    for (Object val : (Array<?>) fromJson) {
		configsInFile.add(Utils.readJsonValue(EntityConfiguration.class, (JsonValue) val));
	    }
	} else {
	    // only one entity type defined -> load it
	    configsInFile.add(Utils.readJsonValue(EntityConfiguration.class, (JsonValue) fromJson));
	}

	for (EntityID entityID : EntityID.values()) {
	    boolean foundEntityConfig = false;

	    for (EntityConfiguration entityCfg : configsInFile) {
		if (!entityID.equals(entityCfg.entityID)) {
		    continue;
		}

		foundEntityConfig = true;
		entityConfigurationCache.add(entityCfg);
	    }

	    if (!foundEntityConfig) {
		throw new GdxRuntimeException("Missing EntityConfiguration for " + entityID);
	    }
	}

	Gdx.app.debug(TAG, "Loaded all entity configurations in " + TimeUtils.timeSinceMillis(startTime) / 1000.0f);
    }

    public void update(float deltaTime) {
	engine.update(deltaTime);
    }

    public void addEntityListener(Family family, EntityListener listener) {
	engine.addEntityListener(family, listener);
    }

    public <T extends Component> T createComponent(Class<T> componentType) {
	Gdx.app.debug(TAG, "Creating component " + componentType);

	return engine.createComponent(componentType);
    }

    public void clear() {
	Gdx.app.debug(TAG, "Clearing entity engine pools");

	engine.clearPools();
    }

    public Entity createEntity(EntityID entityID, float x, float y) {
	Gdx.app.debug(TAG, "Creating entity " + entityID + " at location (" + x + "/" + y + ")");

	final EntityConfiguration entityConfig = entityConfigurationCache.get(entityID.ordinal());
	final Entity entity = engine.createEntity();

	final PositionComponent positionComponent = engine.createComponent(PositionComponent.class);
	positionComponent.position.set(x, y);
	positionComponent.previousPosition.set(x, y);
	entity.add(positionComponent);

	if (entityConfig.speed != 0) {
	    final SpeedComponent speedComponent = engine.createComponent(SpeedComponent.class);
	    speedComponent.maxSpeed = entityConfig.speed;
	    entity.add(speedComponent);
	}

	if (entityConfig.animationID != null) {
	    final AnimationComponent animationComponent = engine.createComponent(AnimationComponent.class);
	    animationComponent.animationID = entityConfig.animationID;
	    entity.add(animationComponent);

	}

	engine.addEntity(entity);
	return entity;
    }

    public void removeEntity(Entity entity) {
	Gdx.app.debug(TAG, "Removing entity " + entity);

	engine.removeEntity(entity);
    }
}
