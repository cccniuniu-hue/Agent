package com.mindbridge.agent.service.knowledge.eval;

import com.mindbridge.agent.config.MindBridgeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RagEvaluationRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(RagEvaluationRunner.class);

    private final MindBridgeProperties properties;
    private final RagEvaluationService evaluationService;
    private final ApplicationContext applicationContext;

    public RagEvaluationRunner(
            MindBridgeProperties properties,
            RagEvaluationService evaluationService,
            ApplicationContext applicationContext
    ) {
        this.properties = properties;
        this.evaluationService = evaluationService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        MindBridgeProperties.RagEval ragEval = properties.getRagEval();
        if (!ragEval.isEnabled()) {
            return;
        }
        RagEvalReport report = evaluationService.evaluate(ragEval.getDataset(), ragEval.getTopK());
        evaluationService.writeReport(report, ragEval.getOutputPath());
        logger.info("\n{}", evaluationService.formatSummary(report));
        if (ragEval.isExitAfterRun()) {
            System.exit(SpringApplication.exit(applicationContext, () -> 0));
        }
    }
}
