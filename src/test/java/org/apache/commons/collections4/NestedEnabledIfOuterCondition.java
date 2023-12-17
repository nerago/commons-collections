package org.apache.commons.collections4;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.platform.commons.support.ReflectionSupport;

public class NestedEnabledIfOuterCondition implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext extensionContext) {
        final Class<?> annotatedType = extensionContext.getRequiredTestClass();
        final NestedEnabledIfOuter annotation = annotatedType.getAnnotation(NestedEnabledIfOuter.class);
        final String methodName = annotation.value();

        if (extensionContext.getTestInstances().isPresent()) {
            final Pair<Method, Object> methodAndInstance = findConditionMethod(extensionContext.getTestInstances().get(), annotatedType, methodName);

            if (methodAndInstance == null) {
                throw new ExtensionConfigurationException("@NestedEnabledIfOuter target method '" + methodName + "' not found in enclosing objects");
            }

            final Object result = ReflectionSupport.invokeMethod(methodAndInstance.getLeft(), methodAndInstance.getRight());
            if (!(result instanceof Boolean)) {
                throw new ExtensionConfigurationException("@NestedEnabledIfOuter target method must return boolean");
            }

            if ((boolean) result) {
                return ConditionEvaluationResult.enabled(null);
            } else {
                return ConditionEvaluationResult.disabled("disabled according to " + methodName);
            }
        } else {
            return ConditionEvaluationResult.enabled("allow until we have instance of a runtime type to check");
        }
    }

    private static Pair<Method, Object> findConditionMethod(final TestInstances testInstances, final Class<?> annotatedType, final String methodName) {
        final List<Object> enclosingInstanceList = testInstances.getEnclosingInstances();
        if (enclosingInstanceList.isEmpty()) {
            throw new ExtensionConfigurationException("@NestedEnabledIfOuter on " + annotatedType.getName()
                    + " but no enclosing instance found");
        }

        Pair<Method, Object> found = null;
        for (final Object instance : enclosingInstanceList) {
            final Class<?> type = instance.getClass();
            final Optional<Method> method = ReflectionSupport.findMethod(type, methodName);
            if (method.isPresent()) {
                found = Pair.of(method.get(), instance);
            }
        }
        return found;
    }
}
