package com.lok.game.ecs.components;

import com.lok.game.conversation.Conversation.ConversationID;

public class ConversationComponent implements Component<ConversationComponent> {
    public ConversationID currentConversationID	= null;
    public String	  conversationImage	= null;

    @Override
    public void reset() {
	this.currentConversationID = null;
	this.conversationImage = null;
    }

    @Override
    public void initialize(ConversationComponent configComponent) {
	this.currentConversationID = configComponent.currentConversationID;
	this.conversationImage = configComponent.conversationImage;
    }

}
