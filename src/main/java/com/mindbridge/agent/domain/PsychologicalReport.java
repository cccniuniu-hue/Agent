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
@Table(name = "psychological_reports")
/**
 * 后台心理状态报告。
 *
 * <p>报告记录的是后台评估和工具执行状态，不会作为学生端展示内容。</p>
 */
public class PsychologicalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private ChatSession session;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IntentType intent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmotionLabel emotion;

    @Column(nullable = false)
    private double emotionScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Column(nullable = false)
    private double confidence;

    @Column(length = 500)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ToolStatus excelStatus = ToolStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ToolStatus emailStatus = ToolStatus.SKIPPED;

    @Column(length = 500)
    private String toolError;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public ChatSession getSession() {
        return session;
    }

    public void setSession(ChatSession session) {
        this.session = session;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public IntentType getIntent() {
        return intent;
    }

    public void setIntent(IntentType intent) {
        this.intent = intent;
    }

    public EmotionLabel getEmotion() {
        return emotion;
    }

    public void setEmotion(EmotionLabel emotion) {
        this.emotion = emotion;
    }

    public double getEmotionScore() {
        return emotionScore;
    }

    public void setEmotionScore(double emotionScore) {
        this.emotionScore = emotionScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public ToolStatus getExcelStatus() {
        return excelStatus;
    }

    public void setExcelStatus(ToolStatus excelStatus) {
        this.excelStatus = excelStatus;
    }

    public ToolStatus getEmailStatus() {
        return emailStatus;
    }

    public void setEmailStatus(ToolStatus emailStatus) {
        this.emailStatus = emailStatus;
    }

    public String getToolError() {
        return toolError;
    }

    public void setToolError(String toolError) {
        this.toolError = toolError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
