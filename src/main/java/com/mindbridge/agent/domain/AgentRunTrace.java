package com.mindbridge.agent.domain;

import com.mindbridge.agent.service.agent.AgentName;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "agent_run_traces",
        indexes = {
                @Index(name = "idx_agent_run_trace_session", columnList = "session_id"),
                @Index(name = "idx_agent_run_trace_user", columnList = "user_id"),
                @Index(name = "idx_agent_run_trace_started_at", columnList = "started_at")
        }
)
/**
 * 一轮学生输入触发的 Agent loop 运行轨迹。
 *
 * <p>概要字段便于管理员快速定位，steps 记录每个专家 Agent 的动作与观察结果。</p>
 */
public class AgentRunTrace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String traceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private ChatSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trigger_message_id")
    private ChatMessage triggerMessage;

    @Lob
    @Column(nullable = false)
    private String input;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private IntentType intent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Lob
    private String memoryBrief;

    @Column(length = 500)
    private String knowledgeQuery;

    @Lob
    private String responsePlan;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private AgentName responseAgent;

    @Column(nullable = false)
    private int stepCount;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(nullable = false)
    private Instant completedAt;

    @OneToMany(mappedBy = "trace", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    private List<AgentRunTraceStep> steps = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public ChatSession getSession() {
        return session;
    }

    public void setSession(ChatSession session) {
        this.session = session;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public ChatMessage getTriggerMessage() {
        return triggerMessage;
    }

    public void setTriggerMessage(ChatMessage triggerMessage) {
        this.triggerMessage = triggerMessage;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public IntentType getIntent() {
        return intent;
    }

    public void setIntent(IntentType intent) {
        this.intent = intent;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getMemoryBrief() {
        return memoryBrief;
    }

    public void setMemoryBrief(String memoryBrief) {
        this.memoryBrief = memoryBrief;
    }

    public String getKnowledgeQuery() {
        return knowledgeQuery;
    }

    public void setKnowledgeQuery(String knowledgeQuery) {
        this.knowledgeQuery = knowledgeQuery;
    }

    public String getResponsePlan() {
        return responsePlan;
    }

    public void setResponsePlan(String responsePlan) {
        this.responsePlan = responsePlan;
    }

    public AgentName getResponseAgent() {
        return responseAgent;
    }

    public void setResponseAgent(AgentName responseAgent) {
        this.responseAgent = responseAgent;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public List<AgentRunTraceStep> getSteps() {
        return List.copyOf(steps);
    }

    public void addStep(AgentRunTraceStep step) {
        steps.add(step);
        step.setTrace(this);
    }
}
