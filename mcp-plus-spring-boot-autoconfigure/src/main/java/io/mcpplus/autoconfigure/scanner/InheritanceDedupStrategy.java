package io.mcpplus.autoconfigure.scanner;

/**
 * Strategy for deduplicating methods in Controller/Service inheritance hierarchies.
 */
public enum InheritanceDedupStrategy {

    /**
     * Only scan methods declared on the concrete bean class.
     */
    DECLARED_ONLY,

    /**
     * Include inherited methods from supertypes; signature conflicts keep the most specific declaring class.
     */
    MOST_SPECIFIC,

    /**
     * Like {@link #MOST_SPECIFIC}, and suppress superclass bean methods shadowed by a subclass bean
     * in the Spring context.
     */
    SUBCLASS_WINS
}
