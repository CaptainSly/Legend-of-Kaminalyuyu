package com.lok.game.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.lok.game.AssetManager;
import com.lok.game.ecs.EntityEngine;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.map.MapManager.MapID;

public class Map {
    public static class Portal {
	private final Rectangle	area;
	private final Vector2	targetPosition;
	private final MapID	targetMapID;

	private Portal(Rectangle area, Vector2 targetPosition, MapID targetMapID) {
	    this.area = area;
	    this.targetPosition = targetPosition;
	    this.targetMapID = targetMapID;
	}

	public Rectangle getArea() {
	    return area;
	}

	public boolean isColliding(Rectangle rectangle) {
	    return area.overlaps(rectangle);
	}

	public Vector2 getTargetPosition() {
	    return targetPosition;
	}

	public MapID getTargetMapID() {
	    return targetMapID;
	}
    }

    private final MapID		   mapID;
    private final TiledMap	   tiledMap;
    private final Rectangle	   boundary;
    private final Array<Rectangle> collisionAreas;
    private final Array<Entity>	   entities;
    private final Array<Portal>	   portals;
    private final Color		   backgroundColor;
    private final int		   numTilesX;
    private final int		   numTilesY;
    private final float		   tileWidthInWorldUnits;
    private final float		   tileHeightInWorldUnits;

    public Map(MapID mapID) {
	this.mapID = mapID;
	this.tiledMap = AssetManager.getManager().getAsset(mapID.getMapName(), TiledMap.class);
	this.boundary = new Rectangle();
	this.collisionAreas = new Array<Rectangle>();
	this.entities = new Array<Entity>();
	this.portals = new Array<Portal>();

	final MapProperties mapProperties = tiledMap.getProperties();
	final String backgroundColor = mapProperties.get("backgroundcolor", String.class);
	if (backgroundColor != null) {
	    this.backgroundColor = Color.valueOf(backgroundColor);
	} else {
	    this.backgroundColor = Color.BLACK;
	}

	tileWidthInWorldUnits = mapProperties.get("tilewidth", Integer.class) * MapManager.WORLD_UNITS_PER_PIXEL;
	tileHeightInWorldUnits = mapProperties.get("tileheight", Integer.class) * MapManager.WORLD_UNITS_PER_PIXEL;
	numTilesX = mapProperties.get("width", Integer.class);
	numTilesY = mapProperties.get("height", Integer.class);
	boundary.set(0, 0, numTilesX * tileWidthInWorldUnits, numTilesY * tileHeightInWorldUnits);

	for (MapLayer mapLayer : tiledMap.getLayers()) {
	    if (mapLayer instanceof TiledMapTileLayer) {
		final TiledMapTileLayer tiledMapLayer = (TiledMapTileLayer) mapLayer;
		parseCollisionAreas(tiledMapLayer, tileWidthInWorldUnits, tileHeightInWorldUnits);
	    } else if ("Portals".equals(mapLayer.getName())) {
		parsePortals(mapLayer);
	    } else if ("Entities".equals(mapLayer.getName())) {
		parseEntities(mapLayer);
	    }
	}
    }

    private void parseCollisionAreas(TiledMapTileLayer tiledMapTileLayer, float tileWidthInWorldUnits, float tileHeightInWorldUnits) {
	for (int y = 0; y < tiledMapTileLayer.getWidth(); ++y) {
	    for (int x = 0; x < tiledMapTileLayer.getHeight(); ++x) {
		final Cell cell = tiledMapTileLayer.getCell(x, y);

		if (cell == null) {
		    continue;
		}

		for (MapObject mapObj : cell.getTile().getObjects()) {
		    if (mapObj instanceof RectangleMapObject) {
			final Rectangle collisionArea = ((RectangleMapObject) mapObj).getRectangle();
			collisionAreas.add(new Rectangle(x * tileWidthInWorldUnits, y * tileHeightInWorldUnits, collisionArea.width * MapManager.WORLD_UNITS_PER_PIXEL,
				collisionArea.height * MapManager.WORLD_UNITS_PER_PIXEL));
		    }
		}
	    }
	}
    }

    private void parseEntities(MapLayer mapLayer) {
	for (MapObject mapObj : mapLayer.getObjects()) {
	    if (mapObj instanceof RectangleMapObject) {
		final MapProperties entityProperties = mapObj.getProperties();
		final String EntityIDStr = entityProperties.get("entityID", String.class);
		final EntityID entityID;

		if (EntityIDStr == null) {
		    throw new GdxRuntimeException("Entity of map " + mapID + " does not have an entityID specified");
		} else {
		    entityID = EntityID.valueOf(EntityIDStr);
		    if (entityID == null) {
			throw new GdxRuntimeException("Entity of map " + mapID + " does not have a valid entityID " + EntityIDStr);
		    }
		}

		final Rectangle entityArea = ((RectangleMapObject) mapObj).getRectangle();
		EntityEngine.getEngine().createEntity(entityID, entityArea.x * MapManager.WORLD_UNITS_PER_PIXEL, entityArea.y * MapManager.WORLD_UNITS_PER_PIXEL);
	    }
	}
    }

    private void parsePortals(MapLayer mapLayer) {
	for (MapObject mapObj : mapLayer.getObjects()) {
	    if (mapObj instanceof RectangleMapObject) {
		final MapProperties portalProperties = mapObj.getProperties();
		final Integer targetTileIndexX = portalProperties.get("targetTileIndexX", Integer.class);
		final Integer targetTileIndexY = portalProperties.get("targetTileIndexY", Integer.class);
		final String targetMapIDStr = portalProperties.get("targetMapID", String.class);
		final MapID targetMapID;

		if (targetTileIndexX == null || targetTileIndexY == null) {
		    throw new GdxRuntimeException("Portal of map " + mapID + " does not have a valid target tile (" + targetTileIndexX + "/" + targetTileIndexY + ")");
		}
		if (targetMapIDStr == null) {
		    targetMapID = this.mapID;
		} else {
		    targetMapID = MapID.valueOf(targetMapIDStr);
		    if (targetMapID == null) {
			throw new GdxRuntimeException("Portal of map " + mapID + " does not have a valid target mapID " + targetMapIDStr);
		    }
		}

		final Rectangle portalArea = ((RectangleMapObject) mapObj).getRectangle();
		portalArea.x *= MapManager.WORLD_UNITS_PER_PIXEL;
		portalArea.y *= MapManager.WORLD_UNITS_PER_PIXEL;
		portalArea.width *= MapManager.WORLD_UNITS_PER_PIXEL;
		portalArea.height *= MapManager.WORLD_UNITS_PER_PIXEL;
		portals.add(new Portal(portalArea,
			new Vector2(targetTileIndexX * tileWidthInWorldUnits, numTilesY * tileHeightInWorldUnits - targetTileIndexY * tileHeightInWorldUnits), targetMapID));
	    }
	}
    }

    public TiledMap getTiledMap() {
	return tiledMap;
    }

    public Array<Rectangle> getCollisionAreas() {
	return collisionAreas;
    }

    public Rectangle getBoundary() {
	return boundary;
    }

    public Array<Entity> getEntities() {
	return entities;
    }

    public Array<Portal> getPortals() {
	return portals;
    }

    public Color getBackgroundColor() {
	return backgroundColor;
    }

    public void dispose() {
	tiledMap.dispose();
    }
}
