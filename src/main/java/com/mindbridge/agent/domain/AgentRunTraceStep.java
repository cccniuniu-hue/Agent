package com.mindbridge.agent.domain;

import com.mindbridge.agent.service.agent.AgentAction;
import com.mindbridge.agent.service.agent.AgentName;
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
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(
        name = "agent_run_trace_steps",
        indexes = @Index(name = "idx_agent_run_trace_step_trace", columnList = "trace_id, step_number")
)
/**
 * Agent loop 中的单步运行记录。
 */
public class AgentRunTraceStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trace_id")
    private AgentRunTrace trace;

    @Column(name = "step_number", nullable = false)
    private int stepNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AgentName agent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AgentAction action;

    @Lob
    @Column(nullable = false)
    private String observation;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public AgentRunTrace getTrace() {
        return trace;
    }

    public void setTrace(AgentRunTrace trace) {
        this.trace = trace;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public AgentName getAgent() {
        return agent;
    }

    public void setAgent(AgentName agent) {
        this.agent = agent;
    }

    public AgentAction getAction() {
        return action;
    }

    public void setAction(AgentAction action) {
        this.action = action;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
