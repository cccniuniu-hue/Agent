package com.mindbridge.agent.service;

import com.mindbridge.agent.domain.AgentRunTrace;
import com.mindbridge.agent.domain.AgentRunTraceStep;
import com.mindbridge.agent.domain.ChatMessage;
import com.mindbridge.agent.domain.ChatSession;
import com.mindbridge.agent.domain.RiskLevel;
import com.mindbridge.agent.domain.UserAccount;
import com.mindbridge.agent.dto.AgentRunTraceResponse;
import com.mindbridge.agent.dto.AgentRunTraceSummaryResponse;
import com.mindbridge.agent.repository.AgentRunTraceRepository;
import com.mindbridge.agent.service.agent.AgentRunResult;
import com.mindbridge.agent.service.agent.AgentStep;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
/**
 * Agent run trace 的落库与管理员查询服务。
 */
public class AgentRunTraceService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final AgentRunTraceRepository agentRunTraceRepository;

    public AgentRunTraceService(AgentRunTraceRepository agentRunTraceRepository) {
        this.agentRunTraceRepository = agentRunTraceRepository;
    }

    @Transactional
    public AgentRunTrace saveRun(
            UserAccount user,
            ChatSession session,
            ChatMessage triggerMessage,
            String input,
            AgentRunResult agentRun,
            Instant startedAt,
            Instant completedAt
    ) {
        AgentRunTrace trace = new AgentRunTrace();
        trace.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        trace.setUser(user);
        trace.setSession(session);
        trace.setTriggerMessage(triggerMessage);
        trace.setInput(input);
        trace.setIntent(agentRun.intent());
        trace.setRiskLevel(agentRun.riskLevel() == null ? RiskLevel.LOW : agentRun.riskLevel());
        trace.setMemoryBrief(agentRun.memoryBrief());
        trace.setKnowledgeQuery(agentRun.knowledgeQuery());
        trace.setResponsePlan(agentRun.responsePlan());
        trace.setResponseAgent(agentRun.responseAgent());
        trace.setStepCount(agentRun.steps().size());
        trace.setStartedAt(startedAt);
        trace.setCompletedAt(completedAt);
        agentRun.steps().forEach(step -> trace.addStep(toEntity(step)));
        return agentRunTraceRepository.save(trace);
    }

    @Transactional(readOnly = true)
    public List<AgentRunTraceSummaryResponse> latestTraces() {
        return agentRunTraceRepository.findTop100ByOrderByStartedAtDesc().stream()
                .filter(trace -> isStudentUser(trace.getUser()))
                .map(AgentRunTraceSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AgentRunTraceResponse> tracesForSession(String sessionId) {
        return agentRunTraceRepository.findBySession_PublicIdOrderByStartedAtAsc(sessionId).stream()
                .filter(trace -> isStudentUser(trace.getUser()))
                .map(AgentRunTraceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AgentRunTraceResponse trace(String traceId) {
        AgentRunTrace trace = agentRunTraceRepository.findByTraceId(traceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Run trace not found"));
        if (!isStudentUser(trace.getUser())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Run trace not found");
        }
        return AgentRunTraceResponse.from(trace);
    }

    private static AgentRunTraceStep toEntity(AgentStep step) {
        AgentRunTraceStep entity = new AgentRunTraceStep();
        entity.setStepNumber(step.step());
        entity.setAgent(step.agent());
        entity.setAction(step.action());
        entity.setObservation(step.observation() == null ? "" : step.observation());
        entity.setCreatedAt(step.createdAt());
        return entity;
    }

    private static boolean isStudentUser(UserAccount user) {
        return user != null && !user.getRoles().contains(ROLE_ADMIN);
    }
}
