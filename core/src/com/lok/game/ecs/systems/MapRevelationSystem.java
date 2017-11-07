package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.lok.game.ecs.components.MapRevelationComponent;
import com.lok.game.ecs.components.SizeComponent;
import com.lok.game.map.Map;
import com.lok.game.map.MapListener;
import com.lok.game.map.MapManager;

public class MapRevelationSystem extends IteratingSystem implements MapListener {
    private final ComponentMapper<SizeComponent>	  sizeComponentMapper;
    private final ComponentMapper<MapRevelationComponent> mapRevelationComponentMapper;
    private Map						  map;

    public MapRevelationSystem(ComponentMapper<SizeComponent> sizeComponentMapper, ComponentMapper<MapRevelationComponent> mapRevelationComponentMapper) {
	super(Family.all(MapRevelationComponent.class, SizeComponent.class).get());

	this.sizeComponentMapper = sizeComponentMapper;
	this.mapRevelationComponentMapper = mapRevelationComponentMapper;
	map = null;

	MapManager.getManager().addListener(this);
    }

    @Override
    public void onMapChange(MapManager manager, Map map) {
	this.map = map;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	if (map != null) {
	    final SizeComponent sizeComponent = sizeComponentMapper.get(entity);
	    final MapRevelationComponent mapRevelationComponent = mapRevelationComponentMapper.get(entity);

	    map.revealArea(sizeComponent.boundingRectangle, mapRevelationComponent.revelationRadius);
	}
    }

}
