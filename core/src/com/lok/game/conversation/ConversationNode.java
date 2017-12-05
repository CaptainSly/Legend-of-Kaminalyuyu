package com.lok.game.conversation;

import com.badlogic.gdx.utils.Array;
import com.lok.game.ecs.EntityEngine.EntityID;

public class ConversationNode {
    private int			      nodeID;
    private EntityID		      entityID;
    private String		      textID;
    private Array<ConversationChoice> choices;

    public int getNodeID() {
	return nodeID;
    }

    public EntityID getEntityID() {
	return entityID;
    }

    public String getTextID() {
	return textID;
    }

    public Array<ConversationChoice> getChoices() {
	return choices;
    }
}
