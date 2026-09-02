package com.mindbridge.agent.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_memory_items")
/**
 * 用户长期画像记忆。
 *
 * <p>保存从对话中抽取出的稳定偏好、沟通方式、支持需求和可复用背景，
 * 原始完整对话仍由 ChatMessage 作为权威记录。</p>
 */
public class UserMemoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_session_id")
    private ChatSession sourceSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private UserMemoryType type = UserMemoryType.PREFERENCE;

    @Lob
    @Column(nullable = false)
    private String summary;

    @Lob
    private String evidence;

    @Column(nullable = false)
    private double confidence = 0.7;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(nullable = false)
    private Instant lastSeenAt = Instant.now();

    public Long getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public ChatSession getSourceSession() {
        return sourceSession;
    }

    public void setSourceSession(ChatSession sourceSession) {
        this.sourceSession = sourceSession;
    }

    public UserMemoryType getType() {
        return type;
    }

    public void setType(UserMemoryType type) {
        this.type = type;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void refreshSeen(ChatSession session, String evidence, double confidence) {
        this.sourceSession = session;
        this.evidence = evidence;
        this.confidence = Math.max(this.confidence, confidence);
        this.updatedAt = Instant.now();
        this.lastSeenAt = Instant.now();
    }
}
