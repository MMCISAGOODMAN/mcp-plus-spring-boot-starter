package io.mcpplus.autoconfigure.scanner;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class McpMethodIntrospectorTest {

    @Test
    void shouldIncludeInheritedMethodsWithMostSpecificStrategy() {
        List<String> methodNames = McpMethodIntrospector
                .getCandidateMethods(ChildService.class, InheritanceDedupStrategy.MOST_SPECIFIC)
                .stream()
                .map(Method::getName)
                .collect(Collectors.toList());

        assertThat(methodNames).contains("baseMethod", "childMethod");
    }

    @Test
    void shouldKeepSubclassOverrideWithMostSpecificStrategy() throws Exception {
        Method baseLookup = BaseService.class.getMethod("lookup", String.class);
        Method childLookup = ChildService.class.getMethod("lookup", String.class);

        List<Method> methods = McpMethodIntrospector.getCandidateMethods(ChildService.class,
                InheritanceDedupStrategy.MOST_SPECIFIC);
        Method selected = methods.stream()
                .filter(method -> "lookup".equals(method.getName()))
                .findFirst()
                .orElseThrow();

        assertThat(selected.getDeclaringClass()).isEqualTo(childLookup.getDeclaringClass());
        assertThat(selected.getDeclaringClass()).isNotEqualTo(baseLookup.getDeclaringClass());
    }

    @Service
    static class BaseService {
        public String baseMethod() {
            return "base";
        }

        public String lookup(String id) {
            return "base:" + id;
        }
    }

    @Service
    static class ChildService extends BaseService {
        public String childMethod() {
            return "child";
        }

        @Override
        public String lookup(String id) {
            return "child:" + id;
        }
    }
}
