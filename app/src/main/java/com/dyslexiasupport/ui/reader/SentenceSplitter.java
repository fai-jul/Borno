package com.dyslexiasupport.ui.reader;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits text into sentences using Bangla Dari (।) as well as ., ?, !.
 */
public class SentenceSplitter {

    public static class Sentence {
        public final int index;
        public final int start;
        public final int end;
        public final String text;

        public Sentence(int index, int start, int end, String text) {
            this.index = index;
            this.start = start;
            this.end = end;
            this.text = text;
        }
    }

    public static List<Sentence> split(String text) {
        List<Sentence> list = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return list;
        }

        int length = text.length();
        int sentenceStart = 0;
        int sentenceIndex = 0;

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            // Check sentence delimiters: Bangla Dari (।), period, question mark, exclamation mark
            if (c == '\u0964' || c == '.' || c == '?' || c == '!') {
                int sentenceEnd = i + 1;
                String sentenceText = text.substring(sentenceStart, sentenceEnd).trim();
                if (!sentenceText.isEmpty()) {
                    list.add(new Sentence(sentenceIndex++, sentenceStart, sentenceEnd, sentenceText));
                }
                sentenceStart = sentenceEnd;
                while (sentenceStart < length && Character.isWhitespace(text.charAt(sentenceStart))) {
                    sentenceStart++;
                }
            }
        }

        if (sentenceStart < length) {
            String sentenceText = text.substring(sentenceStart, length).trim();
            if (!sentenceText.isEmpty()) {
                list.add(new Sentence(sentenceIndex, sentenceStart, length, sentenceText));
            }
        }

        return list;
    }
}
