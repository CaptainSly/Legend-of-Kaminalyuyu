package com.lok.game.sound;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.lok.game.Utils;

public class SoundManager {
    private static SoundManager	instance = null;

    private Music		music;
    private final AssetManager	assetManager;

    private SoundManager() {
	music = null;
	assetManager = Utils.getAssetManager();
    }

    public static SoundManager getManager() {
	if (instance == null) {
	    instance = new SoundManager();
	}
	return instance;
    }

    public void playMusic(String musicFilePath, boolean loop) {
	if (music != null) {
	    music.stop();
	}
	music = assetManager.get(musicFilePath, Music.class);
	music.setLooping(loop);
	music.play();
    }

    public void stopMusic() {
	if (music != null) {
	    music.stop();
	}
    }

    public long playSound(String soundFilePath, boolean loop) {
	final Sound sound = assetManager.get(soundFilePath, Sound.class);
	final long soundID = sound.play();
	sound.setLooping(soundID, loop);
	return soundID;
    }

    public void stopSound(String soundFilePath, long soundID) {
	assetManager.get(soundFilePath, Sound.class).stop(soundID);
    }
}
