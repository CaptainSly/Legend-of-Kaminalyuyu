package com.lok.game.ecs.components;

import com.lok.game.ecs.EntityEngine.EntityID;

public class IDComponent implements Component<IDComponent> {
    public EntityID entityID = null;

    @Override
    public void reset() {
	entityID = null;
    }

    @Override
    public void initialize(IDComponent configComponent) {
	this.entityID = configComponent.entityID;
    }
}
