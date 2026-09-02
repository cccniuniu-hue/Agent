package com.mindbridge.agent.repository;

import com.mindbridge.agent.domain.AgentRunTrace;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Agent run trace 数据访问接口。
 */
public interface AgentRunTraceRepository extends JpaRepository<AgentRunTrace, Long> {

    /** 管理员后台最新运行轨迹列表。 */
    @EntityGraph(attributePaths = {"user", "session", "triggerMessage"})
    List<AgentRunTrace> findTop100ByOrderByStartedAtDesc();

    /** 管理员查看完整会话时，附带读取该会话下每轮输入的 agent trace。 */
    @EntityGraph(attributePaths = {"user", "session", "triggerMessage", "steps"})
    List<AgentRunTrace> findBySession_PublicIdOrderByStartedAtAsc(String publicId);

    /** 管理员打开单条运行轨迹详情。 */
    @EntityGraph(attributePaths = {"user", "session", "triggerMessage", "steps"})
    Optional<AgentRunTrace> findByTraceId(String traceId);
}
