package com.hcmute.devhire.components;

import lombok.RequiredArgsConstructor;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jakarta.xml.bind.DatatypeConverter.parseDate;

@Component
@RequiredArgsConstructor
public class OpenNlpTextProcessor {
    private final SentenceModel sentenceModel;
    private final TokenizerModel tokenizerModel;
    private final POSModel posModel;

    public OpenNlpTextProcessor() throws IOException {
        // Load models (có thể tải từ file hoặc resource)
        InputStream sentenceModelStream = getClass().getResourceAsStream("/models/en-sent.bin");
        sentenceModel = new SentenceModel(sentenceModelStream);

        InputStream tokenModelStream = getClass().getResourceAsStream("/models/en-token.bin");
        tokenizerModel = new TokenizerModel(tokenModelStream);

        InputStream posModelStream = getClass().getResourceAsStream("/models/en-pos-maxent.bin");
        posModel = new POSModel(posModelStream);
    }

    public String normalizeText(String text) {
        // Xóa ký tự đặc biệt, chuyển về lowercase
        return text.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
    }

    public List<String> extractSentences(String text) {
        SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentenceModel);
        return Arrays.asList(sentenceDetector.sentDetect(text));
    }

    public List<String> extractNouns(String text) {
        List<String> nouns = new ArrayList<>();

        TokenizerME tokenizer = new TokenizerME(tokenizerModel);
        POSTaggerME posTagger = new POSTaggerME(posModel);

        String[] sentences = extractSentences(text).toArray(new String[0]);

        for (String sentence : sentences) {
            String[] tokens = tokenizer.tokenize(sentence);
            String[] tags = posTagger.tag(tokens);

            for (int i = 0; i < tokens.length; i++) {
                if (tags[i].startsWith("NN")) { // Noun tags
                    nouns.add(tokens[i]);
                }
            }
        }

        return nouns;
    }

    public double estimateExperienceFromDates(String text) {
        // Phân tích các mốc thời gian làm việc để ước tính số năm kinh nghiệm
        Pattern datePattern = Pattern.compile(
                "\\b(jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|" +
                        "jul(?:y)?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?)\\s+\\d{4}" +
                        "|\\d{1,2}/\\d{4}"
        );
        Matcher matcher = datePattern.matcher(text);
        List<LocalDate> dates = new ArrayList<>();

        while (matcher.find()) {
            String match = matcher.group();
            LocalDate date = parseDateFlexible(match);
            if (date != null) {
                dates.add(date);
            }
        }

        if (dates.size() >= 2) {
            dates.sort(Comparator.naturalOrder());
            long days = ChronoUnit.DAYS.between(dates.getFirst(), dates.getLast());
            return days / 365.0;
        }

        return 0;
    }

    private String capitalizeFirstMonthWord(String input) {
        String[] parts = input.split(" ");
        if (parts.length < 2) return input;
        parts[0] = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1).toLowerCase();
        return parts[0] + " " + parts[1];
    }

    private LocalDate parseDateFlexible(String dateStr) {
        try {
            // Normalize tháng: viết hoa chữ cái đầu tiên
            String normalized = capitalizeFirstMonthWord(dateStr.trim());

            // Định dạng "MMM yyyy"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
            return YearMonth.parse(normalized, formatter).atDay(1);
        } catch (Exception ignored) {}

        try {
            // Kiểu "01/2020"
            DateTimeFormatter mmYYYY = DateTimeFormatter.ofPattern("M/yyyy");
            return YearMonth.parse(dateStr.trim(), mmYYYY).atDay(1);
        } catch (Exception ignored) {}

        return null;
    }
}
