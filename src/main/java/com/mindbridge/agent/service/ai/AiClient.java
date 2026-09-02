package com.mindbridge.agent.service.ai;

import java.util.List;
import reactor.core.publisher.Flux;

/**
 * 模型调用统一接口。
 *
 * <p>业务层通过 complete 做分类/评估，通过 stream 做学生端流式回答。</p>
 */
public interface AiClient {

    String complete(List<AiMessage> messages);

    Flux<String> stream(List<AiMessage> messages);
}
