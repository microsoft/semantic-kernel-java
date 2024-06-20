// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.presidio;

import com.azure.core.http.HttpClient;
import com.microsoft.semantickernel.presidio.models.AnalyzerResult;
import com.microsoft.semantickernel.presidio.models.anonymizerType.AnonymizerType.Replace;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import com.microsoft.semantickernel.semanticfunctions.annotations.SKSample;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class RedactorPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedactorPlugin.class);

    private final PresidioAnalysisClient analysisClient;

    private final PresidioAnonmizerClient anonymizeClient;

    public RedactorPlugin(
        String analysisServer,
        String anonymizeServer) {
        URL analysisUrl = null;
        try {
            analysisUrl = URI.create(analysisServer).toURL();
        } catch (MalformedURLException e) {
            LOGGER.error("Failed to parse url", e);
        }

        analysisClient = new PresidioAnalysisClient(
            HttpClient.createDefault(),
            analysisUrl);

        URL anonymizeUrl = null;
        try {
            anonymizeUrl = URI.create(anonymizeServer).toURL();
        } catch (MalformedURLException e) {
            LOGGER.error("Failed to parse url", e);
        }

        anonymizeClient = new PresidioAnonmizerClient(
            HttpClient.createDefault(),
            anonymizeUrl);
    }

    @DefineKernelFunction(name = "redact", description = "Takes a string of data and redacts sensitive data from that text.", returnType = "com.microsoft.semantickernel.presidio.AnonymizedText", returnDescription = "The redacted text", samples = {
            @SKSample(inputs = "Bob is tall.", output = "PERSON1 is tall.")
    })
    public Mono<AnonymizedText> redactData(
        @KernelFunctionParameter(name = "input", description = "Text to be redacted") String text) {
        return analysisClient
            .analyze(text, "en")
            .flatMap(analysisResult -> {
                Map<String, List<AnalyzerResult>> grouped = groupResultsByAnonymizedData(
                    analysisResult, text);

                Map<String, Replace> requestMaps = formAnonymizersMap(grouped);

                List<AnalyzerResult> allAnalyses = collectAllAnalyses(grouped);

                return anonymizeClient
                    .anonymize(text, requestMaps, allAnalyses)
                    .map(redacted -> {
                        Map<String, String> anonymizedTokenMap = formAnonymizedTokenMap(grouped);

                        return new AnonymizedText(
                            text,
                            redacted.text(),
                            anonymizedTokenMap);
                    });
            });

    }

    private static Map<String, String> formAnonymizedTokenMap(
        Map<String, List<AnalyzerResult>> grouped) {
        return grouped
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                entry -> entry.getValue().get(0).entityType(),
                Entry::getKey));
    }

    private static List<AnalyzerResult> collectAllAnalyses(
        Map<String, List<AnalyzerResult>> grouped) {
        return grouped.values()
            .stream()
            .flatMap(Collection::stream)
            .toList();
    }

    private static Map<String, Replace> formAnonymizersMap(
        Map<String, List<AnalyzerResult>> grouped) {
        return grouped
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                entry -> entry.getValue().get(0).entityType(),
                entry -> new Replace(entry.getValue().get(0).entityType())));
    }

    private static Map<String, List<AnalyzerResult>> groupResultsByAnonymizedData(
        List<AnalyzerResult> analysisResult, String text) {
        Map<String, List<AnalyzerResult>> grouped = analysisResult
            .stream()
            .collect(Collectors.groupingBy(
                analyzerResult -> text.substring(analyzerResult.start(),
                    analyzerResult.end())));

        AtomicInteger count = new AtomicInteger(0);
        grouped = grouped
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Entry::getKey,
                entry -> {
                    count.incrementAndGet();
                    return entry.getValue().stream().map(
                        r -> new AnalyzerResult(
                            r.start(),
                            r.end(),
                            r.score(),
                            r.entityType() + count.get(),
                            r.recognitionMetadata(),
                            r.analysisExplanation()))
                        .toList();
                }));
        return grouped;
    }
}
