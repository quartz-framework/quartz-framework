package xyz.quartzframework.beans.condition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConditionType {
    CONDITIONAL(BeanEvaluationMomentType.POST_REGISTRATION),
    ON_CLASS(BeanEvaluationMomentType.PRE_REGISTRATION),
    ON_MISSING_CLASS(BeanEvaluationMomentType.PRE_REGISTRATION),
    ON_PROPERTY(BeanEvaluationMomentType.PRE_REGISTRATION),
    ON_ENVIRONMENT(BeanEvaluationMomentType.PRE_REGISTRATION),
    ON_BEAN(BeanEvaluationMomentType.POST_REGISTRATION),
    ON_MISSING_BEAN(BeanEvaluationMomentType.POST_REGISTRATION),
    ON_ANNOTATION(BeanEvaluationMomentType.POST_REGISTRATION);

    private final BeanEvaluationMomentType evaluationMomentType;

}