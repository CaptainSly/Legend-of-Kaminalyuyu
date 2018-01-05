package com.lok.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.lok.game.Utils;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.ui.Touchpad.TouchpadDirection;
import com.lok.game.ui.UIEventListener.UIEvent;

public class TownUI extends InputAdapter implements EventListener {
    private final Stage			 stage;
    private final Skin			 skin;
    private final ConversationDialog	 convDialog;
    private final InputMultiplexer	 inputMultiplexer;

    private final Array<ImageButton>	 btn_townLocations;
    private final Array<Label>		 townLocationLabels;
    private ImageButton			 btn_currentSelectedLocation;

    private final Touchpad		 touchpad;
    private final TextButton		 btn_Select;
    private final AnimationActor	 selectionActor;

    private final Array<UIEventListener> uiEventListeners;

    public TownUI() {
	this.stage = new Stage(new FitViewport(1280, 720));
	this.skin = Utils.getUISkin();
	this.convDialog = new ConversationDialog(skin);
	this.convDialog.addListener(this);
	this.uiEventListeners = new Array<UIEventListener>();
	this.btn_townLocations = new Array<ImageButton>();
	this.townLocationLabels = new Array<Label>();
	this.btn_currentSelectedLocation = null;

	// background
	stage.addActor(skin.get("village-bgd", Image.class));

	// input handling actors
	this.touchpad = new Touchpad(skin);
	touchpad.setPosition(15, 15);
	touchpad.addListener(this);
	stage.addActor(touchpad);

	this.btn_Select = new TextButton(Utils.getLabel("Button.ok"), skin, "default");
	btn_Select.setPosition(1280 - 15 - btn_Select.getWidth(), 15);
	btn_Select.addListener(this);
	stage.addActor(btn_Select);

	this.selectionActor = new AnimationActor("ui/ui.atlas", "selection_sphere", 8, 1, 0.05f);
	selectionActor.setPosition(0, 0);
	selectionActor.scaleBy(0.75f);
	stage.addActor(selectionActor);

	this.inputMultiplexer = new InputMultiplexer(this, stage);
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
	btn_townLocations.add(locationButton);

	final Label label = new Label(Utils.getLabel("Entity." + entityID + ".name"), skin, "townlocation");
	label.setPosition(x + 50, y + 20);
	label.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(1, Interpolation.fade)));
	townLocationLabels.add(label);

	stage.addActor(locationButton);
	stage.addActor(label);
    }

    public void clearTownLocations() {
	for (ImageButton button : btn_townLocations) {
	    button.remove();
	}
	btn_townLocations.clear();
	for (Label lbl : townLocationLabels) {
	    lbl.remove();
	}
	townLocationLabels.clear();
    }

    public void selectLocation(EntityID entityID) {
	for (ImageButton btn : btn_townLocations) {
	    if (entityID.equals(btn.getUserObject())) {
		selectionActor.setPosition(btn.getX() + 23, btn.getY() + 60);
		btn_currentSelectedLocation = btn;
		return;
	    }
	}
    }

    @Override
    public boolean keyDown(int keycode) {
	switch (keycode) {
	    case Keys.UP:
		touchpad.setChecked(TouchpadDirection.UP, true);
		return true;
	    case Keys.DOWN:
		touchpad.setChecked(TouchpadDirection.DOWN, true);
		return true;
	    case Keys.LEFT:
		touchpad.setChecked(TouchpadDirection.LEFT, true);
		return true;
	    case Keys.RIGHT:
		touchpad.setChecked(TouchpadDirection.RIGHT, true);
		return true;
	    case Keys.ENTER:
		btn_Select.setChecked(true);
		return true;
	    default:
		return false;
	}
    }

    @Override
    public boolean keyUp(int keycode) {
	switch (keycode) {
	    case Keys.UP:
		touchpad.setChecked(TouchpadDirection.UP, false);
		return true;
	    case Keys.DOWN:
		touchpad.setChecked(TouchpadDirection.DOWN, false);
		return true;
	    case Keys.LEFT:
		touchpad.setChecked(TouchpadDirection.LEFT, false);
		return true;
	    case Keys.RIGHT:
		touchpad.setChecked(TouchpadDirection.RIGHT, false);
		return true;
	    case Keys.ENTER:
		btn_Select.setChecked(false);
		return true;
	    default:
		return false;
	}
    }

    @Override
    public boolean handle(Event event) {
	if (event instanceof InputEvent) {
	    final InputEvent inputEvent = (InputEvent) event;
	    if (InputEvent.Type.exit.equals(inputEvent.getType()) || InputEvent.Type.touchUp.equals(inputEvent.getType())) {
		if (touchpad.contains(event.getTarget())) {
		    ((Button) event.getTarget()).setChecked(false);
		    return true;
		} else if (btn_Select.getLabel().equals(event.getTarget())) {
		    btn_Select.setChecked(false);
		    return true;
		}
	    }

	    return false;
	}

	if (touchpad.contains(event.getTarget())) {
	    final Button button = (Button) event.getTarget();
	    if (button.isChecked()) {
		// change selection
		final TouchpadDirection direction = (TouchpadDirection) button.getUserObject();
		for (UIEventListener listener : uiEventListeners) {
		    listener.onUIEvent(button, direction.getUIEvent());
		}
	    }

	    return true;
	}

	if (btn_Select.equals(event.getTarget())) {
	    if (btn_Select.isChecked()) {
		if (convDialog.isShown()) {
		    final TextButton btn = convDialog.getCurrentSelectedChoice();
		    for (UIEventListener listener : uiEventListeners) {
			listener.onUIEvent(btn, UIEvent.CONVERSATION_CHOICE_SELECTED);
		    }
		} else {
		    for (UIEventListener listener : uiEventListeners) {
			listener.onUIEvent(btn_currentSelectedLocation, UIEvent.SELECT_ENTITY);
		    }
		}
	    }

	    return true;
	}

	return false;
    }

    public void show() {
	touchpad.uncheckAll();
	btn_Select.setChecked(false);
	hideConversationDialog();
	selectionActor.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(1, Interpolation.fade)));
	Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public void render(float delta) {
	Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

	convDialog.update(delta);
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
    }

    public void showConversationDialog() {
	convDialog.show(stage).setPosition(convDialog.getX() + 30, 20);
	touchpad.toFront();
	btn_Select.toFront();
    }

    public void updateConversationDialog(String title, String conversationImage, String text) {
	convDialog.updateContent(title, conversationImage, text);
    }

    public void addConversationDialogChoice(String choiceText, int choiceIndex) {
	convDialog.addChoice(choiceText, choiceIndex);
    }

    public void hideConversationDialog() {
	convDialog.hide();
    }

    public void selectConversationChoice(int index) {
	convDialog.selectChoice(index);
    }

    public void nextConversationChoice() {
	convDialog.selectNextChoice();
    }

    public void previousConversationChoice() {
	convDialog.selectPreviousChoice();
    }
}
