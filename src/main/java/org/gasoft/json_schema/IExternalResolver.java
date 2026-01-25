package org.gasoft.json_schema;

import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A helper tool used to resolve references to other documents.<br/>
 *
 * Can be used if references are identifiers for some external schemes. For example,
 * the reference urn:1 may point to a scheme from a database with the identifier 1.
 */
public interface IExternalResolver {

    /**
     * Trying external resolution of referenced by {@code foundId} schema.
     * <br/>
     * If null is returned, then the reference will be resolved according to the specification.
     *
     * @param foundId id from $ref keyword
     * @param schemaLocator the nullable context of the schema in which the reference was found
     * @return resolution result or null.
     */
    @Nullable IExternalResolutionResult resolve(@NonNull String foundId, @NonNull ISchemaLocator schemaLocator);
}
