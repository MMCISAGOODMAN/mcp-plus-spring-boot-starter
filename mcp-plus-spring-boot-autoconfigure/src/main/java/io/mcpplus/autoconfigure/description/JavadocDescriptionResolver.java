package io.mcpplus.autoconfigure.description;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class JavadocDescriptionResolver implements DescriptionResolver {

    private static final boolean THERAPI_AVAILABLE = isClassPresent(
            "com.github.therapi.runtimejavadoc.RuntimeJavadoc");

    @Override
    public boolean isAvailable() {
        return THERAPI_AVAILABLE;
    }

    @Override
    public ToolDescription resolve(Method method, Class<?> beanClass) {
        if (!THERAPI_AVAILABLE) {
            return ToolDescription.empty();
        }
        try {
            Class<?> runtimeJavadocClass = Class.forName("com.github.therapi.runtimejavadoc.RuntimeJavadoc");
            Object classDoc = runtimeJavadocClass.getMethod("getJavadoc", Class.class).invoke(null, beanClass);
            if (classDoc == null) {
                return ToolDescription.empty();
            }

            Object methodDoc = classDoc.getClass().getMethod("getMethod", String.class, String.class)
                    .invoke(classDoc, method.getName(), buildParameterSignature(method));
            if (methodDoc == null) {
                return ToolDescription.empty();
            }

            String comment = (String) methodDoc.getClass().getMethod("getComment").invoke(methodDoc);
            String returnComment = (String) methodDoc.getClass().getMethod("getReturnsComment").invoke(methodDoc);

            Map<String, String> parameterDescriptions = new HashMap<>();
            for (Object paramDoc : (Iterable<?>) methodDoc.getClass().getMethod("getParams").invoke(methodDoc)) {
                String paramName = (String) paramDoc.getClass().getMethod("getName").invoke(paramDoc);
                String paramComment = (String) paramDoc.getClass().getMethod("getComment").invoke(paramDoc);
                if (paramComment != null && !paramComment.isBlank()) {
                    parameterDescriptions.put(paramName, paramComment);
                }
            }

            return new ToolDescription(
                    comment != null ? comment : "",
                    returnComment != null ? returnComment : "",
                    parameterDescriptions);
        }
        catch (Exception ex) {
            return ToolDescription.empty();
        }
    }

    private static String buildParameterSignature(Method method) {
        StringBuilder signature = new StringBuilder();
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                signature.append(',');
            }
            signature.append(parameterTypes[i].getName());
        }
        return signature.toString();
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, JavadocDescriptionResolver.class.getClassLoader());
            return true;
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
