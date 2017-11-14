package com.lok.game;

import static com.badlogic.gdx.graphics.g2d.Batch.C1;
import static com.badlogic.gdx.graphics.g2d.Batch.C2;
import static com.badlogic.gdx.graphics.g2d.Batch.C3;
import static com.badlogic.gdx.graphics.g2d.Batch.C4;
import static com.badlogic.gdx.graphics.g2d.Batch.U1;
import static com.badlogic.gdx.graphics.g2d.Batch.U2;
import static com.badlogic.gdx.graphics.g2d.Batch.U3;
import static com.badlogic.gdx.graphics.g2d.Batch.U4;
import static com.badlogic.gdx.graphics.g2d.Batch.V1;
import static com.badlogic.gdx.graphics.g2d.Batch.V2;
import static com.badlogic.gdx.graphics.g2d.Batch.V3;
import static com.badlogic.gdx.graphics.g2d.Batch.V4;
import static com.badlogic.gdx.graphics.g2d.Batch.X1;
import static com.badlogic.gdx.graphics.g2d.Batch.X2;
import static com.badlogic.gdx.graphics.g2d.Batch.X3;
import static com.badlogic.gdx.graphics.g2d.Batch.X4;
import static com.badlogic.gdx.graphics.g2d.Batch.Y1;
import static com.badlogic.gdx.graphics.g2d.Batch.Y2;
import static com.badlogic.gdx.graphics.g2d.Batch.Y3;
import static com.badlogic.gdx.graphics.g2d.Batch.Y4;

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
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
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

    private Map					      map;
    private final Array<Entity>			      entities;
    private SizeComponent			      cameraLockEntitySizeComponent;
    private MapRevelationComponent		      cameraLockEntityRevelationComponent;

    private Array<Rectangle>			      mapCollisionAreas;
    private Array<Portal>			      mapPortals;
    private Color				      mapBackgroundColor;

    private final Array<TiledMapTileLayer>	      backgroundLayers;
    private TiledMapTileLayer			      entityLayer;
    private final Array<TiledMapTileLayer>	      foregroundLayers;

    private final yPositionComparator		      entityComparator;

    private final ComponentMapper<SizeComponent>      sizeComponentMapper;
    private final ComponentMapper<AnimationComponent> animationComponentMapper;

    private float				      layerTileWidth;
    private float				      layerTileHeight;

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
	this.entityLayer = null;
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

    private void updateMapRenderInformation() {
	this.layerTileWidth = map.getTileWidthInWorldUnits();
	this.layerTileHeight = map.getTileHeightInWorldUnits();

	mapCollisionAreas = map.getCollisionAreas();
	mapBackgroundColor = map.getBackgroundColor();
	mapPortals = map.getPortals();

	this.backgroundLayers.clear();
	this.foregroundLayers.clear();
	this.entityLayer = null;
	for (MapLayer mapLayer : map.getTiledMap().getLayers()) {
	    if (mapLayer instanceof TiledMapTileLayer) {
		if (mapLayer.getName().startsWith("background")) {
		    backgroundLayers.add((TiledMapTileLayer) mapLayer);
		} else if (mapLayer.getName().startsWith("foreground")) {
		    foregroundLayers.add((TiledMapTileLayer) mapLayer);
		} else {
		    entityLayer = (TiledMapTileLayer) mapLayer;
		}
	    }
	}
    }

    @Override
    public void onMapChange(MapManager manager, Map map) {
	this.map = map;
	super.setMap(map.getTiledMap());
	updateMapRenderInformation();
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

	prepareLightFrameBuffer();

	batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

	Gdx.gl.glClearColor(mapBackgroundColor.r, mapBackgroundColor.g, mapBackgroundColor.b, mapBackgroundColor.a);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

	viewport.calculateScissors(batch.getTransformMatrix(), visibleArea, scissors);
	ScissorStack.pushScissors(scissors);

	if (cameraLockEntitySizeComponent != null) {
	    camera.position.set(cameraLockEntitySizeComponent.previousPosition, 0);
	    visibleArea.setCenter(cameraLockEntitySizeComponent.previousPosition);
	}

	viewport.apply();
	AnimatedTiledMapTile.updateAnimationBaseTime();

	setView(camera.combined, visibleArea.x, visibleArea.y, visibleArea.width, visibleArea.height);
	batch.begin();

	renderBackgroundLayers();
	renderEntityLayer(entities, alpha);
	renderForegroundLayers();

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
	    sizeComp.previousPosition.x = sizeComp.previousPosition.x * invAlpha + sizeComp.boundingRectangle.x * alpha;
	    sizeComp.previousPosition.y = sizeComp.previousPosition.y * invAlpha + sizeComp.boundingRectangle.y * alpha;
	}
    }

    private void applyLightFrameBuffer() {
	if (cameraLockEntityRevelationComponent != null) {
	    batch.begin();
	    batch.setProjectionMatrix(batch.getProjectionMatrix().idt());
	    batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_COLOR);
	    batch.draw(frameBuffer.getColorBufferTexture(), -1, 1, 2, -2);
	    batch.end();
	}
    }

    private void prepareLightFrameBuffer() {
	if (cameraLockEntityRevelationComponent != null) {
	    frameBuffer.begin();

	    Gdx.gl.glClearColor(0, 0, 0, 1);
	    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

	    batch.setProjectionMatrix(camera.combined);
	    batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
	    batch.begin();

	    final Rectangle boundingRectangle = cameraLockEntitySizeComponent.boundingRectangle;
	    Rectangle.tmp.set(cameraLockEntitySizeComponent.previousPosition.x + boundingRectangle.width / 2f - cameraLockEntityRevelationComponent.revelationRadius, // x
		    cameraLockEntitySizeComponent.previousPosition.y + boundingRectangle.height / 2f - cameraLockEntityRevelationComponent.revelationRadius, // y
		    cameraLockEntityRevelationComponent.revelationRadius * 2f, cameraLockEntityRevelationComponent.revelationRadius * 2f); // size

	    batch.draw(lightTexture, Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);
	    batch.end();

	    frameBuffer.end();
	}
    }

    private void renderDebugInformation() {
	shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
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

	shapeRenderer.end();
    }

    private void renderBackgroundLayers() {
	for (TiledMapTileLayer mapLayer : backgroundLayers) {
	    renderTileLayer(mapLayer, null, 0);
	}
    }

    private void renderEntityLayer(Array<Entity> entities, float alpha) {
	// code taken from original class file but adjusted to go through all cells of the viewport of
	// the entity layer and render cells and entities ordered by y-axis
	entities.sort(entityComparator);

	if (entityLayer == null) {
	    // simply render all entities inside the viewport
	    for (Entity entity : entities) {
		renderEntity(entity, sizeComponentMapper.get(entity), alpha);
	    }
	} else {
	    // render entities + cells of entity layer
	    renderTileLayer(entityLayer, entities, alpha);
	}
    }

    private void renderEntity(Entity entity, SizeComponent sizeComp, float alpha) {
	final AnimationComponent animationComp = animationComponentMapper.get(entity);

	if (animationComp.animation != null) {
	    if (!viewBounds.overlaps(sizeComp.boundingRectangle)) {
		return;
	    }

	    final TextureRegion keyFrame = animationComp.animation.getKeyFrame(animationComp.animationTime, true);
	    batch.draw(keyFrame, sizeComp.previousPosition.x, sizeComp.previousPosition.y, sizeComp.boundingRectangle.width, sizeComp.boundingRectangle.height);
	}
    }

    private void renderForegroundLayers() {
	for (TiledMapTileLayer mapLayer : foregroundLayers) {
	    renderTileLayer(mapLayer, null, 0);
	}
    }

    private void renderTileLayer(TiledMapTileLayer layer, Array<Entity> entities, float alpha) {
	// code taken from original class file
	// added a map.isVisible(col, row) check to avoid rendering of cells who are not revelaed by the player yet
	// also given entities and cells are rendered according to their y-position if entities != null
	int currentEntityIndex = 0;
	if (entities != null) {
	    for (; currentEntityIndex < entities.size; ++currentEntityIndex) {
		final Entity entity = entities.get(currentEntityIndex);
		final AnimationComponent animationComp = animationComponentMapper.get(entity);

		if (animationComp.animation != null) {
		    final SizeComponent sizeComp = sizeComponentMapper.get(entity);

		    if (viewBounds.overlaps(sizeComp.boundingRectangle)) {
			// found start index for render checking
			break;
		    }
		}
	    }
	}

	final Color batchColor = batch.getColor();
	final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity());

	final int layerWidth = layer.getWidth();
	final int layerHeight = layer.getHeight();

	final float layerOffsetX = layer.getRenderOffsetX() * unitScale;
	// offset in tiled is y down, so we flip it
	final float layerOffsetY = -layer.getRenderOffsetY() * unitScale;

	final int col1 = Math.max(0, (int) ((viewBounds.x - layerOffsetX) / layerTileWidth));
	final int col2 = Math.min(layerWidth, (int) ((viewBounds.x + viewBounds.width + layerTileWidth - layerOffsetX) / layerTileWidth));

	final int row1 = Math.max(0, (int) ((viewBounds.y - layerOffsetY) / layerTileHeight));
	final int row2 = Math.min(layerHeight, (int) ((viewBounds.y + viewBounds.height + layerTileHeight - layerOffsetY) / layerTileHeight));

	float y = row2 * layerTileHeight + layerOffsetY;
	float xStart = col1 * layerTileWidth + layerOffsetX;
	final float[] vertices = this.vertices;

	for (int row = row2; row >= row1; row--) {
	    float x = xStart;
	    for (int col = col1; col < col2; col++) {
		while (entities != null && currentEntityIndex < entities.size) {
		    final Entity entity = entities.get(currentEntityIndex);
		    final SizeComponent sizeComp = sizeComponentMapper.get(entity);
		    if (y >= sizeComp.boundingRectangle.y) {
			break;
		    }

		    renderEntity(entity, sizeComp, alpha);
		    ++currentEntityIndex;
		}

		final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
		if (cell == null) {
		    x += layerTileWidth;
		    continue;
		}
		final TiledMapTile tile = cell.getTile();

		if (tile != null) {
		    final boolean flipX = cell.getFlipHorizontally();
		    final boolean flipY = cell.getFlipVertically();
		    final int rotations = cell.getRotation();

		    TextureRegion region = tile.getTextureRegion();

		    float x1 = x + tile.getOffsetX() * unitScale;
		    float y1 = y + tile.getOffsetY() * unitScale;
		    float x2 = x1 + region.getRegionWidth() * unitScale;
		    float y2 = y1 + region.getRegionHeight() * unitScale;

		    float u1 = region.getU();
		    float v1 = region.getV2();
		    float u2 = region.getU2();
		    float v2 = region.getV();

		    vertices[X1] = x1;
		    vertices[Y1] = y1;
		    vertices[C1] = color;
		    vertices[U1] = u1;
		    vertices[V1] = v1;

		    vertices[X2] = x1;
		    vertices[Y2] = y2;
		    vertices[C2] = color;
		    vertices[U2] = u1;
		    vertices[V2] = v2;

		    vertices[X3] = x2;
		    vertices[Y3] = y2;
		    vertices[C3] = color;
		    vertices[U3] = u2;
		    vertices[V3] = v2;

		    vertices[X4] = x2;
		    vertices[Y4] = y1;
		    vertices[C4] = color;
		    vertices[U4] = u2;
		    vertices[V4] = v1;

		    if (flipX) {
			float temp = vertices[U1];
			vertices[U1] = vertices[U3];
			vertices[U3] = temp;
			temp = vertices[U2];
			vertices[U2] = vertices[U4];
			vertices[U4] = temp;
		    }
		    if (flipY) {
			float temp = vertices[V1];
			vertices[V1] = vertices[V3];
			vertices[V3] = temp;
			temp = vertices[V2];
			vertices[V2] = vertices[V4];
			vertices[V4] = temp;
		    }
		    if (rotations != 0) {
			switch (rotations) {
			    case Cell.ROTATE_90: {
				float tempV = vertices[V1];
				vertices[V1] = vertices[V2];
				vertices[V2] = vertices[V3];
				vertices[V3] = vertices[V4];
				vertices[V4] = tempV;

				float tempU = vertices[U1];
				vertices[U1] = vertices[U2];
				vertices[U2] = vertices[U3];
				vertices[U3] = vertices[U4];
				vertices[U4] = tempU;
				break;
			    }
			    case Cell.ROTATE_180: {
				float tempU = vertices[U1];
				vertices[U1] = vertices[U3];
				vertices[U3] = tempU;
				tempU = vertices[U2];
				vertices[U2] = vertices[U4];
				vertices[U4] = tempU;
				float tempV = vertices[V1];
				vertices[V1] = vertices[V3];
				vertices[V3] = tempV;
				tempV = vertices[V2];
				vertices[V2] = vertices[V4];
				vertices[V4] = tempV;
				break;
			    }
			    case Cell.ROTATE_270: {
				float tempV = vertices[V1];
				vertices[V1] = vertices[V4];
				vertices[V4] = vertices[V3];
				vertices[V3] = vertices[V2];
				vertices[V2] = tempV;

				float tempU = vertices[U1];
				vertices[U1] = vertices[U4];
				vertices[U4] = vertices[U3];
				vertices[U3] = vertices[U2];
				vertices[U2] = tempU;
				break;
			    }
			}
		    }
		    batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
		}
		x += layerTileWidth;
	    }
	    y -= layerTileHeight;
	}

	// render entities which are positioned outside the current viewport but maybe parts of it should be
	// visible already inside the current viewport
	while (entities != null && currentEntityIndex < entities.size) {
	    final Entity entity = entities.get(currentEntityIndex);
	    final SizeComponent sizeComp = sizeComponentMapper.get(entity);

	    renderEntity(entity, sizeComp, alpha);
	    ++currentEntityIndex;
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
