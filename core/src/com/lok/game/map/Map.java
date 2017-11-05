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
import com.lok.game.map.MapManager.MapID;

public class Map {
    public class Portal {
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

	public boolean isInside(Vector2 position) {
	    return area.contains(position);
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

    public Map(MapID mapID) {
	this.mapID = mapID;
	this.tiledMap = AssetManager.getManager().getAsset(mapID.getMapName(), TiledMap.class);
	this.boundary = new Rectangle();
	this.collisionAreas = new Array<Rectangle>();
	this.entities = new Array<Entity>();
	this.portals = new Array<Portal>();
	final String backgroundColor = tiledMap.getProperties().get("backgroundcolor", String.class);
	if (backgroundColor != null) {
	    this.backgroundColor = Color.valueOf(backgroundColor);
	} else {
	    this.backgroundColor = Color.BLACK;
	}

	parseBoundary();
	parseCollisionAreas();
	parseEntities();
	parsePortals();
    }

    private void parseBoundary() {
	final MapProperties mapProperties = tiledMap.getProperties();
	boundary.set(0, 0, // x, y
		mapProperties.get("width", Integer.class) * mapProperties.get("tilewidth", Integer.class) * MapManager.WORLD_UNITS_PER_PIXEL, // width
		mapProperties.get("height", Integer.class) * mapProperties.get("tileheight", Integer.class) * MapManager.WORLD_UNITS_PER_PIXEL); // height
    }

    private void parseCollisionAreas() {
	final MapProperties mapProperties = tiledMap.getProperties();
	final int tileWidth = mapProperties.get("tilewidth", Integer.class);
	final int tileHeight = mapProperties.get("tileheight", Integer.class);

	for (MapLayer mapLayer : tiledMap.getLayers()) {
	    if (!(mapLayer instanceof TiledMapTileLayer)) {
		continue;
	    }

	    final TiledMapTileLayer tiledMapTileLayer = (TiledMapTileLayer) mapLayer;

	    for (int y = 0; y < tiledMapTileLayer.getWidth(); ++y) {
		for (int x = 0; x < tiledMapTileLayer.getHeight(); ++x) {
		    final Cell cell = tiledMapTileLayer.getCell(x, y);

		    if (cell == null) {
			continue;
		    }

		    for (MapObject mapObj : cell.getTile().getObjects()) {
			if (mapObj instanceof RectangleMapObject) {
			    final Rectangle collisionArea = ((RectangleMapObject) mapObj).getRectangle();
			    collisionAreas.add(new Rectangle(x * tileWidth * MapManager.WORLD_UNITS_PER_PIXEL, y * tileHeight * MapManager.WORLD_UNITS_PER_PIXEL,
				    collisionArea.width * MapManager.WORLD_UNITS_PER_PIXEL, collisionArea.height * MapManager.WORLD_UNITS_PER_PIXEL));
			}
		    }
		}
	    }
	}
    }

    private void parseEntities() {
	// TODO Auto-generated method stub

    }

    private void parsePortals() {
	for (MapLayer mapLayer : tiledMap.getLayers()) {
	    if (mapLayer instanceof TiledMapTileLayer || !"Portals".equals(mapLayer.getName())) {
		continue;
	    }

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
		    portals.add(new Portal(portalArea, new Vector2(targetTileIndexX, targetTileIndexY), targetMapID));
		}
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

    public void dispose() {
	tiledMap.dispose();
    }

    public Color getBackgroundColor() {
	return backgroundColor;
    }

}
