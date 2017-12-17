package com.lok.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.lok.game.conversation.Conversation.ConversationID;

public class ConversationComponent implements Component, Poolable {
    public ConversationID currentConversationID	= null;
    public String	  conversationImage	= null;

    @Override
    public void reset() {
	this.currentConversationID = null;
	this.conversationImage = null;
    }

}
