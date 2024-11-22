// Copyright (c) Microsoft. All rights reserved.
import com.microsoft.semantic.kernel.rag.splitting.Chunk;
import com.microsoft.semantic.kernel.rag.splitting.Splitter;
import com.microsoft.semantic.kernel.rag.splitting.document.TextDocument;
import com.microsoft.semantic.kernel.rag.splitting.overlap.CountOverlapCondition;
import com.microsoft.semantic.kernel.rag.splitting.overlap.PercentageOverlapCondition;
import com.microsoft.semantic.kernel.rag.splitting.splitconditions.CountSplitCondition;
import com.microsoft.semantic.kernel.rag.splitting.splitconditions.NewLineSplitter;
import com.microsoft.semantic.kernel.rag.splitting.splitconditions.ParagraphSplitter;
import com.microsoft.semantic.kernel.rag.splitting.splitconditions.SentenceSplitter;
import com.microsoft.semantic.kernel.rag.splitting.splitconditions.WordSplitter;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DocumentSplitTest {

    private final String NUMBERS = """
        01 02 03 04 05 06 07 08 09 10.
        11 12 13 14 15 16 17 18 19 20.
        21 22 23 24 25 26 27 28 29 30.
        31 32 33 34 35 36 37 38 39 40.
        41 42 43 44 45 46 47 48 49 50.
        51 52 53 54 55 56 57 58 59 60.
        61 62 63 64 65 66 67 68 69 70.
        71 72 73 74 75 76 77 78 79 80.
        81 82 83 84 85 86 87 88 89 90.
        91 92 93 94 95 96 97 98 99 100.
        """.stripIndent();

    private final String PARAGRAPHS = """
        01 02 03 04 05 06 07 08 09 10. 11 12 13 14 15
        16 17 18 19 20. 21 22 23 24
        25 26 27 28 29 30.

        31 32 33 34 35 36 37 38 39 40. 41 42 43 44
        45 46 47 48 49 50.

        51 52 53 54 55 56 57 58 59 60. 61
        62 63 64 65 66 67 68 69 70. 71 72 73
        74 75 76 77 78 79 80.


        81 82 83 84 85 86 87 88 89 90.


        91 92 93 94 95 96 97 98 99 100.
        """.stripIndent();

    @Test
    public void testWordSplit() {
        List<Chunk> chunks = Splitter.builder()
            .addChunkEndCondition(new CountSplitCondition(9, new WordSplitter()))
            .setOverlapCondition(new PercentageOverlapCondition(20.0f, new WordSplitter()))
            .trimWhitespace()
            .build()
            .splitDocument(new TextDocument(NUMBERS))
            .collectList()
            .block();

        Assertions.assertEquals(14, chunks.size());

        Assertions.assertEquals("""
            01 02 03 04 05 06 07 08 09"""
            .stripIndent(), chunks.get(0).getContents());

        Assertions.assertEquals("""
            08 09 10.
            11 12 13 14 15 16"""
            .stripIndent(), chunks.get(1).getContents());

        Assertions.assertEquals("""
            92 93 94 95 96 97 98 99 100."""
            .stripIndent(), chunks.get(13).getContents());
    }

    @Test
    public void testSentenceSplit() {
        List<Chunk> chunks = Splitter.builder()
            .addChunkEndCondition(new CountSplitCondition(4, new SentenceSplitter()))
            .setOverlapCondition(new CountOverlapCondition(2, new SentenceSplitter()))
            .trimWhitespace()
            .build()
            .splitDocument(new TextDocument(NUMBERS))
            .collectList()
            .block();

        Assertions.assertEquals(4, chunks.size());

        Assertions.assertEquals("""
            01 02 03 04 05 06 07 08 09 10.
            11 12 13 14 15 16 17 18 19 20.
            21 22 23 24 25 26 27 28 29 30.
            31 32 33 34 35 36 37 38 39 40."""
            .stripIndent(), chunks.get(0).getContents());
        Assertions.assertEquals("""
            21 22 23 24 25 26 27 28 29 30.
            31 32 33 34 35 36 37 38 39 40.
            41 42 43 44 45 46 47 48 49 50.
            51 52 53 54 55 56 57 58 59 60."""
            .stripIndent(), chunks.get(1).getContents());
        Assertions.assertEquals("""
            61 62 63 64 65 66 67 68 69 70.
            71 72 73 74 75 76 77 78 79 80.
            81 82 83 84 85 86 87 88 89 90.
            91 92 93 94 95 96 97 98 99 100."""
            .stripIndent(), chunks.get(3).getContents());
    }

    @Test
    public void testParagraphSplitter() {

        List<Chunk> chunks = Splitter.builder()
            .addChunkEndCondition(new CountSplitCondition(2, new ParagraphSplitter()))
            .setOverlapCondition(new CountOverlapCondition(2, new WordSplitter()))
            .trimWhitespace()
            .build()
            .splitDocument(new TextDocument(PARAGRAPHS))
            .collectList()
            .block();

        Assertions.assertEquals(
            """
                01 02 03 04 05 06 07 08 09 10. 11 12 13 14 15
                16 17 18 19 20. 21 22 23 24
                25 26 27 28 29 30.

                31 32 33 34 35 36 37 38 39 40. 41 42 43 44
                45 46 47 48 49 50.""".stripIndent(),
            chunks.get(0).getContents());

        Assertions.assertEquals(
            """
                89 90.


                91 92 93 94 95 96 97 98 99 100.""".stripIndent(),
            chunks.get(2).getContents());
    }

    @Test
    public void testNewLineSplitter() {

        List<Chunk> chunks = Splitter.builder()
            .addChunkEndCondition(new CountSplitCondition(2, new NewLineSplitter()))
            .trimWhitespace()
            .build()
            .splitDocument(new TextDocument(PARAGRAPHS))
            .collectList()
            .block();

        Assertions.assertEquals(
            """
                01 02 03 04 05 06 07 08 09 10. 11 12 13 14 15
                16 17 18 19 20. 21 22 23 24""".stripIndent(),
            chunks.get(0).getContents());
        Assertions.assertEquals(
            """
                25 26 27 28 29 30.

                31 32 33 34 35 36 37 38 39 40. 41 42 43 44""".stripIndent(),
            chunks.get(1).getContents());

        Assertions.assertEquals(
            """
                81 82 83 84 85 86 87 88 89 90.


                91 92 93 94 95 96 97 98 99 100.""".stripIndent(),
            chunks.get(4).getContents());
    }
}
