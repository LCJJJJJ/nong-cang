package com.nongcang.server.modules.assistant.service;

import java.util.List;

public record AssistantMessageMetadata(
		List<AssistantResultBlock> resultBlocks,
		AssistantActionCard actionCard) {
}
