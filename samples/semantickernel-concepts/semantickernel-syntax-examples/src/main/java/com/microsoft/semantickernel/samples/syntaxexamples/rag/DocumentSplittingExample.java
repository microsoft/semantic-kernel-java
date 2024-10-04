// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples.rag;

import com.microsoft.semantic.kernel.rag.splitting.Chunk;
import com.microsoft.semantic.kernel.rag.splitting.Document;
import com.microsoft.semantic.kernel.rag.splitting.Splitter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import reactor.core.publisher.Flux;

public class DocumentSplittingExample {

    private static String BENEFITS_DOC = "https://raw.githubusercontent.com/Azure-Samples/azure-search-openai-demo-java/refs/heads/main/data/Benefit_Options.pdf";

    private static class PDFDocument implements Document {

        private final byte[] pdf;

        private PDFDocument(byte[] pdf) {
            this.pdf = pdf;
        }

        @Override
        public Flux<String> getContent() {
            try {
                PDFParser parser = new PDFParser(
                    RandomAccessReadBuffer.createBufferFromStream(new ByteArrayInputStream(pdf)));
                PDDocument document = parser.parse();
                String text = new PDFTextStripper().getText(document);

                return Flux.just(text);
            } catch (IOException e) {
                return Flux.error(e);
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        byte[] pdfBytes = getPdfDoc();
        PDFDocument pdfDoc = new PDFDocument(pdfBytes);

        Splitter splitter = Splitter
            .builder()
            .maxParagraphsPerChunk(4)
            .overlapNPercent(30.0f)
            .trimWhitespace()
            .build();

        List<Chunk> chunks = splitter
            .splitDocument(pdfDoc)
            .collectList()
            .block();

        chunks
            .forEach(chunk -> {
                System.out.println("=========");
                System.out.println(chunk.getContents());
            });
    }

    private static byte[] getPdfDoc() throws IOException, InterruptedException {
        HttpResponse<byte[]> doc = HttpClient.newHttpClient()
            .send(HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BENEFITS_DOC))
                .build(),
                BodyHandlers.ofByteArray());
        return doc.body();
    }

}
