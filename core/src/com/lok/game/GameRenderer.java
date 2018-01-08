package com.lok.game;

import java.util.Comparator;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
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
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.ecs.components.CollisionComponent;
import com.lok.game.ecs.components.MapRevelationComponent;
import com.lok.game.ecs.components.SizeComponent;
import com.lok.game.map.Map;
import com.lok.game.map.MapManager;
import com.lok.game.map.Portal;

public class GameRenderer extends OrthogonalTiledMapRenderer {
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

    private SizeComponent			      cameraLockEntitySizeComponent;
    private MapRevelationComponent		      cameraLockEntityRevelationComponent;

    private Map					      map;
    private TiledMapTileLayer			      groundLayer;
    private final Array<TiledMapTileLayer>	      backgroundLayers;
    private final Array<TiledMapTileLayer>	      foregroundLayers;
    private TiledMapImageLayer			      lightMapLayer;

    private final yPositionComparator		      entityComparator;

    private final ComponentMapper<SizeComponent>      sizeComponentMapper;
    private final ComponentMapper<AnimationComponent> animationComponentMapper;

    private final Camera			      camera;
    private final Viewport			      viewport;
    private final Rectangle			      visibleArea;
    private final Rectangle			      scissors;

    private final ShapeRenderer			      shapeRenderer;

    private FrameBuffer				      frameBuffer;
    private final AtlasRegion			      lightTexture;
    private final AtlasRegion			      shadowTexture;

    public GameRenderer() {
	super(null, MapManager.WORLD_UNITS_PER_PIXEL);

	if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
	    Gdx.app.debug(TAG, "Creating in debug mode");
	    shapeRenderer = new ShapeRenderer();
	} else {
	    Gdx.app.debug(TAG, "Creating in non-debug mode");
	    shapeRenderer = null;
	}

	viewport = new FitViewport(32, 18);
	camera = viewport.getCamera();
	visibleArea = new Rectangle();
	scissors = new Rectangle();

	this.backgroundLayers = new Array<TiledMapTileLayer>();
	this.foregroundLayers = new Array<TiledMapTileLayer>();

	this.sizeComponentMapper = ComponentMapper.getFor(SizeComponent.class);
	this.animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);

	this.entityComparator = new yPositionComparator(sizeComponentMapper);

	final TextureAtlas textureAtlas = Utils.getAssetManager().get("lights/lights.atlas", TextureAtlas.class);
	lightTexture = textureAtlas.findRegion("light");
	shadowTexture = textureAtlas.findRegion("shadow");
	frameBuffer = null;
    }

    public void setMap(Map map) {
	this.map = map;
	super.setMap(map.getTiledMap());

	this.backgroundLayers.clear();
	this.foregroundLayers.clear();
	this.lightMapLayer = null;
	for (MapLayer mapLayer : map.getTiledMap().getLayers()) {
	    if (mapLayer instanceof TiledMapTileLayer) {
		if ("ground".equals(mapLayer.getName())) {
		    groundLayer = (TiledMapTileLayer) mapLayer;
		} else if (mapLayer.getName().startsWith("background")) {
		    backgroundLayers.add((TiledMapTileLayer) mapLayer);
		} else {
		    foregroundLayers.add((TiledMapTileLayer) mapLayer);
		}
	    } else if (mapLayer instanceof TiledMapImageLayer) {
		lightMapLayer = (TiledMapImageLayer) mapLayer;
	    }
	}
    }

    public void resize(int width, int height) {
	Gdx.app.debug(TAG, "Resizing with " + width + "x" + height + " from viewport " + viewport.getScreenWidth() + "x" + viewport.getScreenHeight());
	viewport.update(width, height, false);
	visibleArea.set(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
	Gdx.app.debug(TAG, "To viewport " + viewport.getScreenWidth() + "x" + viewport.getScreenHeight());

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

    private void interpolateEntities(float alpha) {
	for (Entity entity : map.getEntities()) {
	    final SizeComponent sizeComp = sizeComponentMapper.get(entity);

	    final float invAlpha = 1.0f - alpha;
	    sizeComp.interpolatedPosition.x = sizeComp.interpolatedPosition.x * invAlpha + sizeComp.boundingRectangle.x * alpha;
	    sizeComp.interpolatedPosition.y = sizeComp.interpolatedPosition.y * invAlpha + sizeComp.boundingRectangle.y * alpha;
	}
    }

    public void render(float alpha) {
	AnimatedTiledMapTile.updateAnimationBaseTime();
	interpolateEntities(alpha);
	map.getEntities().sort(entityComparator);

	if (cameraLockEntitySizeComponent != null) {
	    camera.position.set(cameraLockEntitySizeComponent.interpolatedPosition, 0);
	    visibleArea.setCenter(cameraLockEntitySizeComponent.interpolatedPosition);
	}

	prepareLightFrameBuffer();

	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

	viewport.apply();
	setView(camera.combined, visibleArea.x, visibleArea.y, visibleArea.width, visibleArea.height);
	batch.begin();
	viewport.calculateScissors(batch.getTransformMatrix(), visibleArea, scissors);
	ScissorStack.pushScissors(scissors);
	if (groundLayer != null) {
	    renderTileLayer(groundLayer);
	}
	for (Entity entity : map.getEntities()) {
	    renderEntityShadow(entity);
	}
	for (TiledMapTileLayer layer : backgroundLayers) {
	    renderTileLayer(layer);
	}
	for (Entity entity : map.getEntities()) {
	    renderEntityEffects(entity);
	}
	for (Entity entity : map.getEntities()) {
	    renderEntity(entity);
	}
	for (TiledMapTileLayer layer : foregroundLayers) {
	    renderTileLayer(layer);
	}
	batch.end();

	applyLightFrameBuffer();

	if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
	    renderDebugInformation();
	}

	ScissorStack.popScissors();
    }

    private void renderEntityShadow(Entity entity) {
	final AnimationComponent animationComp = animationComponentMapper.get(entity);

	if (animationComp.animation != null) {
	    final SizeComponent sizeComp = sizeComponentMapper.get(entity);
	    if (!viewBounds.overlaps(sizeComp.boundingRectangle)) {
		return;
	    }

	    if (cameraLockEntityRevelationComponent != null && !Intersector.overlaps(cameraLockEntityRevelationComponent.revelationCircle, sizeComp.boundingRectangle)) {
		return;
	    }

	    batch.draw(shadowTexture, sizeComp.interpolatedPosition.x, sizeComp.interpolatedPosition.y - sizeComp.boundingRectangle.height * 0.2f, sizeComp.boundingRectangle.width,
		    sizeComp.boundingRectangle.height * 0.5f);
	}
    }

    private void renderEntityEffects(Entity entity) {
	final AnimationComponent animationComp = animationComponentMapper.get(entity);

	if (animationComp.animation != null && animationComp.originEffects.size > 0) {
	    final SizeComponent sizeComp = sizeComponentMapper.get(entity);
	    if (!viewBounds.overlaps(sizeComp.boundingRectangle)) {
		return;
	    }

	    if (cameraLockEntityRevelationComponent != null && !Intersector.overlaps(cameraLockEntityRevelationComponent.revelationCircle, sizeComp.boundingRectangle)) {
		return;
	    }

	    final float x = sizeComp.interpolatedPosition.x + animationComp.originPoint.x;
	    final float y = sizeComp.interpolatedPosition.y + animationComp.originPoint.y;
	    for (SpecialEffect effect : animationComp.originEffects) {
		final float width = effect.getWidth();
		final float height = effect.getHeight();
		batch.draw(effect.getCurrentKeyFrame(), x - width * 0.5f, y - height * 0.5f, width, height);
	    }
	}
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

	    final Color batchColor = batch.getColor();
	    batch.setColor(animationComp.color);
	    final TextureRegion keyFrame = animationComp.animation.getKeyFrame(animationComp.animationTime, true);
	    batch.draw(keyFrame, sizeComp.interpolatedPosition.x, sizeComp.interpolatedPosition.y, sizeComp.boundingRectangle.width, sizeComp.boundingRectangle.height);
	    batch.setColor(batchColor);
	}
    }

    private void prepareLightFrameBuffer() {
	if (cameraLockEntityRevelationComponent != null) {
	    frameBuffer.begin();

	    final Color mapBackgroundColor = map.getBackgroundColor();
	    Gdx.gl.glClearColor(mapBackgroundColor.r, mapBackgroundColor.g, mapBackgroundColor.b, mapBackgroundColor.a);
	    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

	    setView(camera.combined, visibleArea.x, visibleArea.y, visibleArea.width, visibleArea.height);
	    batch.begin();
	    if (lightMapLayer != null) {
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		renderImageLayer(lightMapLayer);
	    }

	    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
	    final Rectangle boundingRectangle = cameraLockEntitySizeComponent.boundingRectangle;
	    batch.draw(lightTexture, cameraLockEntitySizeComponent.interpolatedPosition.x + boundingRectangle.width * 0.5f - cameraLockEntityRevelationComponent.revelationRadius, // x
		    cameraLockEntitySizeComponent.interpolatedPosition.y + boundingRectangle.height * 0.5f - cameraLockEntityRevelationComponent.revelationRadius, // y
		    cameraLockEntityRevelationComponent.revelationRadius * 2f, cameraLockEntityRevelationComponent.revelationRadius * 2f);

	    batch.end();

	    frameBuffer.end();
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

    private void renderDebugInformation() {
	shapeRenderer.setProjectionMatrix(camera.combined);
	shapeRenderer.begin(ShapeType.Line);

	shapeRenderer.setColor(Color.RED);
	for (Rectangle rect : map.getCollisionAreas()) {
	    shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
	}

	for (Entity entity : map.getEntities()) {
	    final CollisionComponent collisionComponent = entity.getComponent(CollisionComponent.class);
	    final SizeComponent sizeComp = sizeComponentMapper.get(entity);
	    if (collisionComponent != null) {
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.rect(sizeComp.interpolatedPosition.x + collisionComponent.rectOffset.x, sizeComp.interpolatedPosition.y + collisionComponent.rectOffset.y,
			collisionComponent.collisionRectangle.width, collisionComponent.collisionRectangle.height);
	    }

	    if (sizeComp != null) {
		shapeRenderer.setColor(Color.BLUE);
		shapeRenderer.rect(sizeComp.interpolatedPosition.x, sizeComp.interpolatedPosition.y, sizeComp.boundingRectangle.width, sizeComp.boundingRectangle.height);
	    }

	}

	shapeRenderer.setColor(Color.BLUE);
	for (Portal portal : map.getPortals()) {
	    shapeRenderer.rect(portal.getArea().x, portal.getArea().y, portal.getArea().width, portal.getArea().height);
	}

	if (cameraLockEntityRevelationComponent != null) {
	    shapeRenderer.setColor(Color.WHITE);
	    shapeRenderer.circle(cameraLockEntitySizeComponent.interpolatedPosition.x + cameraLockEntitySizeComponent.boundingRectangle.width * 0.5f,
		    cameraLockEntitySizeComponent.interpolatedPosition.y + cameraLockEntitySizeComponent.boundingRectangle.height * 0.5f,
		    cameraLockEntityRevelationComponent.revelationCircle.radius, 64);
	}

	shapeRenderer.end();
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
	    frameBuffer = null;
	}
    }
}
