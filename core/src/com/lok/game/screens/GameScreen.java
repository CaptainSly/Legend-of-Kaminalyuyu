package com.lok.game.screens;

import com.badlogic.gdx.Screen;
import com.lok.game.GameLogic;
import com.lok.game.GameRenderer;
import com.lok.game.ecs.EntityEngine;

public class GameScreen implements Screen {
    private float	       accumulator;
    private final float	       fixedPhysicsStep;
    private final GameLogic    gameLogic;
    private final GameRenderer renderer;
    private final EntityEngine entityEngine;

    public GameScreen() {
	fixedPhysicsStep = 1.0f / 30.0f;
	accumulator = 0.0f;

	this.entityEngine = EntityEngine.getEngine();
	this.renderer = new GameRenderer();
	this.gameLogic = new GameLogic(renderer);
    }

    @Override
    public void show() {
	gameLogic.show();
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
	gameLogic.pause();
    }

    @Override
    public void resume() {
	gameLogic.resume();
    }

    @Override
    public void hide() {
	gameLogic.hide();
    }

    @Override
    public void dispose() {
	renderer.dispose();
    }

}
