package com.lok.game.screens;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.lok.game.Utils;
import com.lok.game.conversation.Conversation;
import com.lok.game.conversation.Conversation.ConversationID;
import com.lok.game.conversation.ConversationChoice;
import com.lok.game.conversation.ConversationChoice.ConversationAction;
import com.lok.game.conversation.ConversationListener;
import com.lok.game.conversation.ConversationNode;
import com.lok.game.ecs.EntityEngine;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.ecs.components.ConversationComponent;
import com.lok.game.ui.TownUI;
import com.lok.game.ui.UIEventListener;

public class TownScreen implements Screen, ConversationListener, UIEventListener {
    private final TownUI				 townUI;

    private boolean					 conversationInProgress;
    private Conversation				 currentConversation;
    private final ComponentMapper<ConversationComponent> convCompMapper;

    private final IntMap<Entity>			 entityMap;
    private EntityID					 currentSelection;

    public TownScreen() {
	this.townUI = new TownUI();
	this.townUI.addUIEventListener(this);
	this.convCompMapper = ComponentMapper.getFor(ConversationComponent.class);
	this.entityMap = new IntMap<Entity>();
	this.conversationInProgress = false;

	this.entityMap.put(EntityID.PLAYER.ordinal(), EntityEngine.getEngine().createEntity(EntityID.PLAYER, 0, 0));

	this.entityMap.put(EntityID.ELDER.ordinal(), EntityEngine.getEngine().createEntity(EntityID.ELDER, 537, 570));
	townUI.addTownLocation(EntityID.ELDER, 537, 570);
	this.currentSelection = EntityID.ELDER;
	townUI.selectLocation(currentSelection);
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
	    case RIGHT:
	    case UP:
		if (!conversationInProgress) {
		    if (EntityID.ELDER.equals(currentSelection) && entityMap.containsKey(EntityID.PORTAL.ordinal())) {
			currentSelection = EntityID.PORTAL;
		    } else if (EntityID.BLACKSMITH.equals(currentSelection) && entityMap.containsKey(EntityID.ELDER.ordinal())) {
			currentSelection = EntityID.ELDER;
		    } else if (EntityID.SHAMAN.equals(currentSelection) && entityMap.containsKey(EntityID.BLACKSMITH.ordinal())) {
			currentSelection = EntityID.BLACKSMITH;
		    } else if (EntityID.PORTAL.equals(currentSelection) && entityMap.containsKey(EntityID.SHAMAN.ordinal())) {
			currentSelection = EntityID.SHAMAN;
		    }
		    townUI.selectLocation(currentSelection);
		} else {
		    townUI.nextConversationChoice();
		}
		break;
	    case LEFT:
	    case DOWN:
		if (!conversationInProgress) {
		    if (EntityID.ELDER.equals(currentSelection) && entityMap.containsKey(EntityID.BLACKSMITH.ordinal())) {
			currentSelection = EntityID.BLACKSMITH;
		    } else if (EntityID.BLACKSMITH.equals(currentSelection) && entityMap.containsKey(EntityID.SHAMAN.ordinal())) {
			currentSelection = EntityID.SHAMAN;
		    } else if (EntityID.SHAMAN.equals(currentSelection) && entityMap.containsKey(EntityID.PORTAL.ordinal())) {
			currentSelection = EntityID.PORTAL;
		    } else if (EntityID.PORTAL.equals(currentSelection) && entityMap.containsKey(EntityID.ELDER.ordinal())) {
			currentSelection = EntityID.ELDER;
		    }
		    townUI.selectLocation(currentSelection);
		} else {
		    townUI.previousConversationChoice();
		}
		break;
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

    private void updateConversationDialog(ConversationNode node) {
	townUI.updateConversationDialog( // params
		Utils.getLabel("Entity." + node.getEntityID() + ".name"), // title
		convCompMapper.get(entityMap.get(node.getEntityID().ordinal())).conversationImage, // image
		Utils.getLabel(node.getTextID())); // text

	final int max = node.getChoices().size;
	for (int i = 0; i < max; ++i) {
	    townUI.addConversationDialogChoice(Utils.getLabel(node.getChoices().get(i).getTextID()), i);
	}

	townUI.selectConversationChoice(0);
    }

    @Override
    public void onStartConversation(Conversation conversation, ConversationNode startNode) {
	conversationInProgress = true;
	updateConversationDialog(startNode);
	townUI.showConversationDialog();
	townUI.selectConversationChoice(0);
    }

    @Override
    public void onEndConversation(Conversation conversation, ConversationNode currentNode, ConversationChoice selectedChoice) {
	conversationInProgress = false;
	townUI.hideConversationDialog();
    }

    @Override
    public void onConversationChoiceSelected(Conversation conversation, ConversationNode currentNode, ConversationNode nextNode, ConversationChoice selectedChoice) {
	updateConversationDialog(nextNode);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onConversationAction(Conversation conversation, ConversationNode currentNode, ConversationChoice selectedChoice, ConversationAction action) {
	switch (action.getActionID()) {
	    case ActivateTownLocation: {
		final Array<?> param = (Array<?>) action.getParam();
		final EntityID entityID = EntityID.valueOf((String) param.get(0));
		final Float x = (Float) param.get(1);
		final Float y = (Float) param.get(2);

		this.entityMap.put(entityID.ordinal(), EntityEngine.getEngine().createEntity(entityID, x, y));
		townUI.addTownLocation(entityID, x, y);

		break;
	    }
	    case SetConversation: {
		final Array<?> param = (Array<?>) action.getParam();
		final EntityID entityID = EntityID.valueOf((String) param.get(0));
		final ConversationID conversationID = ConversationID.valueOf((String) param.get(1));

		convCompMapper.get(entityMap.get(entityID.ordinal())).currentConversationID = conversationID;

		break;
	    }
	    case SetScreen:
		try {
		    final Array<?> param = (Array<?>) action.getParam();
		    ScreenManager.getManager().setScreen(ClassReflection.forName((String) param.get(0)));
		} catch (ReflectionException e) {
		    throw new GdxRuntimeException("Invalid screen class for setScreen", e);
		}
		break;
	    default:
		break;
	}
    }
}
