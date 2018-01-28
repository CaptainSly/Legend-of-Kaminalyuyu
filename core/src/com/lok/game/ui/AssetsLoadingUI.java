package com.lok.game.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.lok.game.Utils;

public class AssetsLoadingUI extends ScreenUI {
    private final Bar loadingBar;

    public AssetsLoadingUI(AssetManager assetManager, Skin skin) {
	super(assetManager, skin);

	loadingBar = new Bar(skin, Utils.getLabel("Label.LoadingAssets"), 1080, false);
	loadingBar.setPosition(100, 50);

	stage.addActor(loadingBar);
    }
    
    @Override
    public void update(float fixedPhysicsStep) {
	loadingBar.setValue(assetManager.getProgress());
        super.update(fixedPhysicsStep);
    }

    @Override
    public boolean handle(Event event) {
	// nothing to handle
	return false;
    }

}
