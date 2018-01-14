package com.lok.game.ecs.components;

public class AIWanderComponent implements Component<AIWanderComponent> {
    public float wanderTime = 0.0f;

    @Override
    public void reset() {
	wanderTime = 0.0f;
    }

    @Override
    public void initialize(AIWanderComponent configComponent) {
    }
}
