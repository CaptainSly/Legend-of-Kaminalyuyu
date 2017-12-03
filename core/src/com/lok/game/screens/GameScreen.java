package com.lok.game.screens;

import com.badlogic.gdx.Screen;
import com.lok.game.GameLogic;
import com.lok.game.GameRenderer;
import com.lok.game.ecs.EntityEngine;
import com.lok.game.ui.PlayerHUD;

public class GameScreen implements Screen {
    private float	       accumulator;
    private final float	       fixedPhysicsStep;
    private final GameLogic    gameLogic;
    private final GameRenderer renderer;
    private final EntityEngine entityEngine;
    private final PlayerHUD    playerHUD;

    public GameScreen() {
	fixedPhysicsStep = 1.0f / 30.0f;
	accumulator = 0.0f;

	this.playerHUD = new PlayerHUD();
	this.entityEngine = EntityEngine.getEngine();
	this.renderer = new GameRenderer();
	this.gameLogic = new GameLogic(renderer);
	playerHUD.addPlayerHUDListener(gameLogic);
    }

    @Override
    public void show() {
	playerHUD.show();
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
	playerHUD.render(delta);
    }

    @Override
    public void resize(int width, int height) {
	playerHUD.resize(width, height);
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
	playerHUD.hide();
	gameLogic.hide();
    }

    @Override
    public void dispose() {
	playerHUD.dispose();
	renderer.dispose();
    }

}
