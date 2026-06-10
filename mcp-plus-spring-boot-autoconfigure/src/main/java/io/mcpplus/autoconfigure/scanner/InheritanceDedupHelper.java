package io.mcpplus.autoconfigure.scanner;

import org.springframework.aop.support.AopUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

final class InheritanceDedupHelper {

    private InheritanceDedupHelper() {
    }

    static boolean isShadowedBySubclassBean(Method method,
            Class<?> beanClass,
            Collection<Object> candidateBeans,
            InheritanceDedupStrategy strategy) {
        if (strategy != InheritanceDedupStrategy.SUBCLASS_WINS) {
            return false;
        }
        MethodSignature signature = MethodSignature.of(method);
        for (Object candidateBean : candidateBeans) {
            Class<?> candidateClass = ClassUtils.getUserClass(AopUtils.getTargetClass(candidateBean));
            if (candidateClass == beanClass || !beanClass.isAssignableFrom(candidateClass)) {
                continue;
            }
            for (Method candidateMethod : McpMethodIntrospector.getCandidateMethods(candidateClass,
                    InheritanceDedupStrategy.MOST_SPECIFIC)) {
                if (MethodSignature.of(candidateMethod).equals(signature)) {
                    return true;
                }
            }
        }
        return false;
    }

    static Set<MethodSignature> signaturesForBean(Class<?> beanClass, InheritanceDedupStrategy strategy) {
        Set<MethodSignature> signatures = new HashSet<>();
        for (Method method : McpMethodIntrospector.getCandidateMethods(beanClass, strategy)) {
            signatures.add(MethodSignature.of(method));
        }
        return signatures;
    }
}
