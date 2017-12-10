package com.lok.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Align;

public class ConversationDialog extends Dialog {
    private final Table	leftContent;
    private final Label	titleLabel;
    private final Image	entityImg;

    private final Table	rightContent;
    private final Label	textLabel;

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
	textLabel.setAlignment(Align.top, Align.left);
	textLabel.getStyle().font.getData().markupEnabled = true;
	textLabel.setWrap(true);
	rightContent.add(textLabel).expand().fill().top().padTop(15).width(800).row();
	rightContent.add(getButtonTable()).right().bottom();
	getContentTable().add(rightContent).expand().fill().padRight(10);
    }

    public void update(String title, String entityImgID, String text) {
	titleLabel.setText(title);
	this.entityImg.setDrawable(getSkin(), entityImgID);
	textLabel.setText(text);

	getButtonTable().clearChildren();
    }

    public void addChoice(String choiceText, int choiceIndex) {
	final TextButton txtButton = new TextButton(choiceText, getSkin().get("default", TextButtonStyle.class));
	txtButton.setUserObject(choiceIndex);
	button(txtButton);
    }

    @Override
    protected void result(Object object) {
	cancel();
	super.result(object);
    }
}
