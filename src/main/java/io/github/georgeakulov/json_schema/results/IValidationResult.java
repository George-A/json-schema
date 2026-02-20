package io.github.georgeakulov.json_schema.results;

import com.fasterxml.jackson.core.JsonPointer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;
import java.util.stream.Stream;

public interface IValidationResult {

    enum Type {
        ANNOTATION,
        ERROR,
        OK,
        CONTAINER
    }

    default Stream<IValidationResult> asStream(){
        return Stream.of(this);
    }

    interface ISchemaLocator extends Comparable<ISchemaLocator>{

        @NonNull UUID getSchemaUUID();
        /**
         * Parent locator, or null for root sourceUri
         */
        @Nullable ISchemaLocator getParent();

        /**
         * Schema id or null if not exists
         */
        @Nullable URI getId();

        /**
         * @return real uri of schema loading. May equal id may be not
         */
        @Nullable URI getOriginUri();

        /**
         * Internal json pointer to subschema (if all then JsonPointer.empty())
         */
        JsonPointer getSchemaRef();

        ISchemaLocator appendProperty(String property);
        ISchemaLocator appendIndex(int idx);

        @Override
        default int compareTo(ISchemaLocator o) {
            var id = o.getId();
            var result = this.getId() == null ? (id == null ? 0 : -1)  : (id == null ? 1 : this.getId().compareTo(id));
            if(result == 0) {
                var uri = o.getOriginUri();
                result = this.getOriginUri() == null ? (uri == null ? 0 : -1) : (uri == null ? 1 : this.getOriginUri().compareTo(uri));
                if(result == 0) {
                    var thisRef = getSchemaRef() == null ? null : getSchemaRef().toString();
                    var ref = o.getSchemaRef() == null ? null : o.getSchemaRef().toString();
                    return thisRef == null ? (ref == null ? 0 : -1) : (ref == null ? 1 : ref.compareTo(thisRef));
                }
            }
            return result;
        }
    }

    interface IValidationId {

        ISchemaLocator getSchemaLocator();
        JsonPointer getInstanceRef();
    }

    Type getType();
    IValidationId getId();
    boolean isOk();

    default Publisher<IValidationResult> publish() {
        return Mono.just(this);
    }

    interface IValidationResultContainer extends IValidationResult {

        Stream<IValidationResult> getNestedResults();

        @Override
        default Stream<IValidationResult> asStream() {
            return Stream.concat(
                    Stream.of(this),
                    getNestedResults()
                            .flatMap(IValidationResult::asStream)
            );
        }
    }

    interface IValidationResultError extends IValidationResult {

        String getError();

        @Override
        default Type getType() {
            return Type.ERROR;
        }

        @Override
        default boolean isOk() {
            return false;
        }
    }

    interface IValidationAnnotation extends IValidationResult {

        @Override
        default Type getType() {
            return Type.ANNOTATION;
        }

        @Override
        default boolean isOk() {
            return true;
        }
    }
}
