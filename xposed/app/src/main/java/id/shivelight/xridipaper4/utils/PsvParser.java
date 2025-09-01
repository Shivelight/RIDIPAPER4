package id.shivelight.xridipaper4.utils;

import java.util.ArrayList;
import java.util.List;

public class PsvParser {

    public static List<String> parse(String csvData) {
        return parse(csvData, 0);
    }

    public static List<String> parse(String psvData, int expectedColumns) {
        List<String> columns = new ArrayList<>();

        // Regex to split by the '|' delimiter, but not when it's inside double quotes.
        String[] fields = psvData.split("\\|(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        for (String field : fields) {
            String columnContent;
            if (field.startsWith("\"") && field.endsWith("\"")) {
                columnContent = field.substring(1, field.length() - 1);
                columnContent = columnContent.replace("\"\"", "\"");
            } else {
                columnContent = field;
            }
            columns.add(columnContent);
        }
        if (expectedColumns > 0 && columns.size() != expectedColumns) {
            throw new IllegalArgumentException(
                    "PSV parsing error: Expected " + expectedColumns + " columns, but found " + columns.size() + "."
            );
        }
        return columns;
    }
}
