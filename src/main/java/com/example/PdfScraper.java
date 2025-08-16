package com.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfScraper {

    // A list of cotton types we are interested in.
    private static final List<String> TARGET_COTTON_TYPES = Arrays.asList(
            "Ne 20/1 Carded Hoisery Yarn",
            "Ne 20/1 Combed Hoisery Yarn"
    );

    public List<CottonPrice> scrapePdf(File pdfFile) throws IOException {
        List<CottonPrice> prices = new ArrayList<>();

        // Extract date from filename
        LocalDate reportDate = extractDateFromFilename(pdfFile.getName());

        // Extract text from PDF
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);

            // For each target cotton type, try to find the price in the text
            for (String cottonType : TARGET_COTTON_TYPES) {
                // Regex to find the cotton type followed by a price (a number with decimals)
                // This regex is an assumption and may need to be adjusted based on the PDF's text layout.
                // It looks for the cotton type, then any characters until it finds a number like "123.45".
                Pattern pattern = Pattern.compile(Pattern.quote(cottonType) + "\\s+.*?(\\d+\\.\\d{2})");
                Matcher matcher = pattern.matcher(text);

                if (matcher.find()) {
                    try {
                        double price = Double.parseDouble(matcher.group(1));
                        prices.add(new CottonPrice(reportDate, cottonType, price));
                    } catch (NumberFormatException e) {
                        // Handle case where the matched text is not a valid double
                        System.err.println("Could not parse price for " + cottonType + " in " + pdfFile.getName());
                    }
                }
            }
        }

        return prices;
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
