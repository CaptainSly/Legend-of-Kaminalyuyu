package com.lok.game;

import java.util.Comparator;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lok.game.ecs.EntityEngine;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.ecs.components.CollisionComponent;
import com.lok.game.ecs.components.IDComponent;
import com.lok.game.ecs.components.MapRevelationComponent;
import com.lok.game.ecs.components.SizeComponent;
import com.lok.game.map.Map;
import com.lok.game.map.Map.Portal;
import com.lok.game.map.MapListener;
import com.lok.game.map.MapManager;

public class GameRenderer extends OrthogonalTiledMapRenderer implements MapListener, EntityListener {
    private final static String TAG = GameRenderer.class.getName();

    private static class yPositionComparator implements Comparator<Entity> {
	private final ComponentMapper<SizeComponent> sizeComponentMapper;

	private yPositionComparator(ComponentMapper<SizeComponent> sizeComponentMapper) {
	    this.sizeComponentMapper = sizeComponentMapper;
	}

	@Override
	public int compare(Entity o1, Entity o2) {
	    if (o1 == o2) {
		return 0;
	    } else if (o1 == null) {
		return -1;
	    } else if (o2 == null) {
		return 1;
	    }

	    return sizeComponentMapper.get(o1).boundingRectangle.y > sizeComponentMapper.get(o2).boundingRectangle.y ? -1 : 1;
	}

    }

    private final Array<Entity>			      entities;
    private SizeComponent			      cameraLockEntitySizeComponent;
    private MapRevelationComponent		      cameraLockEntityRevelationComponent;

    private Array<Rectangle>			      mapCollisionAreas;
    private Array<Portal>			      mapPortals;
    private Color				      mapBackgroundColor;

    private final Array<TiledMapTileLayer>	      backgroundLayers;
    private final Array<TiledMapTileLayer>	      foregroundLayers;

    private final yPositionComparator		      entityComparator;

    private final ComponentMapper<SizeComponent>      sizeComponentMapper;
    private final ComponentMapper<AnimationComponent> animationComponentMapper;

    private final Camera			      camera;
    private final Viewport			      viewport;
    private final Rectangle			      visibleArea;
    private final Rectangle			      scissors;

    private final ShapeRenderer			      shapeRenderer;

    private FrameBuffer				      frameBuffer;
    private final Texture			      lightTexture;

    public GameRenderer() {
	super(null, MapManager.WORLD_UNITS_PER_PIXEL);

	if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
	    Gdx.app.debug(TAG, "Creating in debug mode");
	    shapeRenderer = new ShapeRenderer();
	} else {
	    Gdx.app.debug(TAG, "Creating in non-debug mode");
	    shapeRenderer = null;
	}

	camera = new OrthographicCamera();
	viewport = new FitViewport(32, 18, camera);
	visibleArea = new Rectangle(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
	scissors = new Rectangle();

	this.backgroundLayers = new Array<TiledMapTileLayer>();
	this.foregroundLayers = new Array<TiledMapTileLayer>();

	this.sizeComponentMapper = ComponentMapper.getFor(SizeComponent.class);
	this.animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);

	this.entityComparator = new yPositionComparator(sizeComponentMapper);

	entities = new Array<Entity>(512);
	EntityEngine.getEngine().addEntityListener(Family.one(AnimationComponent.class, SizeComponent.class).get(), this);
	MapManager.getManager().addListener(this);

	lightTexture = AssetManager.getManager().getAsset("lights/light.png", Texture.class);
	frameBuffer = null;
    }

    @Override
    public void onMapChange(MapManager manager, Map map) {
	super.setMap(map.getTiledMap());

	mapCollisionAreas = map.getCollisionAreas();
	mapBackgroundColor = map.getBackgroundColor();
	mapPortals = map.getPortals();

	this.backgroundLayers.clear();
	this.foregroundLayers.clear();
	for (MapLayer mapLayer : map.getTiledMap().getLayers()) {
	    if (mapLayer instanceof TiledMapTileLayer) {
		if (mapLayer.getName().startsWith("background")) {
		    backgroundLayers.add((TiledMapTileLayer) mapLayer);
		} else if (mapLayer.getName().startsWith("foreground")) {
		    foregroundLayers.add((TiledMapTileLayer) mapLayer);
		}
	    }
	}
    }

    public void resize(int width, int height) {
	Gdx.app.debug(TAG, "Resizing to " + width + " x " + height);
	viewport.update(width, height);
	visibleArea.set(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

	if (frameBuffer != null) {
	    frameBuffer.dispose();
	}

	try {
	    frameBuffer = FrameBuffer.createFrameBuffer(Pixmap.Format.RGBA8888, viewport.getScreenWidth(), viewport.getScreenHeight(), false);
	} catch (GdxRuntimeException e) {
	    frameBuffer = FrameBuffer.createFrameBuffer(Pixmap.Format.RGB565, viewport.getScreenWidth(), viewport.getScreenHeight(), false);
	}
    }

    public void lockCameraToEntity(Entity entity) {
	if (entity == null) {
	    cameraLockEntitySizeComponent = null;
	    cameraLockEntityRevelationComponent = null;
	} else {
	    cameraLockEntityRevelationComponent = entity.getComponent(MapRevelationComponent.class);
	    cameraLockEntitySizeComponent = entity.getComponent(SizeComponent.class);

	    if (cameraLockEntitySizeComponent == null) {
		throw new GdxRuntimeException("Trying to lock camera to an entity without size component: " + entity);
	    }
	}
    }

    public void render(float alpha) {
	interpolateEntities(alpha);
	entities.sort(entityComparator);

	prepareLightFrameBuffer();

	Gdx.gl.glClearColor(mapBackgroundColor.r, mapBackgroundColor.g, mapBackgroundColor.b, mapBackgroundColor.a);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

	viewport.calculateScissors(batch.getTransformMatrix(), visibleArea, scissors);
	ScissorStack.pushScissors(scissors);

	if (cameraLockEntitySizeComponent != null) {
	    camera.position.set(cameraLockEntitySizeComponent.interpolatedPosition, 0);
	    visibleArea.setCenter(cameraLockEntitySizeComponent.interpolatedPosition);
	}

	viewport.apply();
	AnimatedTiledMapTile.updateAnimationBaseTime();

	setView(camera.combined, visibleArea.x, visibleArea.y, visibleArea.width, visibleArea.height);
	batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

	batch.begin();

	for (TiledMapTileLayer layer : backgroundLayers) {
	    renderTileLayer(layer);
	}
	for (Entity entity : entities) {
	    renderEntity(entity);
	}
	for (TiledMapTileLayer layer : foregroundLayers) {
	    renderTileLayer(layer);
	}

	if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
	    batch.end();
	    renderDebugInformation();
	    batch.begin();
	}
	batch.end();

	applyLightFrameBuffer();

	ScissorStack.popScissors();
    }

    private void interpolateEntities(float alpha) {
	for (Entity entity : entities) {
	    final SizeComponent sizeComp = sizeComponentMapper.get(entity);

	    final float invAlpha = 1.0f - alpha;
	    sizeComp.interpolatedPosition.x = sizeComp.interpolatedPosition.x * invAlpha + sizeComp.boundingRectangle.x * alpha;
	    sizeComp.interpolatedPosition.y = sizeComp.interpolatedPosition.y * invAlpha + sizeComp.boundingRectangle.y * alpha;
	}
    }

    private void applyLightFrameBuffer() {
	if (cameraLockEntityRevelationComponent != null) {
	    batch.setProjectionMatrix(batch.getProjectionMatrix().idt());
	    batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_COLOR);
	    batch.begin();
	    batch.draw(frameBuffer.getColorBufferTexture(), -1, 1, 2, -2);
	    batch.end();
	}
    }

    private void prepareLightFrameBuffer() {
	if (cameraLockEntityRevelationComponent != null) {
	    frameBuffer.begin();

	    Gdx.gl.glClearColor(mapBackgroundColor.r, mapBackgroundColor.g, mapBackgroundColor.b, mapBackgroundColor.a);
	    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

	    batch.setProjectionMatrix(camera.combined);
	    batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);

	    batch.begin();
	    final Rectangle boundingRectangle = cameraLockEntitySizeComponent.boundingRectangle;
	    batch.draw(lightTexture, cameraLockEntitySizeComponent.interpolatedPosition.x + boundingRectangle.width * 0.5f - cameraLockEntityRevelationComponent.revelationRadius, // x
		    cameraLockEntitySizeComponent.interpolatedPosition.y + boundingRectangle.height * 0.5f - cameraLockEntityRevelationComponent.revelationRadius, // y
		    cameraLockEntityRevelationComponent.revelationRadius * 2f, cameraLockEntityRevelationComponent.revelationRadius * 2f);
	    batch.end();

	    frameBuffer.end();
	}
    }

    private void renderDebugInformation() {
	shapeRenderer.setProjectionMatrix(camera.combined);
	shapeRenderer.begin(ShapeType.Line);

	shapeRenderer.setColor(Color.RED);
	for (Rectangle rect : mapCollisionAreas) {
	    shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
	}

	for (Entity entity : entities) {
	    final CollisionComponent collisionComponent = entity.getComponent(CollisionComponent.class);
	    if (collisionComponent == null) {
		continue;
	    }

	    shapeRenderer.rect(collisionComponent.collisionRectangle.x, collisionComponent.collisionRectangle.y, collisionComponent.collisionRectangle.width,
		    collisionComponent.collisionRectangle.height);
	}

	shapeRenderer.setColor(Color.BLUE);
	for (Portal portal : mapPortals) {
	    shapeRenderer.rect(portal.getArea().x, portal.getArea().y, portal.getArea().width, portal.getArea().height);
	}

	for (Entity entity : entities) {
	    final SizeComponent sizeComp = sizeComponentMapper.get(entity);
	    if (sizeComp == null) {
		continue;
	    }

	    shapeRenderer.rect(sizeComp.boundingRectangle.x, sizeComp.boundingRectangle.y, sizeComp.boundingRectangle.width, sizeComp.boundingRectangle.height);
	}

	if (cameraLockEntityRevelationComponent != null) {
	    shapeRenderer.setColor(Color.WHITE);
	    shapeRenderer.circle(cameraLockEntityRevelationComponent.revelationCircle.x, cameraLockEntityRevelationComponent.revelationCircle.y,
		    cameraLockEntityRevelationComponent.revelationCircle.radius, 64);
	}

	shapeRenderer.end();
    }

    private void renderEntity(Entity entity) {
	final AnimationComponent animationComp = animationComponentMapper.get(entity);

	if (animationComp.animation != null) {
	    final SizeComponent sizeComp = sizeComponentMapper.get(entity);
	    if (!viewBounds.overlaps(sizeComp.boundingRectangle)) {
		return;
	    }

	    if (cameraLockEntityRevelationComponent != null && !Intersector.overlaps(cameraLockEntityRevelationComponent.revelationCircle, sizeComp.boundingRectangle)) {
		return;
	    }

	    final TextureRegion keyFrame = animationComp.animation.getKeyFrame(animationComp.animationTime, true);
	    batch.draw(keyFrame, sizeComp.interpolatedPosition.x, sizeComp.interpolatedPosition.y, sizeComp.boundingRectangle.width, sizeComp.boundingRectangle.height);
	}
    }

    @Override
    public void entityAdded(Entity entity) {
	entities.add(entity);
	if (entity.getComponent(IDComponent.class).entityID == EntityID.PLAYER) {
	    lockCameraToEntity(entity);
	}
    }

    @Override
    public void entityRemoved(Entity entity) {
	entities.removeValue(entity, false);
	if (entity.getComponent(IDComponent.class).entityID == EntityID.PLAYER) {
	    lockCameraToEntity(null);
	}
    }

    @Override
    public void dispose() {
	Gdx.app.debug(TAG, "Disposing Gamerenderer");
	super.dispose();
	if (shapeRenderer != null) {
	    shapeRenderer.dispose();
	}
	if (frameBuffer != null) {
	    frameBuffer.dispose();
	}
    }
}
