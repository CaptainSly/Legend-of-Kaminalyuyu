package com.lok.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.lok.game.map.MapManager;
import com.lok.game.screen.ScreenManager;
import com.lok.game.screen.TownScreen;

public class LegendOfKaminalyuyu extends Game {
    @Override
    public void create() {
	Gdx.app.setLogLevel(Application.LOG_INFO);

	Gdx.graphics.setTitle(Utils.getLabel("GameWindow.Title"));

	Utils.initializeCustomColors();
	ScreenManager.getManager().setScreen(TownScreen.class);
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
	ScreenManager.getManager().dispose();
    }

    /*
     * GDX lifecycle
     * 1) create
     * 2) resize
     * 3) render in endless loop
     * 
     * pause -> lose focus
     * resume -> gained focus back
     * 
     * 4) pause
     * 5) dispose
     * 
     * hide -> screen is no longer current one
     * show -> screen becomes the current one
     * 
     */
}
