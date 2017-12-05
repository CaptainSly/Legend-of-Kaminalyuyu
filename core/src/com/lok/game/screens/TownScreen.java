package com.lok.game.screens;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.lok.game.Utils;
import com.lok.game.conversation.Conversation;
import com.lok.game.conversation.ConversationChoice;
import com.lok.game.conversation.ConversationListener;
import com.lok.game.conversation.ConversationNode;
import com.lok.game.ecs.EntityEngine;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.ecs.components.ConversationComponent;
import com.lok.game.ui.TownUI;
import com.lok.game.ui.UIEventListener;

public class TownScreen implements Screen, ConversationListener, UIEventListener {
    private final TownUI				 townUI;

    private Conversation				 currentConversation;
    private final ComponentMapper<ConversationComponent> convCompMapper;

    private final IntMap<Entity>			 entityMap;

    public TownScreen() {
	this.townUI = new TownUI();
	this.townUI.addUIEventListener(this);
	this.convCompMapper = ComponentMapper.getFor(ConversationComponent.class);
	this.entityMap = new IntMap<Entity>();

	/*
	 * btnElder = addTownLocation("Elder", skin, 537, 570);
	 * btnBlacksmith = addTownLocation("Blacksmith", skin, 430, 295);
	 * btnShaman = addTownLocation("Shaman", skin, 105, 235);
	 * btnPortal = addTownLocation("Portal", skin, 837, 145);
	 */
	this.entityMap.put(EntityID.PLAYER.ordinal(), EntityEngine.getEngine().createEntity(EntityID.PLAYER, 0, 0));

	this.entityMap.put(EntityID.ELDER.ordinal(), EntityEngine.getEngine().createEntity(EntityID.ELDER, 537, 570));
	townUI.addTownLocation(EntityID.ELDER, 537, 570);
    }

    @Override
    public void show() {
	townUI.show();
    }

    @Override
    public void render(float delta) {
	townUI.render(delta);
    }

    @Override
    public void resize(int width, int height) {
	townUI.resize(width, height);
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
	townUI.hide();
    }

    @Override
    public void dispose() {
	townUI.dispose();
    }

    @Override
    public void onUIEvent(Actor triggerActor, UIEvent event) {
	switch (event) {
	    case SELECT_ENTITY:
		final EntityID entityID = (EntityID) triggerActor.getUserObject();

		if (currentConversation != null) {
		    currentConversation.removeConversationListener(this);
		}
		currentConversation = Conversation.load(convCompMapper.get(entityMap.get(entityID.ordinal())).currentConversationID);
		currentConversation.addConversationListener(this);
		currentConversation.startConversation();

		break;
	    case CONVERSATION_CHOICE_SELECTED:
		final int choiceIndex = (int) triggerActor.getUserObject();

		currentConversation.triggerConversationChoice(choiceIndex);

		break;
	    default:
		break;
	}
    }

    @Override
    public void onStartConversation(ConversationNode startNode) {
	// TODO Auto-generated method stub
	final Array<String> choices = new Array<String>();
	for (ConversationChoice choice : startNode.getChoices()) {
	    choices.add(Utils.getLabel(choice.getTextID()));
	}
	townUI.updateConversationDialog( // params
		Utils.getLabel("Entity." + startNode.getEntityID() + ".name"), // title
		convCompMapper.get(entityMap.get(startNode.getEntityID().ordinal())).conversationImage, // image
		Utils.getLabel(startNode.getTextID()), // text
		choices); // choices
	townUI.showConversationDialog();
    }

    @Override
    public void onTriggerConversationChoice(ConversationChoice choice, ConversationNode nextNode) {
	// TODO Auto-generated method stub
	final Array<String> choices = new Array<String>();
	for (ConversationChoice cc : nextNode.getChoices()) {
	    choices.add(Utils.getLabel(cc.getTextID()));
	}
	townUI.updateConversationDialog( // params
		Utils.getLabel("Entity." + nextNode.getEntityID() + ".name"), // title
		convCompMapper.get(entityMap.get(nextNode.getEntityID().ordinal())).conversationImage, // image
		Utils.getLabel(nextNode.getTextID()), // text
		choices); // choices
    }

    @Override
    public void onEndConversation(ConversationChoice choice, ConversationNode currentNode) {
	townUI.hideConversationDialog();
    }

}
