package com.lok.game.conversation;

import com.badlogic.gdx.utils.Array;

public class ConversationChoice {
    public static enum ConversationActionID {
	EndConversation,
	SetScreen,
	ActivateTownLocation,
	SetConversation
    }

    public static class ConversationAction {
	private ConversationActionID actionID;
	private Object		     param;

	public ConversationActionID getActionID() {
	    return actionID;
	}

	public Object getParam() {
	    return param;
	}
    }

    private String		      textID;
    private int			      targetNodeID;
    private Array<ConversationAction> actions;

    public String getTextID() {
	return textID;
    }

    public int getTargetNodeID() {
	return targetNodeID;
    }

    public Array<ConversationAction> getActions() {
	return actions;
    }
}
