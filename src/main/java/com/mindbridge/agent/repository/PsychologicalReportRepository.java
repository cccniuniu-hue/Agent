package com.mindbridge.agent.repository;

import com.mindbridge.agent.domain.PsychologicalReport;
import com.mindbridge.agent.domain.ToolStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 心理报告的数据访问接口。
 */
public interface PsychologicalReportRepository extends JpaRepository<PsychologicalReport, Long> {

    /** 当前用户自己的报告查询，主要保留给接口扩展。 */
    @EntityGraph(attributePaths = {"user", "session"})
    List<PsychologicalReport> findTop50ByUser_IdOrderByCreatedAtDesc(Long userId);

    /** 管理员后台报告列表。 */
    @EntityGraph(attributePaths = {"user", "session"})
    List<PsychologicalReport> findTop100ByOrderByCreatedAtDesc();

    /** Excel 写入记录页面只需要写入成功的报告。 */
    @EntityGraph(attributePaths = {"user", "session"})
    List<PsychologicalReport> findTop100ByExcelStatusOrderByCreatedAtDesc(ToolStatus excelStatus);
}
