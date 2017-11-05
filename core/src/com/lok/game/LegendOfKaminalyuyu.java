package com.lok.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.lok.game.map.MapManager;
import com.lok.game.screens.GameScreen;

public class LegendOfKaminalyuyu extends Game {
    public enum ScreenID {
	GameScreen(new GameScreen());

	private final Screen screen;

	private ScreenID(Screen screen) {
	    this.screen = screen;
	}

	public Screen getScreen() {
	    return screen;
	}
    }

    @Override
    public void create() {
	Gdx.app.setLogLevel(Application.LOG_DEBUG);

	Gdx.graphics.setTitle(Utils.getLabel("GameWindow.Title"));

	Utils.changeScreen(ScreenID.GameScreen);
    }

    @Override
    public void render() {
	if (screen != null) {
	    // use raw delta time instead of deltatime because deltaTime
	    // uses an average value instead of the real value between two frames
	    screen.render(Gdx.graphics.getRawDeltaTime());
	}
    }

    @Override
    public void dispose() {
	MapManager.getManager().dispose();
	AssetManager.getManager().dispose();

	super.dispose();
    }
}
