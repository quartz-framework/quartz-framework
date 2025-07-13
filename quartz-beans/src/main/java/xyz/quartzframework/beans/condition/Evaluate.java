package xyz.quartzframework.beans.condition;

import xyz.quartzframework.beans.condition.metadata.*;

import java.util.Map;

public interface Evaluate {

    static Map<ConditionType, ConditionEvaluator> getEvaluators() {
        return Evaluators.getEvaluators();
    }

    GenericConditionMetadata getGenericConditionMetadata();

    PropertyConditionMetadata getPropertyConditionMetadata();

    BeanConditionMetadata getBeanConditionMetadata();

    BeanConditionMetadata getMissingBeanConditionMetadata();

    ClassConditionMetadata getClassConditionMetadata();

    ClassConditionMetadata getMissingClassConditionMetadata();

    AnnotationConditionMetadata getAnnotationConditionMetadata();

}