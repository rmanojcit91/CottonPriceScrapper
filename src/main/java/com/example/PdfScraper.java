package com.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class PdfScraper {

    private static final List<String> TARGET_COTTON_TYPES = Arrays.asList(
            "Ne 20/1 Carded Hoisery Yarn",
            "Ne 20/1 Combed Hoisery Yarn"
    );

    private final SentenceDetectorME sentenceDetector;
    private final TokenizerME tokenizer;
    private final NameFinderME nameFinder;

    public PdfScraper(String sentenceModelPath, String tokenizerModelPath, String nerModelPath) throws IOException {
        try (InputStream sentenceModelIn = new FileInputStream(sentenceModelPath);
             InputStream tokenizerModelIn = new FileInputStream(tokenizerModelPath);
             InputStream nerModelIn = new FileInputStream(nerModelPath)) {

            SentenceModel sentenceModel = new SentenceModel(sentenceModelIn);
            this.sentenceDetector = new SentenceDetectorME(sentenceModel);

            TokenizerModel tokenizerModel = new TokenizerModel(tokenizerModelIn);
            this.tokenizer = new TokenizerME(tokenizerModel);

            TokenNameFinderModel nerModel = new TokenNameFinderModel(nerModelIn);
            this.nameFinder = new NameFinderME(nerModel);
        }
    }

    public List<CottonPrice> scrapePdf(File pdfFile) throws IOException {
        List<CottonPrice> prices = new ArrayList<>();
        LocalDate reportDate = extractDateFromFilename(pdfFile.getName());

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            String[] sentences = sentenceDetector.sentDetect(text);

            for (int i = 0; i < sentences.length; i++) {
                String sentence = sentences[i];
                for (String cottonType : TARGET_COTTON_TYPES) {
                    if (sentence.contains(cottonType)) {
                        // Found a sentence with a target cotton type.
                        // Now, let's look for a price in this sentence or the next one.
                        String[] tokens = tokenizer.tokenize(sentence);
                        Span[] nameSpans = nameFinder.find(tokens);

                        if (nameSpans.length > 0) {
                            // Found a price in the same sentence.
                            String priceStr = tokens[nameSpans[0].getStart()];
                            addPrice(prices, reportDate, cottonType, priceStr);
                        } else if (i + 1 < sentences.length) {
                            // Didn't find a price, let's check the next sentence.
                            String nextSentence = sentences[i + 1];
                            String[] nextTokens = tokenizer.tokenize(nextSentence);
                            Span[] nextNameSpans = nameFinder.find(nextTokens);

                            if (nextNameSpans.length > 0) {
                                String priceStr = nextTokens[nextNameSpans[0].getStart()];
                                addPrice(prices, reportDate, cottonType, priceStr);
                            }
                        }
                    }
                }
            }
        }
        nameFinder.clearAdaptiveData(); // Clear adaptive data between documents
        return prices;
    }

    private void addPrice(List<CottonPrice> prices, LocalDate reportDate, String cottonType, String priceStr) {
        try {
            // Clean the price string: remove currency symbols, commas, etc.
            String cleanPriceStr = priceStr.replaceAll("[^\\d.]", "");
            double price = Double.parseDouble(cleanPriceStr);
            prices.add(new CottonPrice(reportDate, cottonType, price));
        } catch (NumberFormatException e) {
            System.err.println("Could not parse price '" + priceStr + "' for " + cottonType);
        }
    }

    private LocalDate extractDateFromFilename(String fileName) {
        // Regex to find a date in "DD Month YYYY" format from the filename.
        // It handles variations like "tecoya trend 17 July 2025.pdf" and "tecoya trend 21 July 2025 (2).pdf"
        Pattern pattern = Pattern.compile("(\\d{1,2})\\s+(January|February|March|April|May|June|July|August|September|October|November|December)\\s+(\\d{4})");
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            String dateStr = matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3);
            try {
                // Using a formatter that matches the "DD Month YYYY" format
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                System.err.println("Could not parse date from filename: " + fileName);
            }
        }
        // Fallback or error handling if no date is found
        throw new IllegalArgumentException("Could not extract date from filename: " + fileName);
    }
}
