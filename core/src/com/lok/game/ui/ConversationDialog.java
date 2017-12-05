package com.lok.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

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
	leftContent.add(titleLabel).row();
	entityImg = skin.get("missing", Image.class);
	leftContent.add(entityImg).expand().fill().bottom().padBottom(0);
	getContentTable().add(leftContent).expand().fill();

	rightContent = new Table(skin);
	textLabel = new Label("", skin.get("normal", LabelStyle.class));
	textLabel.setAlignment(Align.top, Align.left);
	textLabel.getStyle().font.getData().markupEnabled = true;
	rightContent.add(textLabel).expand().fill().top().padTop(15).row();
	rightContent.add(getButtonTable()).right().bottom();
	getContentTable().add(rightContent).expand().fill().padRight(10);

	// TODO test stuff
	Colors.put("Highlight", Color.FIREBRICK);
    }

    public void update(String title, String entityImgID, String text, Array<String> choices) {
	titleLabel.setText(title);
	this.entityImg.setDrawable(getSkin(), entityImgID);
	textLabel.setText(text + " [Highlight]Test123 [] 456");

	getButtonTable().clearChildren();
	for (int i = 0; i < choices.size; ++i) {
	    final TextButton txtButton = new TextButton(choices.get(i), getSkin().get("default", TextButtonStyle.class));
	    txtButton.setUserObject(i);
	    button(txtButton);
	}
    }

    @Override
    protected void result(Object object) {
	cancel();
	super.result(object);
    }
}
