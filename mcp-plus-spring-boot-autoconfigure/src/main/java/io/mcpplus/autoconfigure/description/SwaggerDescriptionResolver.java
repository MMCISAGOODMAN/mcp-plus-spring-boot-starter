package io.mcpplus.autoconfigure.description;

import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class SwaggerDescriptionResolver implements DescriptionResolver {

    private static final boolean OPENAPI_AVAILABLE = isClassPresent(
            "io.swagger.v3.oas.annotations.Operation");
    private static final boolean SWAGGER2_AVAILABLE = isClassPresent(
            "io.swagger.annotations.ApiOperation");

    @Override
    public boolean isAvailable() {
        return OPENAPI_AVAILABLE || SWAGGER2_AVAILABLE;
    }

    @Override
    public ToolDescription resolve(Method method, Class<?> beanClass) {
        String toolDescription = resolveOpenApiDescription(method);
        if (toolDescription.isBlank()) {
            toolDescription = resolveSwagger2Description(method);
        }

        Map<String, String> parameterDescriptions = new HashMap<>();
        for (Parameter parameter : method.getParameters()) {
            String paramDescription = resolveOpenApiParameterDescription(parameter);
            if (paramDescription.isBlank()) {
                paramDescription = resolveSwagger2ParameterDescription(parameter);
            }
            if (!paramDescription.isBlank()) {
                parameterDescriptions.put(parameter.getName(), paramDescription);
            }
        }

        return new ToolDescription(toolDescription, "", parameterDescriptions);
    }

    private static String resolveOpenApiDescription(Method method) {
        if (!OPENAPI_AVAILABLE) {
            return "";
        }
        try {
            Class<? extends Annotation> operationClass = loadAnnotationClass(
                    "io.swagger.v3.oas.annotations.Operation");
            Object operation = AnnotatedElementUtils.findMergedAnnotation(method, operationClass);
            if (operation == null) {
                return "";
            }
            String summary = (String) invoke(operation, "summary");
            String description = (String) invoke(operation, "description");
            return firstNonBlank(summary, description);
        }
        catch (Exception ex) {
            return "";
        }
    }

    private static String resolveSwagger2Description(Method method) {
        if (!SWAGGER2_AVAILABLE) {
            return "";
        }
        try {
            Class<? extends Annotation> apiOperationClass = loadAnnotationClass(
                    "io.swagger.annotations.ApiOperation");
            Object apiOperation = AnnotatedElementUtils.findMergedAnnotation(method, apiOperationClass);
            if (apiOperation == null) {
                return "";
            }
            String value = (String) invoke(apiOperation, "value");
            String notes = (String) invoke(apiOperation, "notes");
            return firstNonBlank(value, notes);
        }
        catch (Exception ex) {
            return "";
        }
    }

    private static String resolveOpenApiParameterDescription(Parameter parameter) {
        if (!OPENAPI_AVAILABLE) {
            return "";
        }
        try {
            Class<? extends Annotation> parameterClass = loadAnnotationClass(
                    "io.swagger.v3.oas.annotations.Parameter");
            Object paramAnnotation = AnnotatedElementUtils.findMergedAnnotation(parameter, parameterClass);
            if (paramAnnotation == null) {
                return "";
            }
            String description = (String) invoke(paramAnnotation, "description");
            return description != null ? description : "";
        }
        catch (Exception ex) {
            return "";
        }
    }

    private static String resolveSwagger2ParameterDescription(Parameter parameter) {
        if (!SWAGGER2_AVAILABLE) {
            return "";
        }
        try {
            Class<? extends Annotation> apiParamClass = loadAnnotationClass("io.swagger.annotations.ApiParam");
            Object paramAnnotation = AnnotatedElementUtils.findMergedAnnotation(parameter, apiParamClass);
            if (paramAnnotation == null) {
                return "";
            }
            String value = (String) invoke(paramAnnotation, "value");
            return value != null ? value : "";
        }
        catch (Exception ex) {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Annotation> loadAnnotationClass(String className) throws ClassNotFoundException {
        return (Class<? extends Annotation>) Class.forName(className);
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, SwaggerDescriptionResolver.class.getClassLoader());
            return true;
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }

    private static Object invoke(Object target, String methodName) throws ReflectiveOperationException {
        return target.getClass().getMethod(methodName).invoke(target);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
