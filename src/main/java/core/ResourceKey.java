package core;

import java.util.Objects;

/**
 * Resource-scoped key for tracking state changes.
 * Prevents parameter collision across different endpoints.
 *
 * Example:
 * POST:/cart/{id}:productId
 * POST:/profile/{id}:userId
 */
public class ResourceKey {

    private final String method;
    private final String normalizedPath;
    private final String paramName;

    public ResourceKey(String method, String normalizedPath, String paramName) {
        this.method = method;
        this.normalizedPath = normalizedPath;
        this.paramName = paramName;
    }

    public String method() {
        return method;
    }

    public String normalizedPath() {
        return normalizedPath;
    }

    public String paramName() {
        return paramName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceKey)) return false;
        ResourceKey that = (ResourceKey) o;
        return Objects.equals(method, that.method) &&
               Objects.equals(normalizedPath, that.normalizedPath) &&
               Objects.equals(paramName, that.paramName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, normalizedPath, paramName);
    }

    @Override
    public String toString() {
        return method + ":" + normalizedPath + ":" + paramName;
    }
}