package com.lok.game.screen;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.lok.game.LegendOfKaminalyuyu;
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
import com.lok.game.ecs.components.IDComponent;
import com.lok.game.ecs.components.SizeComponent;
import com.lok.game.serialization.TownEntityData;
import com.lok.game.sound.SoundManager;
import com.lok.game.ui.TownUI;

public class TownScreen extends Screen<TownUI> implements ConversationListener {
    private boolean					 conversationInProgress;
    private Conversation				 currentConversation;
    private final ComponentMapper<ConversationComponent> convCompMapper;

    private final IntMap<Entity>			 entityMap;
    private EntityID					 currentSelection;

    public TownScreen(LegendOfKaminalyuyu game, AssetManager assetManager, Skin uiSkin) {
	super(game, assetManager, TownUI.class, uiSkin);
	this.convCompMapper = ComponentMapper.getFor(ConversationComponent.class);
	this.entityMap = new IntMap<Entity>();
    }

    @Override
    public void show() {
	entityMap.clear();
	this.conversationInProgress = false;
	SoundManager.getManager().playMusic("sounds/music/town.ogg", true);

	super.show();
    }

    @Override
    public void hide() {
	super.hide();
	for (Entity entity : entityMap.values()) {
	    EntityEngine.getEngine().removeEntity(entity);
	}
    }

    @Override
    public void onUpdate(float fixedPhysicsStep) {
	// no special update logic needed
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
		    screenUI.selectLocation(currentSelection);
		} else {
		    screenUI.nextConversationChoice();
		}
		SoundManager.getManager().playSound("sounds/effects/menu_selection.wav", false);
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
		    screenUI.selectLocation(currentSelection);
		} else {
		    screenUI.previousConversationChoice();
		}
		SoundManager.getManager().playSound("sounds/effects/menu_selection.wav", false);
		break;
	    case SELECT_ENTITY:
		final EntityID entityID = (EntityID) triggerActor.getUserObject();

		if (currentConversation != null) {
		    currentConversation.removeConversationListener(this);
		}
		currentConversation = Conversation.getConversation(convCompMapper.get(entityMap.get(entityID.ordinal())).currentConversationID);
		currentConversation.addConversationListener(this);
		currentConversation.startConversation();
		SoundManager.getManager().playSound("sounds/effects/menu_selection.wav", false);
		break;
	    case CONVERSATION_CHOICE_SELECTED:
		final int choiceIndex = (int) triggerActor.getUserObject();
		currentConversation.triggerConversationChoice(choiceIndex);
		SoundManager.getManager().playSound("sounds/effects/menu_selection.wav", false);
		break;
	    default:
		break;
	}
    }

    private void updateConversationDialog(ConversationNode node) {
	screenUI.updateConversationDialog( // params
		Utils.getLabel("Entity." + node.getEntityID() + ".name"), // title
		convCompMapper.get(entityMap.get(node.getEntityID().ordinal())).conversationImage, // image
		Utils.getLabel(node.getTextID())); // text

	final int max = node.getChoices().size;
	for (int i = 0; i < max; ++i) {
	    screenUI.addConversationDialogChoice(Utils.getLabel(node.getChoices().get(i).getTextID()), i);
	}

	screenUI.selectConversationChoice(0);
    }

    @Override
    public void onStartConversation(Conversation conversation, ConversationNode startNode) {
	conversationInProgress = true;
	updateConversationDialog(startNode);
	screenUI.showConversationDialog();
	screenUI.selectConversationChoice(0);
    }

    @Override
    public void onEndConversation(Conversation conversation, ConversationNode currentNode, ConversationChoice selectedChoice) {
	conversationInProgress = false;
	screenUI.hideConversationDialog();
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
		screenUI.addTownLocation(entityID, x, y);

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
		    game.setScreen(ClassReflection.forName((String) param.get(0)));
		} catch (Exception e) {
		    throw new GdxRuntimeException("Invalid screen class for setScreen", e);
		}
		break;
	    default:
		break;
	}
    }

    @Override
    public void onSave(Json json, Preferences preferences) {
	final Array<TownEntityData> dataToStore = new Array<TownEntityData>();
	for (Entity entity : entityMap.values()) {
	    dataToStore.add(TownEntityData.newTownEntityData( // param
		    entity.getComponent(IDComponent.class).entityID, // entityID
		    entity.getComponent(ConversationComponent.class).currentConversationID, // conversationID
		    entity.getComponent(SizeComponent.class).boundingRectangle.x, // x
		    entity.getComponent(SizeComponent.class).boundingRectangle.y)); // y

	}
	preferences.putString("TownScreen-entityData", json.toJson(dataToStore));
	for (TownEntityData data : dataToStore) {
	    TownEntityData.removeTownEntityData(data);
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoad(Json json, Preferences preferences) {
	final Array<TownEntityData> dataToLoad;
	if (preferences.contains("TownScreen-entityData")) {
	    dataToLoad = json.fromJson(Array.class, preferences.getString("TownScreen-entityData"));
	} else {
	    // default town screen setup
	    dataToLoad = new Array<TownEntityData>();
	    dataToLoad.add(TownEntityData.newTownEntityData(EntityID.PLAYER, null, 0, 0));
	    dataToLoad.add(TownEntityData.newTownEntityData(EntityID.ELDER, ConversationID.ElderIntro, 537, 570));
	}

	entityMap.clear();
	screenUI.clearTownLocations();
	for (TownEntityData data : dataToLoad) {
	    final Entity entity = EntityEngine.getEngine().createEntity(data.entityID, data.position.x, data.position.y);
	    this.entityMap.put(data.entityID.ordinal(), entity);
	    if (!EntityID.PLAYER.equals(data.entityID)) {
		convCompMapper.get(entity).currentConversationID = data.conversationID;
		screenUI.addTownLocation(data.entityID, data.position.x, data.position.y);
	    }
	    TownEntityData.removeTownEntityData(data);
	}

	this.currentSelection = EntityID.ELDER;
	screenUI.selectLocation(currentSelection);
    }
}
