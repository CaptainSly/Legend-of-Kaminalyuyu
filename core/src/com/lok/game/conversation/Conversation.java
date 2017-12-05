package com.lok.game.conversation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.lok.game.Utils;
import com.lok.game.conversation.ConversationChoice.ConversationActionID;

public class Conversation {
    private final String			       TAG = Conversation.class.getSimpleName();

    private final String			       conversationID;
    private final ConversationNode		       startNode;
    private ConversationNode			       currentNode;
    private final ObjectMap<Integer, ConversationNode> nodes;
    private final Array<ConversationListener>	       listeners;

    private Conversation(String conversationID, ConversationNode startNode) {
	if (conversationID == null || conversationID.trim().isEmpty() || startNode == null) {
	    throw new IllegalStateException("Conversation must have an ID " + conversationID + " and a startnode " + startNode);
	}

	this.conversationID = conversationID;
	this.startNode = startNode;
	this.currentNode = startNode;
	this.nodes = new ObjectMap<Integer, ConversationNode>();
	this.nodes.put(startNode.getNodeID(), startNode);
	this.listeners = new Array<ConversationListener>();
    }

    public void addConversationListener(ConversationListener listener) {
	this.listeners.add(listener);
    }

    public void removeConversationListener(ConversationListener listener) {
	this.listeners.removeValue(listener, false);
    }

    public void addNode(ConversationNode node) {
	if (nodes.containsKey(node.getNodeID())) {
	    throw new GdxRuntimeException("Conversation " + conversationID + " already contains a node of ID " + node.getNodeID());
	}

	nodes.put(node.getNodeID(), node);
    }

    public void startConversation() {
	Gdx.app.debug(TAG, "Starting conversation " + conversationID);
	currentNode = startNode;
	for (ConversationListener listener : listeners) {
	    listener.onStartConversation(startNode);
	}
    }

    public void triggerConversationChoice(int choiceIndex) {
	if (choiceIndex < 0 || choiceIndex >= currentNode.getChoices().size) {
	    throw new GdxRuntimeException("Invalid choice index " + choiceIndex + " for conversation node " + currentNode.getNodeID() + " of conversation " + conversationID);
	}

	final ConversationChoice choice = currentNode.getChoices().get(choiceIndex);
	final int targetNodeID = choice.getTargetNodeID();
	if (!nodes.containsKey(targetNodeID)) {
	    throw new GdxRuntimeException("Choice " + choiceIndex + " of conversation node " + currentNode.getNodeID() + " of conversation " + conversationID
		    + " links to an invalid node " + targetNodeID);
	}

	if (choice.getAction() != null) {
	    if (choice.getAction().getActionID().equals(ConversationActionID.EndConversation)) {
		for (ConversationListener listener : listeners) {
		    listener.onEndConversation(choice, currentNode);
		}
		return;
	    }
	}

	Gdx.app.debug(TAG, "Trigger conversation choice of conversation " + conversationID + " to node " + targetNodeID);
	currentNode = nodes.get(targetNodeID);
	for (ConversationListener listener : listeners) {
	    listener.onTriggerConversationChoice(choice, currentNode);
	}
    }

    public static Conversation load(String conversationPath) {
	final Object fromJson = Utils.fromJson(Gdx.files.internal(conversationPath));

	if (fromJson instanceof Array<?>) {
	    // multiple animations defined within file
	    final Array<?> jsonArray = (Array<?>) fromJson;
	    final Conversation conversation = new Conversation(conversationPath, Utils.readJsonValue(ConversationNode.class, (JsonValue) jsonArray.get(0)));
	    for (int i = 1; i < jsonArray.size; ++i) {
		conversation.addNode(Utils.readJsonValue(ConversationNode.class, (JsonValue) jsonArray.get(i)));
	    }
	    return conversation;
	} else {
	    // only one animation defined -> load it
	    return new Conversation(conversationPath, Utils.readJsonValue(ConversationNode.class, (JsonValue) fromJson));
	}
    }
}
