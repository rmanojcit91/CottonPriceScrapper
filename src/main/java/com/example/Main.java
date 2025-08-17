package com.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Initialize components
        PdfScraper scraper;
        try {
            scraper = new PdfScraper("models/en-sent.bin", "models/en-token.bin", "models/en-ner-money.bin");
        } catch (IOException e) {
            System.err.println("Error initializing NLP models: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        CsvWriter csvWriter = new CsvWriter();
        ChartGenerator chartGenerator = new ChartGenerator();

        // Find all PDF files in the current directory
        File currentDir = new File(".");
        File[] files = currentDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

        if (files == null || files.length == 0) {
            System.out.println("No PDF files found in the current directory.");
            return;
        }

        // List to hold all extracted data
        List<CottonPrice> allPrices = new ArrayList<>();

        // Process each PDF file
        for (File file : files) {
            try {
                System.out.println("Processing file: " + file.getName());
                List<CottonPrice> pricesFromFile = scraper.scrapePdf(file);
                allPrices.addAll(pricesFromFile);
            } catch (IOException e) {
                System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("Error parsing date from file " + file.getName() + ": " + e.getMessage());
            }
        }

        // Write data to CSV
        try {
            csvWriter.writeToCsv(allPrices, "cotton_prices.csv");
            System.out.println("Data successfully written to cotton_prices.csv");
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }

        // Generate chart
        try {
            chartGenerator.generateChart(allPrices, "price_trend_chart.png");
            System.out.println("Chart successfully generated as price_trend_chart.png");
        } catch (IOException e) {
            System.err.println("Error generating chart: " + e.getMessage());
        }
    }
}
