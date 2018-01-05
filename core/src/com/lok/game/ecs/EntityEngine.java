package com.lok.game.ecs;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.lok.game.AnimationManager;
import com.lok.game.AnimationManager.AnimationID;
import com.lok.game.AnimationManager.AnimationType;
import com.lok.game.Utils;
import com.lok.game.ability.Ability.AbilityID;
import com.lok.game.ability.AbilitySystem;
import com.lok.game.conversation.Conversation.ConversationID;
import com.lok.game.ecs.components.AIWanderComponent;
import com.lok.game.ecs.components.AbilityComponent;
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.ecs.components.CollisionComponent;
import com.lok.game.ecs.components.ConversationComponent;
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
import com.lok.game.map.MapManager;

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

    private static class EntityConfiguration {
	private EntityID	 entityID;
	private AnimationID	 animationID;
	private Vector2		 originPoint;
	private float		 speed;
	private float		 revelationRadius;
	private Vector2		 size;
	private Rectangle	 collisionRectangle;
	private Array<String>	 additionalComponents;
	private ConversationID	 conversationID;
	private String		 conversationImage;
	private Array<AbilityID> abilities;
    }

    private static final String	       TAG	= EntityEngine.class.getName();
    private static EntityEngine	       instance	= null;

    private final PooledEngine	       engine;
    private Array<EntityConfiguration> entityConfigurationCache;
    private final AbilitySystem	       abilitySystem;

    private EntityEngine() {
	entityConfigurationCache = null;
	engine = new PooledEngine(128, 512, 512, 2048);

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

    private void loadEntityConfigurations() {
	final long startTime = TimeUtils.millis();
	entityConfigurationCache = new Array<EntityConfiguration>();

	// parse player
	Object fromJson = Utils.fromJson(Gdx.files.internal("json/player.json"));
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

	// parse townfolk entities
	fromJson = Utils.fromJson(Gdx.files.internal("json/townfolk.json"));
	if (fromJson instanceof Array<?>) {
	    // multiple entity types defined within file
	    for (Object val : (Array<?>) fromJson) {
		configsInFile.add(Utils.readJsonValue(EntityConfiguration.class, (JsonValue) val));
	    }
	} else {
	    // only one entity type defined -> load it
	    configsInFile.add(Utils.readJsonValue(EntityConfiguration.class, (JsonValue) fromJson));
	}

	// parse monster entities
	fromJson = Utils.fromJson(Gdx.files.internal("json/monsters.json"));
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
	abilitySystem.update(deltaTime);
	engine.update(deltaTime);
    }

    public void addEntityListener(Family family, EntityListener listener) {
	engine.addEntityListener(family, listener);
    }

    public void removeEntityListener(EntityListener listener) {
	engine.removeEntityListener(listener);
    }

    public <T extends Component> T createComponent(Class<T> componentType) {
	Gdx.app.debug(TAG, "Creating component " + componentType);

	return engine.createComponent(componentType);
    }

    public AbilitySystem getAbilitySystem() {
	return abilitySystem;
    }

    public <T extends EntitySystem> T getSystem(Class<T> systemType) {
	return engine.getSystem(systemType);
    }

    public void removeAllEntities() {
	Gdx.app.debug(TAG, "Removing all entities from engine");
	engine.removeAllEntities();
    }

    public Entity createEntity(EntityID entityID, float x, float y) {
	if (entityConfigurationCache == null) {
	    loadEntityConfigurations();
	}

	Gdx.app.debug(TAG, "Creating entity " + entityID + " at location (" + x + "/" + y + ")");

	final EntityConfiguration entityConfig = entityConfigurationCache.get(entityID.ordinal());
	final Entity entity = engine.createEntity();

	final IDComponent idComponent = engine.createComponent(IDComponent.class);
	idComponent.entityID = entityID;
	entity.add(idComponent);

	createSizeComponentIfNeeded(entityConfig, entity, x, y);
	createSpeedComponentIfNeeded(entityConfig, entity);
	createAnimationComponentIfNeeded(entityConfig, entity);
	createMapRevelationComponentIfNeeded(entityConfig, entity);
	createCollisionComponentIfNeeded(entityConfig, entity, x, y);
	createConversationComponentIfNeeded(entityConfig, entity);
	createAbilityComponentIfNeeded(entityConfig, entity);
	createAdditionalComponentsIfNeeded(entityConfig, entity);

	engine.addEntity(entity);
	return entity;
    }

    private void createSizeComponentIfNeeded(EntityConfiguration entityConfig, Entity entity, float x, float y) {
	if (entityConfig.size != null) {
	    final SizeComponent sizeComponent = engine.createComponent(SizeComponent.class);
	    if (entityConfig.size != null) {
		sizeComponent.boundingRectangle.set(x, y, entityConfig.size.x, entityConfig.size.y);
	    } else {
		sizeComponent.boundingRectangle.set(x, y, 0, 0);
	    }
	    sizeComponent.interpolatedPosition.set(x, y);
	    entity.add(sizeComponent);
	}
    }

    private void createCollisionComponentIfNeeded(EntityConfiguration entityConfig, Entity entity, float x, float y) {
	if (entityConfig.collisionRectangle != null) {
	    final CollisionComponent collisionComponent = engine.createComponent(CollisionComponent.class);
	    collisionComponent.rectOffset.set(entityConfig.collisionRectangle.x * MapManager.WORLD_UNITS_PER_PIXEL,
		    entityConfig.collisionRectangle.y * MapManager.WORLD_UNITS_PER_PIXEL);

	    collisionComponent.collisionRectangle.set(x + collisionComponent.rectOffset.x, y + collisionComponent.rectOffset.y, // position
		    entityConfig.collisionRectangle.width * MapManager.WORLD_UNITS_PER_PIXEL, // width
		    entityConfig.collisionRectangle.height * MapManager.WORLD_UNITS_PER_PIXEL); // height

	    entity.add(collisionComponent);
	}
    }

    private void createSpeedComponentIfNeeded(EntityConfiguration entityConfig, Entity entity) {
	if (entityConfig.speed != 0) {
	    final SpeedComponent speedComponent = engine.createComponent(SpeedComponent.class);
	    speedComponent.maxSpeed = entityConfig.speed;
	    entity.add(speedComponent);
	}
    }

    private void createAnimationComponentIfNeeded(EntityConfiguration entityConfig, Entity entity) {
	if (entityConfig.animationID != null) {
	    final AnimationComponent animationComponent = engine.createComponent(AnimationComponent.class);
	    animationComponent.animationID = entityConfig.animationID;
	    if (entityConfig.originPoint != null) {
		animationComponent.originPoint.set(entityConfig.originPoint.x * MapManager.WORLD_UNITS_PER_PIXEL, entityConfig.originPoint.y * MapManager.WORLD_UNITS_PER_PIXEL);
	    }
	    animationComponent.animation = AnimationManager.getManager().getAnimation(animationComponent.animationID, AnimationType.IDLE);
	    entity.add(animationComponent);
	}
    }

    private void createMapRevelationComponentIfNeeded(EntityConfiguration entityConfig, Entity entity) {
	if (entityConfig.revelationRadius > 0) {
	    final MapRevelationComponent mapRevelationComponent = engine.createComponent(MapRevelationComponent.class);
	    mapRevelationComponent.revelationRadius = entityConfig.revelationRadius;
	    mapRevelationComponent.minRevelationRadius = entityConfig.revelationRadius - 0.25f;
	    mapRevelationComponent.maxRevelationRadius = entityConfig.revelationRadius + 0.25f;
	    mapRevelationComponent.incPerFrame = 2f;
	    entity.add(mapRevelationComponent);
	}
    }

    private void createConversationComponentIfNeeded(EntityConfiguration entityConfig, Entity entity) {
	if (entityConfig.conversationImage != null || entityConfig.conversationID != null) {
	    final ConversationComponent convComponent = engine.createComponent(ConversationComponent.class);
	    convComponent.currentConversationID = entityConfig.conversationID;
	    convComponent.conversationImage = entityConfig.conversationImage;
	    entity.add(convComponent);
	}
    }

    private void createAbilityComponentIfNeeded(EntityConfiguration entityConfig, Entity entity) {
	if (entityConfig.abilities != null) {
	    final AbilityComponent abilityComponent = engine.createComponent(AbilityComponent.class);
	    for (AbilityID ability : entityConfig.abilities) {
		abilityComponent.abilities.add(ability);
	    }
	    entity.add(abilityComponent);
	}
    }

    @SuppressWarnings("unchecked")
    private void createAdditionalComponentsIfNeeded(EntityConfiguration entityConfig, Entity entity) {
	if (entityConfig.additionalComponents != null) {
	    for (String additionalComponent : entityConfig.additionalComponents) {
		try {
		    entity.add(engine.createComponent(ClassReflection.forName(additionalComponent)));
		} catch (Exception e) {
		    throw new GdxRuntimeException("Could not create component " + additionalComponent + " for entity " + entity.getComponent(IDComponent.class).entityID, e);
		}
	    }
	}
    }

    public void removeEntity(Entity entity) {
	Gdx.app.debug(TAG, "Removing entity " + entity);

	engine.removeEntity(entity);
    }
}
