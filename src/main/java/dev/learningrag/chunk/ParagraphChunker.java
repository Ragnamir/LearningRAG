package dev.learningrag.chunk;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic paragraph-aware chunker.
 *
 * <p>Text is normalized first (line endings unified, runs of blank lines collapsed), and all reported offsets
 * refer to that normalized text. Chunks are built from sentence-sized units so that a chunk boundary never cuts a
 * sentence when the sentence fits within the limit. Consecutive chunks share a tail of at most
 * {@code overlapChars}, which is aligned to unit boundaries rather than raw character counts.
 */
public class ParagraphChunker implements DocumentChunker {

    private final int maxChars;
    private final int overlapChars;

    public ParagraphChunker(int maxChars, int overlapChars) {
        if (maxChars < 100) {
            throw new IllegalArgumentException("maxChars must be at least 100");
        }
        if (overlapChars < 0 || overlapChars >= maxChars) {
            throw new IllegalArgumentException("overlapChars must be in [0, maxChars)");
        }
        this.maxChars = maxChars;
        this.overlapChars = overlapChars;
    }

    @Override
    public List<Chunk> chunk(String documentText) {
        String text = normalize(documentText);
        List<Unit> units = splitUnits(text);
        if (units.isEmpty()) {
            return List.of();
        }

        List<Chunk> chunks = new ArrayList<>();
        int start = 0;
        while (start < units.size()) {
            int end = start;
            while (end + 1 < units.size() && units.get(end + 1).end() - units.get(start).start() <= maxChars) {
                end++;
            }

            Unit first = units.get(start);
            Unit last = units.get(end);
            String content = text.substring(first.start(), last.end());
            chunks.add(new Chunk(chunks.size(), content, first.start(), last.end(),
                    first.paragraph(), last.paragraph()));

            if (end + 1 >= units.size()) {
                break;
            }
            start = nextStart(units, start, end);
        }
        return chunks;
    }

    private int nextStart(List<Unit> units, int start, int end) {
        int overlapStart = end;
        while (overlapStart - 1 >= start
                && units.get(end).end() - units.get(overlapStart - 1).start() <= overlapChars) {
            overlapStart--;
        }
        int next = Math.max(overlapStart, start + 1);
        return Math.min(next, end + 1);
    }

    static String normalize(String text) {
        String unified = text.replace("\r\n", "\n").replace('\r', '\n');
        return unified.replaceAll("\n{3,}", "\n\n");
    }

    private List<Unit> splitUnits(String text) {
        List<Unit> units = new ArrayList<>();
        int paragraph = 0;
        int cursor = 0;
        while (cursor < text.length()) {
            int blank = indexOfBlankLine(text, cursor);
            int paragraphEnd = blank < 0 ? text.length() : blank;
            addParagraphUnits(text, cursor, paragraphEnd, paragraph, units);
            paragraph++;
            if (blank < 0) {
                break;
            }
            cursor = skipBlankLine(text, blank);
        }
        return units;
    }

    private void addParagraphUnits(String text, int from, int to, int paragraph, List<Unit> units) {
        int cursor = from;
        while (cursor < to) {
            while (cursor < to && Character.isWhitespace(text.charAt(cursor))) {
                cursor++;
            }
            if (cursor >= to) {
                return;
            }
            int limit = Math.min(to, cursor + maxChars);
            int end = findUnitEnd(text, cursor, limit, to);
            units.add(new Unit(cursor, end, paragraph));
            cursor = end;
        }
    }

    /**
     * Returns the end offset of the next unit: a sentence terminator followed by whitespace, a line break, or the
     * paragraph end. Sentences longer than the chunk limit are cut at the last word boundary that fits.
     */
    private int findUnitEnd(String text, int start, int limit, int paragraphEnd) {
        for (int i = start; i < limit; i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                return i;
            }
            if (isTerminator(c) && (i + 1 >= paragraphEnd || Character.isWhitespace(text.charAt(i + 1)))) {
                return i + 1;
            }
        }
        if (limit >= paragraphEnd) {
            return paragraphEnd;
        }
        int cut = limit;
        while (cut > start && !Character.isWhitespace(text.charAt(cut - 1))) {
            cut--;
        }
        return cut > start ? cut : limit;
    }

    private static boolean isTerminator(char c) {
        return c == '.' || c == '!' || c == '?' || c == '…' || c == ';';
    }

    private static int indexOfBlankLine(String text, int from) {
        int i = text.indexOf("\n\n", from);
        return i;
    }

    private static int skipBlankLine(String text, int blankStart) {
        int i = blankStart;
        while (i < text.length() && text.charAt(i) == '\n') {
            i++;
        }
        return i;
    }

    private record Unit(int start, int end, int paragraph) {
    }
}
