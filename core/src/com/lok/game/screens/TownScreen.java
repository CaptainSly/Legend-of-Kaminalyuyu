package com.lok.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.lok.game.AssetManager;

public class TownScreen extends Stage implements Screen, EventListener {
    private final Skin	      skin;

    private final ImageButton btnElder;
    private final ImageButton btnBlacksmith;
    private final ImageButton btnShaman;
    private final ImageButton btnPortal;

    public TownScreen() {
	super(new FitViewport(1280, 720));

	skin = AssetManager.getManager().getAsset("ui/ui.json", Skin.class, new SkinLoader.SkinParameter("ui/ui.atlas"));
	final Table table = new Table(skin);
	table.setFillParent(true);
	table.setBackground(new TextureRegionDrawable(new TextureRegion(AssetManager.getManager().getAsset("ui/village.jpg", Texture.class))));
	addActor(table);

	btnElder = addTownLocation("Elder", skin, 537, 570);
	btnBlacksmith = addTownLocation("Blacksmith", skin, 430, 295);
	btnShaman = addTownLocation("Shaman", skin, 105, 235);
	btnPortal = addTownLocation("Portal", skin, 837, 145);
    }

    private ImageButton addTownLocation(String tooltipKey, Skin skin, int x, int y) {
	final ImageButton locationButton = new ImageButton(skin, "town-location");
	locationButton.setSize(64, 64);
	locationButton.setPosition(x, y);
	locationButton.getImage().setOrigin(locationButton.getWidth() * 0.5f, locationButton.getHeight() * 0.5f);
	locationButton.getImage().addAction(Actions.forever(Actions.rotateBy(7.5f)));
	locationButton.addListener(this);

	final Label label = new Label(tooltipKey, skin, "title");
	label.setPosition(x + 50, y + 20);

	addActor(locationButton);
	addActor(label);

	return locationButton;
    }

    @Override
    public void show() {
	btnElder.setChecked(false);
	btnBlacksmith.setChecked(false);
	btnShaman.setChecked(false);
	btnPortal.setChecked(false);
	Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
	Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	act(delta);
	getViewport().apply();
	draw();
    }

    @Override
    public void resize(int width, int height) {
	getViewport().update(width, height, true);
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
	Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
	super.dispose();
	AssetManager.getManager().dispose();
    }

    @Override
    public boolean handle(Event event) {
	if (btnPortal.equals(event.getTarget()) && btnPortal.isChecked()) {
	    btnPortal.setChecked(false);

	    Dialog dia = new Dialog("Portal", skin, "dialog") {
		protected void result(Object object) {
		    if (Boolean.TRUE.equals(object)) {
			ScreenManager.getManager().setScreen(GameScreen.class);
		    }
		}
	    };
	    dia.getTitleTable().bottom().right().pad(0, 30, 0, 30);

	    dia.getContentTable().add(skin.get("portal", Image.class));
	    dia.text("Do you really want to enter the portal to the demon lair?", skin.get("normal", LabelStyle.class));
	    dia.getContentTable().pad(30, 30, 0, 30);

	    dia.button("Yes", true, skin.get("default", TextButtonStyle.class));
	    dia.button("No", false, skin.get("default", TextButtonStyle.class));
	    dia.getButtonTable().right().pad(0, 0, 25, 25);

	    dia.show(this).setPosition(dia.getX(), 20);

	    return true;
	} else if (btnElder.equals(event.getTarget()) && btnElder.isChecked()) {
	    btnElder.setChecked(false);
	    Dialog dia = new Dialog("Elder", skin, "dialog");
	    dia.getTitleTable().bottom().right().pad(0, 30, 0, 30);

	    dia.getContentTable().add(skin.get("elder", Image.class));
	    dia.text("You need to enter the demon lair to be an awesome hero!\nGo now!!!", skin.get("normal", LabelStyle.class));
	    dia.getContentTable().pad(30, 30, 0, 30);

	    dia.button("Next", null, skin.get("default", TextButtonStyle.class));
	    dia.getButtonTable().right().pad(0, 0, 25, 25);

	    dia.show(this).setPosition(dia.getX(), 20);
	}

	return false;
    }

}
