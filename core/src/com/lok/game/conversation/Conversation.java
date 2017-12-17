package com.lok.game.conversation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.lok.game.Utils;
import com.lok.game.conversation.ConversationChoice.ConversationAction;
import com.lok.game.conversation.ConversationChoice.ConversationActionID;

public class Conversation {
    public static enum ConversationID {
	ElderIntro("conversations/elder_intro.json"),
	ElderGame("conversations/elder_game.json"),
	BlacksmithIntro("conversations/blacksmith_intro.json"),
	Portal("conversations/portal.json"),
	ShamanIntro("conversations/shaman_intro.json");

	private final String filePath;

	private ConversationID(String filePath) {
	    this.filePath = filePath;
	}

	public String getFilePath() {
	    return filePath;
	}
    }

    private final static String			       TAG		 = Conversation.class.getSimpleName();
    private static Array<Conversation>		       conversationCache = null;

    private final ConversationID		       conversationID;
    private final ConversationNode		       startNode;
    private ConversationNode			       currentNode;
    private final ObjectMap<Integer, ConversationNode> nodes;
    private final Array<ConversationListener>	       listeners;

    private Conversation(ConversationID conversationID, ConversationNode startNode) {
	if (conversationID == null || startNode == null) {
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
	    listener.onStartConversation(this, startNode);
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

	if (choice.getActions() != null) {
	    boolean endConversation = false;
	    for (ConversationAction action : choice.getActions()) {
		if (action.getActionID().equals(ConversationActionID.EndConversation)) {
		    endConversation = true;
		    continue;
		}

		for (ConversationListener listener : listeners) {
		    listener.onConversationAction(this, currentNode, choice, action);
		}
	    }

	    if (endConversation) {
		for (ConversationListener listener : listeners) {
		    listener.onEndConversation(this, currentNode, choice);
		}
		return;
	    }
	}

	Gdx.app.debug(TAG, "Trigger conversation choice of conversation " + conversationID + " to node " + targetNodeID);
	final ConversationNode nextNode = nodes.get(targetNodeID);
	for (ConversationListener listener : listeners) {
	    listener.onConversationChoiceSelected(this, currentNode, nextNode, choice);
	}
	currentNode = nextNode;
    }

    public static Conversation load(ConversationID conversationID) {
	if (conversationCache == null) {
	    // initialize cache
	    final int max = ConversationID.values().length;
	    conversationCache = new Array<Conversation>(max);
	    for (int i = max; i >= 0; --i) {
		conversationCache.add(null);
	    }
	}

	if (conversationCache.get(conversationID.ordinal()) != null) {
	    return conversationCache.get(conversationID.ordinal());
	}

	final Object fromJson = Utils.fromJson(Gdx.files.internal(conversationID.filePath));

	if (fromJson instanceof Array<?>) {
	    // multiple nodes defined for the conversation
	    final Array<?> jsonArray = (Array<?>) fromJson;
	    final Conversation conversation = new Conversation(conversationID, Utils.readJsonValue(ConversationNode.class, (JsonValue) jsonArray.get(0)));
	    for (int i = 1; i < jsonArray.size; ++i) {
		conversation.addNode(Utils.readJsonValue(ConversationNode.class, (JsonValue) jsonArray.get(i)));
	    }

	    conversationCache.set(conversationID.ordinal(), conversation);
	    return conversation;
	} else {
	    // only one node defined for the conversation
	    final Conversation conversation = new Conversation(conversationID, Utils.readJsonValue(ConversationNode.class, (JsonValue) fromJson));
	    conversationCache.set(conversationID.ordinal(), conversation);
	    return conversation;
	}
    }
}
