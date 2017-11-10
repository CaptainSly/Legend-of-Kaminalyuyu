package com.lok.game.map;

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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.ecs.components.PositionComponent;
import com.lok.game.ecs.components.SizeComponent;

public class MapRenderer extends OrthogonalTiledMapRenderer {

    private static class yPositionComparator implements Comparator<Entity> {
	private final ComponentMapper<PositionComponent> posComponentMapper;

	private yPositionComparator(ComponentMapper<PositionComponent> posComponentMapper) {
	    this.posComponentMapper = posComponentMapper;
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

	    return posComponentMapper.get(o1).position.y > posComponentMapper.get(o2).position.y ? -1 : 1;
	}

    }

    private final Array<TiledMapTileLayer>	     backgroundLayers;
    private final Array<TiledMapTileLayer>	     foregroundLayers;
    private TiledMapTileLayer			     entityLayer;
    private Map					     map;
    private final yPositionComparator		     entityComparator;
    private final ComponentMapper<PositionComponent> posComponentMapper;
    private ComponentMapper<SizeComponent>	     sizeComponentMapper;
    private ComponentMapper<AnimationComponent>	     animationComponentMapper;

    public MapRenderer(Map map, Batch batch) {
	super(map.getTiledMap(), MapManager.WORLD_UNITS_PER_PIXEL, batch);

	this.map = map;
	this.backgroundLayers = new Array<TiledMapTileLayer>();
	this.foregroundLayers = new Array<TiledMapTileLayer>();
	this.posComponentMapper = ComponentMapper.getFor(PositionComponent.class);
	sizeComponentMapper = ComponentMapper.getFor(SizeComponent.class);
	animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);
	this.entityComparator = new yPositionComparator(posComponentMapper);
	updateBackgroundAndForegroundLayers();
    }

    private void updateBackgroundAndForegroundLayers() {
	this.backgroundLayers.clear();
	this.foregroundLayers.clear();

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

    public void renderBackgroundLayers() {
	for (TiledMapTileLayer mapLayer : backgroundLayers) {
	    renderTileLayer(mapLayer);
	}
    }

    public void renderEntityLayer(Array<Entity> entities, float alpha) {
	// code taken from original class file but adjusted to go through all cells of the viewport of
	// the entity layer and render cells and entities ordered by y-axis
	entities.sort(entityComparator);

	if (entityLayer == null) {
	    // simply render all entities inside the viewport
	    for (int i = 0; i < entities.size; ++i) {
		final Entity entity = entities.get(i);
		final AnimationComponent animationComp = animationComponentMapper.get(entity);

		if (animationComp.animation != null) {
		    final PositionComponent posComp = posComponentMapper.get(entity);
		    final SizeComponent sizeComp = sizeComponentMapper.get(entity);

		    if (!viewBounds.contains(sizeComp.boundingRectangle)) {
			continue;
		    }

		    posComp.previousPosition.interpolate(posComp.position, alpha, Interpolation.smoother);
		    final TextureRegion keyFrame = animationComp.animation.getKeyFrame(animationComp.animationTime, true);
		    batch.draw(keyFrame, posComp.previousPosition.x, posComp.previousPosition.y, sizeComp.boundingRectangle.width, sizeComp.boundingRectangle.height);
		}
	    }
	} else {
	    // render entities + cells of entity layer
	    final Color batchColor = batch.getColor();
	    final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * entityLayer.getOpacity());

	    final int layerWidth = entityLayer.getWidth();
	    final int layerHeight = entityLayer.getHeight();

	    final float layerTileWidth = entityLayer.getTileWidth() * unitScale;
	    final float layerTileHeight = entityLayer.getTileHeight() * unitScale;

	    final float layerOffsetX = entityLayer.getRenderOffsetX() * unitScale;
	    // offset in tiled is y down, so we flip it
	    final float layerOffsetY = -entityLayer.getRenderOffsetY() * unitScale;

	    final int col1 = Math.max(0, (int) ((viewBounds.x - layerOffsetX) / layerTileWidth));
	    final int col2 = Math.min(layerWidth, (int) ((viewBounds.x + viewBounds.width + layerTileWidth - layerOffsetX) / layerTileWidth));

	    final int row1 = Math.max(0, (int) ((viewBounds.y - layerOffsetY) / layerTileHeight));
	    final int row2 = Math.min(layerHeight, (int) ((viewBounds.y + viewBounds.height + layerTileHeight - layerOffsetY) / layerTileHeight));

	    float y = row2 * layerTileHeight + layerOffsetY;
	    float xStart = col1 * layerTileWidth + layerOffsetX;
	    final float[] vertices = this.vertices;

	    int currentEntityIndex = 0;
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

	    for (int row = row2; row >= row1; row--) {
		float x = xStart;
		for (int col = col1; col < col2; col++) {
		    while (currentEntityIndex < entities.size && y < posComponentMapper.get(entities.get(currentEntityIndex)).position.y) {
			final Entity entity = entities.get(currentEntityIndex);
			++currentEntityIndex;
			final AnimationComponent animationComp = animationComponentMapper.get(entity);

			if (animationComp.animation != null) {
			    final PositionComponent posComp = posComponentMapper.get(entity);
			    final SizeComponent sizeComp = sizeComponentMapper.get(entity);

			    if (!viewBounds.overlaps(sizeComp.boundingRectangle) || !map.isVisible(sizeComp.boundingRectangle)) {
				continue;
			    }

			    posComp.previousPosition.interpolate(posComp.position, alpha, Interpolation.smoother);
			    final TextureRegion keyFrame = animationComp.animation.getKeyFrame(animationComp.animationTime, true);
			    batch.draw(keyFrame, posComp.previousPosition.x, posComp.previousPosition.y, sizeComp.boundingRectangle.width, sizeComp.boundingRectangle.height);
			}
		    }

		    final TiledMapTileLayer.Cell cell = entityLayer.getCell(col, row);
		    if (cell == null || !map.isVisible(col, row)) {
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

	    while (currentEntityIndex < entities.size) {
		final Entity entity = entities.get(currentEntityIndex);
		++currentEntityIndex;
		final AnimationComponent animationComp = animationComponentMapper.get(entity);

		if (animationComp.animation != null) {
		    final PositionComponent posComp = posComponentMapper.get(entity);
		    final SizeComponent sizeComp = sizeComponentMapper.get(entity);

		    if (!viewBounds.overlaps(sizeComp.boundingRectangle) || !map.isVisible(sizeComp.boundingRectangle)) {
			continue;
		    }

		    posComp.previousPosition.interpolate(posComp.position, alpha, Interpolation.smoother);
		    final TextureRegion keyFrame = animationComp.animation.getKeyFrame(animationComp.animationTime, true);
		    batch.draw(keyFrame, posComp.previousPosition.x, posComp.previousPosition.y, sizeComp.boundingRectangle.width, sizeComp.boundingRectangle.height);
		}
	    }
	}
    }

    public void renderForegroundLayers() {
	for (TiledMapTileLayer mapLayer : foregroundLayers) {
	    renderTileLayer(mapLayer);
	}
    }

    @Override
    public void renderTileLayer(TiledMapTileLayer layer) {
	// code taken from original class file
	// added a map.isVisible(col, row) check to avoid rendering of cells who are not revelaed by the player yet
	final Color batchColor = batch.getColor();
	final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity());

	final int layerWidth = layer.getWidth();
	final int layerHeight = layer.getHeight();

	final float layerTileWidth = layer.getTileWidth() * unitScale;
	final float layerTileHeight = layer.getTileHeight() * unitScale;

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
		final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
		if (cell == null || !map.isVisible(col, row)) {
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
    }

    public void setMap(Map map) {
	super.setMap(map.getTiledMap());
	this.map = map;
	updateBackgroundAndForegroundLayers();
    }
}
