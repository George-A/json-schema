package org.gasoft.json_schema;

/**
 * Interface for customize contentEncoding, contentType keywords behavior
 */
public interface IContentProcessing {

    enum ContentValidationLevel{
        /**
         * Disable any contentXXX keyword validations
         */
        DISABLE,

        /**
         * Default behavior as in specification. In Drafts 2019,2020 no any validations,
         * in Draft7 validate contentEncoding, and jsons contentMediaType
         */
        DEFAULT,

        /**
         * Validate contentEncoding keywords, if encoding is supported then check contentEncoding, and if contentMediaType
         * defined and pointed to any json format then json validated.
         * Draft7 - no affecting
         * Draft 2019+ - validate contentEncoding and validate instance value json format if contentMediaType pointed to json
         */
        ENCODING,

        /**
         * VALIDATE_ENCODING + trying validating contentSchema
         */
        ENCODING_AND_SCHEMA
    }
}
