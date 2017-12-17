package com.lok.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.lok.game.AssetManager;
import com.lok.game.Utils;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.ui.UIEventListener.UIEvent;

public class TownUI implements EventListener {
    private final Stage			 stage;
    private final Skin			 skin;
    private final ConversationDialog	 convDialog;

    private final Array<UIEventListener> uiEventListeners;

    public TownUI() {
	this.stage = new Stage(new FitViewport(1280, 720));
	this.skin = Utils.getUISkin();
	this.convDialog = new ConversationDialog(skin);
	this.convDialog.addListener(this);
	this.uiEventListeners = new Array<UIEventListener>();

	// background
	stage.addActor(new Image(new TextureRegionDrawable(new TextureRegion(AssetManager.getManager().getAsset("ui/village.jpg", Texture.class)))));
	stage.setKeyboardFocus(convDialog);
    }

    public void addUIEventListener(UIEventListener listener) {
	this.uiEventListeners.add(listener);
    }

    public void removeUIEventListener(UIEventListener listener) {
	this.uiEventListeners.removeValue(listener, false);
    }

    public void addTownLocation(EntityID entityID, float x, float y) {
	final ImageButton locationButton = new ImageButton(skin, "town-location");

	locationButton.setUserObject(entityID);
	locationButton.setSize(64, 64);
	locationButton.setPosition(x, y);
	locationButton.getImage().setOrigin(locationButton.getWidth() * 0.5f, locationButton.getHeight() * 0.5f);
	locationButton.getImage().addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(1, Interpolation.fade), Actions.forever(Actions.rotateBy(7.5f))));
	locationButton.addListener(this);

	final Label label = new Label(Utils.getLabel("Entity." + entityID + ".name"), skin, "townlocation");
	label.setPosition(x + 50, y + 20);
	label.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(1, Interpolation.fade)));

	stage.addActor(locationButton);
	stage.addActor(label);
    }

    @Override
    public boolean handle(Event event) {
	if (event instanceof InputEvent) {
	    // TODO
	    final InputEvent inputevent = (InputEvent) event;

	    switch (inputevent.getKeyCode()) {
		default:
		    return false;
	    }

	    // return true;
	}

	if (event.getTarget() instanceof ImageButton) {
	    final ImageButton imgButton = (ImageButton) event.getTarget();

	    if (imgButton.isChecked()) {
		imgButton.setChecked(false);

		for (UIEventListener listener : uiEventListeners) {
		    listener.onUIEvent(imgButton, UIEvent.SELECT_ENTITY);
		}
	    }

	    return true;
	} else if (event.getTarget() instanceof TextButton) {
	    final TextButton txtButton = (TextButton) event.getTarget();

	    if (txtButton.isChecked()) {
		txtButton.setChecked(false);

		for (UIEventListener listener : uiEventListeners) {
		    listener.onUIEvent(txtButton, UIEvent.CONVERSATION_CHOICE_SELECTED);
		}
	    }

	    return true;
	}

	return false;
    }

    public void show() {
	Gdx.input.setInputProcessor(stage);
    }

    public void render(float delta) {
	Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	stage.act(delta);
	stage.getViewport().apply();
	stage.draw();
    }

    public void resize(int width, int height) {
	stage.getViewport().update(width, height, true);
    }

    public void hide() {
	convDialog.hide(Actions.fadeOut(0));
	Gdx.input.setInputProcessor(null);
    }

    public void dispose() {
	stage.dispose();
	skin.dispose();
	AssetManager.getManager().dispose();
    }

    public void showConversationDialog() {
	convDialog.show(stage).setY(20);
    }

    public void updateConversationDialog(String title, String conversationImage, String text) {
	convDialog.update(title, conversationImage, text);
    }

    public void addConversationDialogChoice(String choiceText, int choiceIndex) {
	convDialog.addChoice(choiceText, choiceIndex);
    }

    public void hideConversationDialog() {
	convDialog.hide();
    }
}
