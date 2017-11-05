package com.lok.game.screens;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.lok.game.GameInputProcessor;
import com.lok.game.GameRenderer;
import com.lok.game.ecs.EntityEngine;
import com.lok.game.ecs.components.AnimationComponent;
import com.lok.game.ecs.components.SpeedComponent;
import com.lok.game.map.MapManager;
import com.lok.game.map.MapManager.MapID;

public class GameScreen implements Screen {
    private float		 accumulator;
    private final float		 fixedPhysicsStep;
    private final GameRenderer	 renderer;
    private final EntityEngine	 entityEngine;
    private final InputProcessor inputProcessor;

    public GameScreen() {
	fixedPhysicsStep = 1.0f / 60.0f;
	accumulator = 0.0f;

	final ComponentMapper<SpeedComponent> speedComponentMapper = ComponentMapper.getFor(SpeedComponent.class);
	final ComponentMapper<AnimationComponent> animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);
	inputProcessor = new GameInputProcessor(speedComponentMapper, animationComponentMapper);

	this.entityEngine = EntityEngine.getEngine();
	this.renderer = new GameRenderer();

	MapManager.getManager().changeMap(MapID.DEMON_LAIR_01);
    }

    @Override
    public void show() {
	// TODO Auto-generated method stub
	Gdx.input.setInputProcessor(inputProcessor);
    }

    @Override
    public void render(float delta) {
	if (delta > 0.25f) {
	    delta = 0.25f;
	}

	accumulator += delta;
	while (accumulator >= fixedPhysicsStep) {
	    entityEngine.update(fixedPhysicsStep);
	    accumulator -= fixedPhysicsStep;
	}

	renderer.render(accumulator / fixedPhysicsStep);
    }

    @Override
    public void resize(int width, int height) {
	renderer.resize(width, height);
    }

    @Override
    public void pause() {
	// TODO Auto-generated method stub

    }

    @Override
    public void resume() {
	// TODO Auto-generated method stub

    }

    @Override
    public void hide() {
	// TODO Auto-generated method stub
	Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
	renderer.dispose();
    }

}
