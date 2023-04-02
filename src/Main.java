import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    private static final String DELIMITER = ",";

    public static void main(String[] args) {
//        printCSVContents(processPostfixExpression(Paths.get("src/testData.csv")));
        printCSVContents(processPostfixExpression(Paths.get(args[0])));

    }

    /**
     * Parses the contents of a CSV file into a nested HashMap structure.
     *
     * @param csvPath the Path to the CSV file
     * @return a HashMap representing the contents of the CSV file
     * @throws RuntimeException if there is an IOException while reading the file
     */
    private static HashMap<Integer, HashMap<Integer, String>> parseCSVToHashMap(Path csvPath) {
        HashMap<Integer, HashMap<Integer, String>> csvMap = new HashMap<>();
        try {
            int row = 0;
            Scanner scanner = new Scanner(csvPath);
            while (scanner.hasNextLine()) {
                HashMap<Integer, String> rowMap = new HashMap<>();
                int col = 0;
                String[] line = scanner.nextLine().split(DELIMITER);
                for (String rowValue : line) {
                    rowMap.put(col, rowValue);
                    col++;
                }
                csvMap.put(row, rowMap);
                row++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return csvMap;
    }


    /**
     * Processes the contents of a CSV file as a postfix mathematical expression.
     *
     * @param csvPath the Path to the CSV file
     * @return a HashMap representing the contents of the CSV file after evaluating each cell
     * as a mathematical expression
     */
    private static HashMap<Integer, HashMap<Integer, String>> processPostfixExpression(Path csvPath) {
        HashMap<Integer, HashMap<Integer, String>> csvList = parseCSVToHashMap(csvPath);
        for (int row = 0; row < csvList.size(); row++) {
            HashMap<Integer, String> currentRow = csvList.get(row);
            for (int col = 0; col < currentRow.size(); col++) {
                String[] cell = getCell(currentRow.get(col));
                double value = evaluateCell(cell, currentRow, col, csvList, new HashSet<>());
                if (Double.isNaN(value)) {
                    setError(currentRow, col);
                } else {
                    currentRow.put(col, String.valueOf(value));

                }
            }
        }
        return csvList;
    }

    /**
     * Evaluates a cell in a CSV file as a mathematical expression using the given HashMap of cell values.
     *
     * @param cell       the cell to evaluate, represented as an array of String tokens
     * @param currentRow the HashMap representing the current row of the cell being evaluated
     * @param col        the index of the current cell within its row
     * @param csvList    the nested HashMap representing the contents of the CSV file
     * @param uniqueSet  a Set containing all previously evaluated cell values
     * @return the numerical result of evaluating the cell, or NaN if there was an error
     */
    private static double evaluateCell(String[] cell, HashMap<Integer, String> currentRow, int col, HashMap<Integer, HashMap<Integer, String>> csvList, Set<String> uniqueSet) {
        Stack<Double> numberBuffer = new Stack<>();
        for (String character : cell) {
            if (!isOperator(character)) {
                String stringValue = getValue(character, csvList, uniqueSet);
                if (stringValue.equals("#ERR")) {
                    setError(numberBuffer, currentRow, col);
                    break;
                }
                numberBuffer.push(Double.valueOf(stringValue));
            } else {
                if (!(numberBuffer.size() <= 1)) {
                    double result = computeArithmetic(character, numberBuffer.pop(), numberBuffer.pop());
                    if (Double.isNaN(result) || Double.isInfinite(result)) {
                        setError(numberBuffer, currentRow, col);
                        break;
                    }
                    numberBuffer.push(result);
                } else {
                    setError(numberBuffer, currentRow, col);
                }
            }
        }
        if (numberBuffer.size() != 1) {
            numberBuffer.removeAllElements();
            return Double.NaN;
        }
        return numberBuffer.pop();
    }

    /**
     * Gets the value of a cell in a CSV file, either by looking it up in the given HashMap or evaluating it as a mathematical expression.
     *
     * @param character the String representation of the cell value to get
     * @param csvList   the nested HashMap representing the contents of the CSV file
     * @param uniqueSet a Set containing all previously evaluated cell values
     * @return the String representation of the cell value
     */
    private static String getValue(String character, HashMap<Integer, HashMap<Integer, String>> csvList, Set<String> uniqueSet) {
        if (isNotDouble(character)) {
            try {

                HashMap<Integer, String> currentRow = getCurrentRow(character, csvList);
                int colValue = getColumnValue(character);
                String cellValue = currentRow.get(colValue);

                if (uniqueSet.contains(cellValue)) return "#ERR";

                if (isNotDouble(cellValue)) {
                    uniqueSet.add(cellValue);
                    double newValue = evaluateCell(getCell(cellValue), currentRow, colValue, csvList, uniqueSet);
                    return String.valueOf(newValue);
                } else {
                    return cellValue;
                }
            } catch (NumberFormatException |
                     NullPointerException |
                     StringIndexOutOfBoundsException e) {
                return "#ERR";
            }
        } else {
            if (isNotDouble(String.valueOf(character.charAt(0)))) {
                return "#ERR";
            } else {
                return character;
            }
        }

    }

    /**
     * Sets the value of a cell in a CSV file to #ERR.
     *
     * @param currentRow the HashMap representing the row containing the cell to set
     * @param col        the index of the cell within its row
     */
    private static void setError(HashMap<Integer, String> currentRow, int col) {
        currentRow.put(col, "#ERR");
    }


    /**
     * Sets the value of a cell in a CSV file to #ERR and clears the contents of the given Stack.
     *
     * @param numberBuffer the Stack containing the results of partial evaluations within a cell
     * @param currentRow   the HashMap representing the row containing the cell to set
     * @param col          the index of the cell within its row
     */
    private static void setError(Stack<Double> numberBuffer, HashMap<Integer, String> currentRow, int col) {
        currentRow.put(col, "#ERR");
        numberBuffer.removeAllElements();
    }

    /**
     * Splits a cell value into an array of String tokens.
     *
     * @param cell the String representation of the cell value to split
     * @return an array of String tokens representing the cell value
     */
    private static String[] getCell(String cell) {
        return cell.trim().split("\\s+");
    }


    /**
     * Computes the arithmetic expression based on the given operator and values.
     *
     * @param character   The operator character (+, -, *, /)
     * @param secondValue The second value in the expression
     * @param firstValue  The first value in the expression
     * @return The result of the arithmetic operation
     */
    private static double computeArithmetic(String character, double secondValue, double firstValue) {
        double result;
        switch (character) {
            case "+" -> result = firstValue + secondValue;
            case "-" -> result = firstValue - secondValue;
            case "*" -> result = firstValue * secondValue;
            case "/" -> result = firstValue / secondValue;
            default -> {
                return Double.NaN;
            }
        }
        return result;
    }


    /**
     * Checks if the given string is a valid operator.
     *
     * @param character The character to be checked
     * @return True if the character is a valid operator (+, -, *, /), false otherwise
     */
    public static boolean isOperator(String character) {
        return switch (character) {
            case "+", "-", "/", "*" -> true;
            default -> false;
        };
    }


    /**
     * Checks if the given string is a valid double.
     *
     * @param character The character to be checked
     * @return True if the character is not a valid double, false otherwise
     */
    private static boolean isNotDouble(String character) {
        try {
            Double.parseDouble(character);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }


    /**
     * Converts the given column character to a zero-indexed integer value.
     *
     * @param character The column character to be converted (A, B, C, etc.)
     * @return The zero-indexed integer value of the column character
     */
    private static int getColumnValue(String character) {
        return ((int) character.toLowerCase().charAt(0) - 96) - 1;
    }

    /**
     * Gets the current row in the CSV file
     *
     * @param character The row character to be converted (1, 2, 3, etc.)
     * @param csvList   the nested HashMap representing the contents of the CSV file
     * @return A HashMap representing the current row in the CSV file
     */
    private static HashMap<Integer, String> getCurrentRow(String character, HashMap<Integer, HashMap<Integer, String>> csvList) {
        return csvList.get(Integer.parseInt(character.substring(1)) - 1);
    }


    /**
     * Prints the contents of a CSV file to the console.
     *
     * @param csvList The list of rows in the CSV file
     */
    private static void printCSVContents(HashMap<Integer, HashMap<Integer, String>> csvList) {
        for (int row = 0; row < csvList.size(); row++) {
            HashMap<Integer, String> currentRow = csvList.get(row);
            int commaCounter = currentRow.size();
            for (int col = 0; col < currentRow.size(); col++) {
                System.out.print(formatString(currentRow.get(col)));
                if (commaCounter != 1) {
                    System.out.print(",");
                    commaCounter--;
                }
            }
            System.out.println();
        }
    }


    /**
     * Formats the given string for display in the CSV file.
     *
     * @param str The string to be formatted
     * @return The formatted string
     */
    private static String formatString(String str) {
        if (str.equals("#ERR")) return str;
        double number = Double.parseDouble(str);
        if (number % 1 == 0) {
            return String.valueOf(((int) number));
        } else {
            return str;
        }
    }

}