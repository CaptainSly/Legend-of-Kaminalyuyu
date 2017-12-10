package com.lok.game.conversation;

import com.lok.game.conversation.ConversationChoice.ConversationAction;

public interface ConversationListener {
    public void onStartConversation(Conversation conversation, ConversationNode startNode);

    public void onEndConversation(Conversation conversation, ConversationNode currentNode, ConversationChoice selectedChoice);

    public void onConversationChoiceSelected(Conversation conversation, ConversationNode currentNode, ConversationNode nextNode, ConversationChoice selectedChoice);

    public void onConversationAction(Conversation conversation, ConversationNode currentNode, ConversationChoice selectedChoice, ConversationAction action);
}
