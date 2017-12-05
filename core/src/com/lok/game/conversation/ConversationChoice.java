package com.lok.game.conversation;

public class ConversationChoice {
    public static enum ConversationActionID {
	EndConversation,
	SetScreen
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

    private String	       textID;
    private int		       targetNodeID;
    private ConversationAction action;

    public String getTextID() {
	return textID;
    }

    public int getTargetNodeID() {
	return targetNodeID;
    }

    public ConversationAction getAction() {
	return action;
    }
}
