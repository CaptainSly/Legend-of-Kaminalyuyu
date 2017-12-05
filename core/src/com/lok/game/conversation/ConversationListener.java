package com.lok.game.conversation;

public interface ConversationListener {
    public void onStartConversation(ConversationNode startNode);

    public void onTriggerConversationChoice(ConversationChoice choice, ConversationNode nextNode);

    public void onEndConversation(ConversationChoice choice, ConversationNode currentNode);
}
