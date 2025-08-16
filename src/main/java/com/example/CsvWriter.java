package com.example;

import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvWriter {

    public void writeToCsv(List<CottonPrice> prices, String filePath) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            String[] header = {"Date", "Cotton Type", "Price"};
            writer.writeNext(header);

            // Write data
            for (CottonPrice price : prices) {
                String[] row = {
                        price.getDate().toString(),
                        price.getCottonType(),
                        String.valueOf(price.getPrice())
                };
                writer.writeNext(row);
            }
        }
    }
}
