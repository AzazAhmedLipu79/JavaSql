package Util;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CsvParser {
    private List<String> columnNames = new ArrayList<>();
    private List<Map<String, String>> rows = new ArrayList<>();

    public CsvParser(String filePath) throws IOException {
        parseCsv(filePath);
    }

    private void parseCsv(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IOException("Empty CSV file");
            }
            columnNames = parseLine(headerLine);

            String line;
            while ((line = br.readLine()) != null) {
                List<String> values = parseLine(line);
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < columnNames.size(); i++) {
                    String value = i < values.size() ? values.get(i) : "";
                    row.put(columnNames.get(i), value);
                }
                rows.add(row);
            }
        }
    }

    // Simple CSV line parser (handles commas inside quotes)
    private List<String> parseLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());

        return tokens;
    }

    // Returns list of column names
    public List<String> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }

    // Search rows where column 'columnName' matches 'value' (exact match)
    public List<Map<String, String>> search(String columnName, String value) {
        List<Map<String, String>> results = new ArrayList<>();
        for (Map<String, String> row : rows) {
            if (value.equals(row.get(columnName))) {
                results.add(row);
            }
        }
        return results;
    }

    // Simple test
    public static void main(String[] args) {
        try {
            CsvParser parser = new CsvParser("pages/meta/columns.csv");

            System.out.println("Columns:");
            for (String col : parser.getColumnNames()) {
                System.out.println(" - " + col);
            }

            System.out.println("\nSearch results for type='TEXT':");
            List<Map<String, String>> results = parser.search("type", "TEXT");
            for (Map<String, String> row : results) {
                System.out.println(row);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
