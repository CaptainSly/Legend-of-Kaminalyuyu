package com.lok.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;

public class Bar extends WidgetGroup {
    private final ProgressBar progressBar;
    private final Image	      frame;
    private final TextButton  label;

    public Bar(Skin skin, String text, float width, boolean vertical) {
	super();
	progressBar = new ProgressBar(0, 1, 0.1f, vertical, skin, "default");
	progressBar.setRound(false);
	progressBar.setPosition(6, 6);
	progressBar.setWidth(width);

	frame = skin.get("progressbar_frame", Image.class);
	frame.setSize(progressBar.getWidth() + 10, progressBar.getHeight() + 12);

	label = new TextButton(text, skin, "progressBar");
	label.setSize(frame.getWidth(), frame.getHeight());

	addActor(progressBar);
	addActor(frame);
	addActor(label);
    }

    public void setValue(float value, float animationDuration) {
	progressBar.setAnimateDuration(animationDuration);
	progressBar.setValue(value);
    }

    public void setText(String text) {
	label.setText(text);
    }
}
