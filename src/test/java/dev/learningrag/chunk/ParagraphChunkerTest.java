package dev.learningrag.chunk;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParagraphChunkerTest {

    private final ParagraphChunker chunker = new ParagraphChunker(200, 40);

    @Test
    void returnsNothingForBlankText() {
        assertThat(chunker.chunk("")).isEmpty();
        assertThat(chunker.chunk("   \n\n  \n")).isEmpty();
    }

    @Test
    void keepsShortDocumentAsSingleChunk() {
        List<Chunk> chunks = chunker.chunk("Первый абзац.\n\nВторой абзац.");

        assertThat(chunks).hasSize(1);
        Chunk only = chunks.get(0);
        assertThat(only.text()).isEqualTo("Первый абзац.\n\nВторой абзац.");
        assertThat(only.startOffset()).isZero();
        assertThat(only.endOffset()).isEqualTo(only.text().length());
        assertThat(only.paragraphStart()).isZero();
        assertThat(only.paragraphEnd()).isEqualTo(1);
    }

    @Test
    void offsetsMatchNormalizedText() {
        String raw = "Абзац один.\r\n\r\nАбзац два.\r\n\r\n\r\nАбзац три.";
        List<Chunk> chunks = chunker.chunk(raw);
        String normalized = ParagraphChunker.normalize(raw);

        assertThat(chunks).isNotEmpty();
        for (Chunk chunk : chunks) {
            assertThat(normalized.substring(chunk.startOffset(), chunk.endOffset())).isEqualTo(chunk.text());
        }
        assertThat(normalized).doesNotContain("\r");
        assertThat(normalized).doesNotContain("\n\n\n");
    }

    @Test
    void respectsMaxCharsAndOverlapsConsecutiveChunks() {
        String text = String.join("\n\n", paragraphs(6, 120));
        List<Chunk> chunks = chunker.chunk(text);

        assertThat(chunks).hasSizeGreaterThan(1);
        for (Chunk chunk : chunks) {
            assertThat(chunk.text().length()).isLessThanOrEqualTo(200);
        }
        for (int i = 1; i < chunks.size(); i++) {
            Chunk previous = chunks.get(i - 1);
            Chunk current = chunks.get(i);
            assertThat(current.startOffset()).isLessThan(previous.endOffset());
            assertThat(previous.endOffset() - current.startOffset()).isLessThanOrEqualTo(40);
            assertThat(current.startOffset()).isGreaterThan(previous.startOffset());
        }
    }

    @Test
    void splitsOversizedParagraphAtWordBoundaries() {
        String longParagraph = "слово ".repeat(200).trim();
        List<Chunk> chunks = chunker.chunk(longParagraph);

        assertThat(chunks).hasSizeGreaterThan(1);
        StringBuilder joined = new StringBuilder();
        for (Chunk chunk : chunks) {
            assertThat(chunk.text().length()).isLessThanOrEqualTo(200);
            assertThat(chunk.text()).doesNotStartWith(" ");
            joined.append(chunk.text()).append(' ');
        }
        assertThat(joined.toString()).contains("слово слово");
        assertThat(chunks.get(0).paragraphStart()).isZero();
    }

    @Test
    void coversWholeDocumentWithoutGaps() {
        String text = String.join("\n\n", paragraphs(8, 150));
        List<Chunk> chunks = chunker.chunk(text);

        int end = 0;
        for (Chunk chunk : chunks) {
            assertThat(chunk.startOffset()).isLessThanOrEqualTo(end);
            end = Math.max(end, chunk.endOffset());
        }
        assertThat(end).isEqualTo(text.length());
    }

    @Test
    void isDeterministic() {
        String text = String.join("\n\n", paragraphs(5, 90));
        assertThat(chunker.chunk(text)).isEqualTo(chunker.chunk(text));
    }

    private static List<String> paragraphs(int count, int words) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            StringBuilder sb = new StringBuilder("Абзац " + i + ".");
            for (int w = 0; w < words; w++) {
                sb.append(" текст").append(w).append('.');
            }
            result.add(sb.toString());
        }
        return result;
    }
}
