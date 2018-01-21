package com.lok.game.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;
import com.lok.game.ui.Animation.AnimationID;

public class ConversationDialog extends Dialog {
    private final Table		 leftContent;
    private final Label		 titleLabel;
    private final Image		 entityImg;

    private final Vector2	 selectionActorPosition;
    private final AnimationActor selectionActor;
    private int			 currentSelection;

    private final Table		 rightContent;
    private float		 timeForNextChar;
    private StringBuilder	 textStrBuilder;
    private final Label		 textLabel;

    private boolean		 isShown;

    public ConversationDialog(Skin skin) {
	super("", skin, "dialog");

	leftContent = new Table(skin);
	titleLabel = new Label("", skin.get("dialog-title", LabelStyle.class));
	leftContent.add(titleLabel).padLeft(10).row();
	entityImg = skin.get("missing", Image.class);
	leftContent.add(entityImg).expand().fill().bottom().padBottom(0);
	getContentTable().add(leftContent).expand().fill();

	rightContent = new Table(skin);
	textLabel = new Label("", skin.get("normal", LabelStyle.class));
	textLabel.setAlignment(Align.topLeft, Align.left);
	textLabel.getStyle().font.getData().markupEnabled = true;
	this.timeForNextChar = 0;
	this.textStrBuilder = new StringBuilder();

	rightContent.add(textLabel).expand().fill().top().padTop(15).width(800).height(140).row();
	rightContent.add(getButtonTable()).right().bottom();
	getContentTable().add(rightContent).expand().fill().padRight(10);

	this.selectionActor = new AnimationActor(AnimationID.SELECTION_SPHERE);
	selectionActor.setPosition(0, 0);
	selectionActor.rotateBy(270);
	selectionActor.scaleBy(0.75f);
	this.addActor(selectionActor);
	this.selectionActorPosition = new Vector2(0, 0);

	this.isShown = false;
    }

    public void update(float deltaTime) {
	if (textStrBuilder.length == textLabel.getText().length) {
	    return;
	}

	timeForNextChar += deltaTime;
	if (timeForNextChar >= 0.05) {
	    timeForNextChar = 0;
	    char charToAdd = textStrBuilder.charAt(textLabel.getText().length);
	    textLabel.getText().append(charToAdd);

	    if (charToAdd == '[') {
		// parse markup color
		while (textStrBuilder.length != textLabel.getText().length) {
		    charToAdd = textStrBuilder.charAt(textLabel.getText().length);
		    textLabel.getText().append(charToAdd);
		    if (charToAdd == ']') {
			if (textStrBuilder.length != textLabel.getText().length) {
			    textLabel.getText().append(textStrBuilder.charAt(textLabel.getText().length));
			}
			break;
		    }
		}
	    }

	    textLabel.invalidateHierarchy();
	}
    }

    public void updateContent(String title, String entityImgID, String text) {
	titleLabel.setText(title);
	this.entityImg.setDrawable(getSkin(), entityImgID);

	timeForNextChar = 0;
	textStrBuilder.setLength(0);
	textStrBuilder.append(text);
	textLabel.setText("");

	getButtonTable().clearChildren();
	currentSelection = 0;
    }

    public void addChoice(String choiceText, int choiceIndex) {
	final TextButton txtButton = new TextButton(choiceText, getSkin().get("dialog-btn", TextButtonStyle.class));
	txtButton.setUserObject(choiceIndex);
	button(txtButton);
    }

    public void selectChoice(int index) {
	currentSelection = index;
	final int numChoices = getButtonTable().getChildren().size;
	if (currentSelection < 0) {
	    currentSelection = numChoices - 1;
	} else if (currentSelection >= numChoices) {
	    currentSelection = 0;
	}

	// need to call validate to layout all actors in order to retrieve the correct coordinates
	validate();
	selectionActorPosition.set(0, 0);
	getButtonTable().getChildren().get(currentSelection).localToStageCoordinates(selectionActorPosition);
	this.stageToLocalCoordinates(selectionActorPosition);

	selectionActor.setPosition(selectionActorPosition.x - 30, selectionActorPosition.y + 7);
    }

    public void selectNextChoice() {
	selectChoice(++currentSelection);
    }

    public void selectPreviousChoice() {
	selectChoice(--currentSelection);
    }

    public boolean isShown() {
	return isShown;
    }

    @Override
    public Dialog show(Stage stage) {
	this.isShown = true;
	selectionActor.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade)));
	return super.show(stage);
    }

    @Override
    public void hide() {
	this.isShown = false;
	selectionActor.addAction(Actions.fadeOut(0.4f, Interpolation.fade));
	super.hide();
    }

    @Override
    protected void result(Object object) {
	cancel();
	super.result(object);
    }

    public TextButton getCurrentSelectedChoice() {
	return (TextButton) getButtonTable().getChildren().get(currentSelection);
    }
}
