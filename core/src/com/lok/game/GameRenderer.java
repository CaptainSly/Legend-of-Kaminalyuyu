package com.lok.game;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lok.game.ecs.EntityEngine;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.ecs.components.CollisionComponent;
import com.lok.game.ecs.components.PositionComponent;
import com.lok.game.map.Map;
import com.lok.game.map.Map.Portal;
import com.lok.game.map.MapListener;
import com.lok.game.map.MapManager;
import com.lok.game.map.MapRenderer;

public class GameRenderer implements EntityListener, MapListener {
    private SpriteBatch				batch;
    private ComponentMapper<PositionComponent>	positionComponentMapper;
    private ComponentMapper<CollisionComponent>	collisionComponentMapper;
    private ComponentMapper<AnimationComponent>	animationComponentMapper;
    private Array<Entity>			entities;
    private MapRenderer				mapRenderer;
    private Viewport				viewport;
    private Rectangle				scissors;
    private Rectangle				visibleArea;
    private ShapeRenderer			shapeRenderer;
    private Entity				player;
    private Array<Rectangle>			mapCollisionAreas;
    private Array<Portal>			mapPortals;
    private Color				mapBackgroundColor;

    public GameRenderer() {
	this.player = null;
	positionComponentMapper = ComponentMapper.getFor(PositionComponent.class);
	animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);
	collisionComponentMapper = ComponentMapper.getFor(CollisionComponent.class);
	entities = new Array<Entity>();
	batch = new SpriteBatch();
	mapRenderer = null;
	// 16 : 9 view port where you can see 32 world units in width
	viewport = new FitViewport(32, 18, new OrthographicCamera());
	viewport.apply();

	mapCollisionAreas = null;
	mapBackgroundColor = Color.BLACK;

	EntityEngine.getEngine().addEntityListener(Family.one(AnimationComponent.class).get(), this);
	MapManager.getManager().addListener(this);

	scissors = new Rectangle();
	visibleArea = new Rectangle(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

	shapeRenderer = new ShapeRenderer();
    }

    public void render(float alpha) {
	Gdx.gl.glClearColor(mapBackgroundColor.r, mapBackgroundColor.g, mapBackgroundColor.b, mapBackgroundColor.a);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

	viewport.calculateScissors(batch.getTransformMatrix(), visibleArea, scissors);
	ScissorStack.pushScissors(scissors);

	if (player != null) {
	    final PositionComponent playerPosComp = positionComponentMapper.get(player);
	    playerPosComp.previousPosition.interpolate(playerPosComp.position, alpha, Interpolation.smoother);
	    viewport.getCamera().position.set(playerPosComp.position.x, playerPosComp.position.y, 0);
	    visibleArea.setCenter(playerPosComp.previousPosition);
	}
	viewport.getCamera().update();
	mapRenderer.setView(viewport.getCamera().combined, visibleArea.x, visibleArea.y, visibleArea.width, visibleArea.height);
	mapRenderer.render();

	batch.setProjectionMatrix(viewport.getCamera().combined);
	batch.begin();

	for (int i = 0; i < entities.size; ++i) {
	    final Entity entity = entities.get(i);
	    final PositionComponent posComp = positionComponentMapper.get(entity);
	    final AnimationComponent animationComp = animationComponentMapper.get(entity);

	    if (animationComp.animation != null) {
		posComp.previousPosition.interpolate(posComp.position, alpha, Interpolation.smoother);
		final TextureRegion keyFrame = animationComp.animation.getKeyFrame(animationComp.animationTime, true);
		batch.draw(keyFrame, posComp.previousPosition.x, posComp.previousPosition.y, keyFrame.getRegionWidth() * MapManager.WORLD_UNITS_PER_PIXEL,
			keyFrame.getRegionHeight() * MapManager.WORLD_UNITS_PER_PIXEL);
	    }
	}

	batch.end();

	shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
	shapeRenderer.begin(ShapeType.Line);

	shapeRenderer.setColor(Color.RED);
	for (Rectangle rect : mapCollisionAreas) {
	    shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
	}

	for (Entity entity : entities) {
	    final CollisionComponent collisionComponent = collisionComponentMapper.get(entity);
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

	shapeRenderer.end();

	batch.flush();
	ScissorStack.popScissors();
    }

    public void resize(int width, int height) {
	viewport.update(width, height);
	visibleArea = new Rectangle(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
    }

    public void dispose() {
	batch.dispose();
	shapeRenderer.dispose();
	mapRenderer.dispose();
    }

    @Override
    public void entityAdded(Entity entity) {
	entities.add(entity);
	if (entity.flags == EntityID.PLAYER.ordinal()) {
	    this.player = entity;
	}
    }

    @Override
    public void entityRemoved(Entity entity) {
	entities.removeValue(entity, false);
	if (entity.flags == EntityID.PLAYER.ordinal()) {
	    this.player = null;
	}
    }

    @Override
    public void onMapChange(MapManager manager, Map map) {
	if (mapRenderer == null) {
	    mapRenderer = new MapRenderer(map, batch);
	} else {
	    mapRenderer.setMap(map);
	}

	mapCollisionAreas = map.getCollisionAreas();
	mapBackgroundColor = map.getBackgroundColor();
	mapPortals = map.getPortals();
    }

}
