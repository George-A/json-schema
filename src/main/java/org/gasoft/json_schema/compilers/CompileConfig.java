package org.gasoft.json_schema.compilers;

import org.gasoft.json_schema.IContentProcessing.ContentValidationLevel;
import org.gasoft.json_schema.IExternalResolver;
import org.gasoft.json_schema.IRegexPredicateFactory;
import org.gasoft.json_schema.common.content.CompositeContentValidationRegistry;
import org.gasoft.json_schema.common.content.DefaultContentValidationRegistryFactory;
import org.gasoft.json_schema.common.content.IContentValidationRegistry;
import org.gasoft.json_schema.common.regex.RegexFactory;
import org.gasoft.json_schema.loaders.IResourceLoader;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

public class CompileConfig {

    private boolean formatEnabled = false;
    private boolean allowTreatAsArray = false;
    private IRegexPredicateFactory regexpFactory = RegexFactory.jdk();
    private IExternalResolver externalSchemaResolver;
    private final List<IResourceLoader> resourceLoaders = new ArrayList<>();
    private Scheduler scheduler = Schedulers.fromExecutorService(Executors.newVirtualThreadPerTaskExecutor());

    private final Map<String, Predicate<String>> formats = new HashMap<>();
    private final CompositeContentValidationRegistry contentValidationRegistry =
            new CompositeContentValidationRegistry(DefaultContentValidationRegistryFactory.getDefault());
    private ContentValidationLevel contentValidationLevel= ContentValidationLevel.DEFAULT;

    public boolean isFormatEnabled() {
        return formatEnabled;
    }

    public CompileConfig setFormatEnabled(boolean formatEnabled) {
        this.formatEnabled = formatEnabled;
        return this;
    }

    public CompileConfig setRegexpFactory(IRegexPredicateFactory regexpFactory) {
        if(regexpFactory != null) {
            this.regexpFactory = regexpFactory;
        }
        return this;
    }

    public IRegexPredicateFactory getRegexpFactory() {
        return regexpFactory;
    }

    public CompileConfig addResourceLoader(IResourceLoader resourceLoader) {
        resourceLoaders.add(resourceLoader);
        return this;
    }

    public CompileConfig addResourceLoaders(Iterable<IResourceLoader> resourceLoaders) {
        resourceLoaders.forEach(this::addResourceLoader);
        return this;
    }

    public List<IResourceLoader> getResourceLoaders() {
        return resourceLoaders;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public CompileConfig setExternalSchemaResolver(IExternalResolver externalSchemaResolver) {
        this.externalSchemaResolver = externalSchemaResolver;
        return this;
    }

    public IExternalResolver getExternalSchemaResolver() {
        return externalSchemaResolver;
    }

    public CompileConfig setAllowTreatAsArray(boolean allowTreatAsArray) {
        this.allowTreatAsArray = allowTreatAsArray;
        return this;
    }

    public boolean isAllowTreatAsArray(){
        return allowTreatAsArray;
    }

    public CompileConfig setScheduler(Scheduler scheduler) {
        if(scheduler != null) {
            this.scheduler = scheduler;
        }
        return this;
    }

    public IContentValidationRegistry getContentValidationRegistry() {
        return contentValidationRegistry;
    }

    public ContentValidationLevel getContentValidationLevel() {
        return contentValidationLevel;
    }

    public CompileConfig setContentValidationLevel(ContentValidationLevel contentValidationLevel) {
        this.contentValidationLevel = contentValidationLevel;
        return this;
    }

    public CompileConfig addFormatValidators(Map<String, Predicate<String>> formatValidators) {
        this.formats.putAll(formatValidators);
        return this;
    }

    public Map<String, Predicate<String>> getFormats() {
        return formats;
    }

    public CompileConfig addFirstContentValidationRegistry(IContentValidationRegistry contentValidationRegistry) {
        this.contentValidationRegistry.addFirst(contentValidationRegistry);
        return this;
    }
}
