package com.mindbridge.agent.repository;

import com.mindbridge.agent.domain.AlertRecord;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 预警发送记录的数据访问接口。
 */
public interface AlertRecordRepository extends JpaRepository<AlertRecord, Long> {

    List<AlertRecord> findByReport_Id(Long reportId);

    /**
     * 管理员后台列表需要同时展示报告、学生账号和会话 id，使用 EntityGraph 避免懒加载反复查询。
     */
    @EntityGraph(attributePaths = {"report", "report.user", "report.session"})
    List<AlertRecord> findTop100ByOrderByCreatedAtDesc();
}
