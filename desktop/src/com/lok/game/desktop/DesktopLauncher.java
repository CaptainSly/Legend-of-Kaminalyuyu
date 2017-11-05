package com.lok.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.lok.game.LegendOfKaminalyuyu;

public class DesktopLauncher {
    public static void main(String[] arg) {
	LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

	config.title = "Legend of Kaminalyuyu";
	config.width = 1280;
	config.height = 720;

	new LwjglApplication(new LegendOfKaminalyuyu(), config);
    }
}
