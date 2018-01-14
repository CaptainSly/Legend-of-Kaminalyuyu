package com.lok.game.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.map.MapManager.MapID;

public class Map {
    private final MapID		       mapID;
    private final TiledMap	       tiledMap;
    private final Rectangle	       boundary;
    private final Array<Rectangle>     collisionAreas;
    private final Array<MapEntityData> entityData;
    private final Array<Portal>	       portals;
    private final Color		       backgroundColor;
    private final String	       musicFilePath;
    private final int		       numTilesX;
    private final int		       numTilesY;
    private final float		       tileWidthInWorldUnits;
    private final float		       tileHeightInWorldUnits;

    public Map(MapID mapID, TiledMap tiledMap) {
	this.mapID = mapID;
	this.tiledMap = tiledMap;
	this.boundary = new Rectangle();
	this.collisionAreas = new Array<Rectangle>();
	this.entityData = new Array<MapEntityData>();
	this.portals = new Array<Portal>();

	final MapProperties mapProperties = tiledMap.getProperties();
	final String backgroundColor = mapProperties.get("backgroundcolor", String.class);
	if (backgroundColor != null) {
	    this.backgroundColor = Color.valueOf(backgroundColor);
	} else {
	    this.backgroundColor = Color.BLACK;
	}
	musicFilePath = mapProperties.get("music", String.class);

	tileWidthInWorldUnits = mapProperties.get("tilewidth", Integer.class) * MapManager.WORLD_UNITS_PER_PIXEL;
	tileHeightInWorldUnits = mapProperties.get("tileheight", Integer.class) * MapManager.WORLD_UNITS_PER_PIXEL;
	numTilesX = mapProperties.get("width", Integer.class);
	numTilesY = mapProperties.get("height", Integer.class);
	boundary.set(0, 0, numTilesX * tileWidthInWorldUnits, numTilesY * tileHeightInWorldUnits);

	for (MapLayer mapLayer : tiledMap.getLayers()) {
	    if ("Portals".equals(mapLayer.getName())) {
		parsePortals(mapLayer);
	    } else if ("Collision".equals(mapLayer.getName())) {
		parseCollisionAreas(mapLayer);
	    } else if ("Entities".equals(mapLayer.getName())) {
		parseEntityData(mapLayer);
	    }
	}
    }

    private void parseCollisionAreas(MapLayer mapLayer) {
	for (MapObject mapObj : mapLayer.getObjects()) {
	    if (mapObj instanceof RectangleMapObject) {
		final Rectangle collisionArea = ((RectangleMapObject) mapObj).getRectangle();
		collisionArea.x *= MapManager.WORLD_UNITS_PER_PIXEL;
		collisionArea.y *= MapManager.WORLD_UNITS_PER_PIXEL;
		collisionArea.width *= MapManager.WORLD_UNITS_PER_PIXEL;
		collisionArea.height *= MapManager.WORLD_UNITS_PER_PIXEL;
		collisionAreas.add(collisionArea);
	    }
	}
    }

    public void parseEntityData(MapLayer mapLayer) {
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
		entityData.add(MapEntityData.newMapEntityData(entityID, new Vector2(entityArea.x * MapManager.WORLD_UNITS_PER_PIXEL, entityArea.y * MapManager.WORLD_UNITS_PER_PIXEL)));
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

    public MapID getMapID() {
	return mapID;
    }

    public String getMusicFilePath() {
	return musicFilePath;
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

    public Array<MapEntityData> getEntityData() {
	return entityData;
    }

    public Array<Portal> getPortals() {
	return portals;
    }

    public Color getBackgroundColor() {
	return backgroundColor;
    }

    public boolean isPathable(Rectangle boundingRectangle) {
	for (Rectangle collArea : collisionAreas) {
	    if (collArea.overlaps(boundingRectangle)) {
		return false;
	    }
	}

	return true;
    }
}
