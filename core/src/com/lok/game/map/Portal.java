package com.lok.game.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.lok.game.ecs.components.SizeComponent;
import com.lok.game.map.MapManager.MapID;

public class Portal {
    private static final String	TAG = Portal.class.getSimpleName();
    private final Rectangle	area;
    private final Vector2	targetPosition;
    private final MapID		targetMapID;

    public Portal(Rectangle area, Vector2 targetPosition, MapID targetMapID) {
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

    public void activate(Entity entity) {
	Gdx.app.debug(TAG, "Entity " + entity + " activated portal with target map " + targetMapID + " and position " + targetPosition);

	if (targetMapID != null) {
	    MapManager.getManager().changeMap(targetMapID);
	}

	final SizeComponent sizeComp = entity.getComponent(SizeComponent.class);

	if (sizeComp == null) {
	    return;
	}

	sizeComp.interpolatedPosition.set(targetPosition);
	sizeComp.boundingRectangle.setPosition(targetPosition);
    }
}