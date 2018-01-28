package com.lok.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.lok.game.screen.Screen;

public abstract class ScreenUI extends InputAdapter implements EventListener {
    protected final AssetManager	   assetManager;
    protected final Skin		   skin;
    protected final Stage		   stage;
    protected final Array<UIEventListener> uiEventListeners;
    private final InputMultiplexer	   inputMultiplexer;

    public ScreenUI(AssetManager assetManager, Skin skin) {
	this.assetManager = assetManager;
	this.skin = skin;
	this.stage = new Stage(new FitViewport(1280, 720));
	this.uiEventListeners = new Array<UIEventListener>();
	this.inputMultiplexer = new InputMultiplexer(this, stage);
    }

    public void show() {
	Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public void update(float fixedPhysicsStep) {
	stage.act(fixedPhysicsStep);
    }

    public void render(float alpha) {
	Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	stage.getViewport().apply();
	stage.draw();
    }

    public void resize(int width, int height) {
	stage.getViewport().update(width, height, true);
    }

    public void hide() {
	Gdx.input.setInputProcessor(null);
    }

    public void dispose() {
	stage.dispose();
    }

    public void addUIEventListener(Screen<? extends ScreenUI> screen) {
	this.uiEventListeners.add(screen);
    }

    public void removeUIEventListener(Screen<? extends ScreenUI> screen) {
	this.uiEventListeners.removeValue(screen, false);
    }

}
