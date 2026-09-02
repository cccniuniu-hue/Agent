package com.mindbridge.agent.repository;

import com.mindbridge.agent.domain.ChatSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 会话的数据访问接口。
 */
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /** 学生继续对话时，必须校验会话属于当前用户。 */
    Optional<ChatSession> findByPublicIdAndUser_Id(String publicId, Long userId);

    /** 管理员查看会话详情时需要连同用户信息一起加载。 */
    @EntityGraph(attributePaths = "user")
    Optional<ChatSession> findByPublicId(String publicId);
}
