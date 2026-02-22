package com.karandev.aether.mapper;

import com.karandev.aether.dto.chat.ChatResponse;
import com.karandev.aether.entity.ChatMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    List<ChatResponse> fromListOfChatMessage(List<ChatMessage> chatMessageList);
}
