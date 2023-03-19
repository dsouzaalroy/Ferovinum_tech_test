import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class Main {

    public static void main(String[] args) {
        printCSVContents(processPostfixExpression(Paths.get(args[0])));
    }

    public static ArrayList<String[]> parseCSVToArrayList(Path csvPath) {
        ArrayList<String[]> csvList = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(csvPath);
            while (scanner.hasNextLine()) {
                csvList.add(scanner.nextLine().split(","));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return csvList;
    }

    public static ArrayList<String[]> processPostfixExpression(Path csvPath) {
        Stack<Float> numberBuffer = new Stack<>();
        ArrayList<String[]> csvList = parseCSVToArrayList(csvPath);

        for (int col = 0; col < csvList.size(); col++) {
            for (int row = 0; row < csvList.get(col).length; row++) {
                String[] cells = getCells(csvList, col, row);
                evaluateCell(numberBuffer, csvList, col, row, cells);
                if (numberBuffer.size() != 1) {
                    setError(numberBuffer, csvList, col, row);
                } else {
                    csvList.get(col)[row] = String.valueOf(numberBuffer.pop());
                }
            }
        }
        return csvList;
    }

    private static void evaluateCell(Stack<Float> numberBuffer, ArrayList<String[]> csvList, int col, int row, String[] cells) {
        for (String character : cells) {
            if (!isOperator(character)) {
                String stringValue = getValue(character, csvList);
                if (stringValue.equals("#ERR")) {
                    setError(numberBuffer, csvList, col, row);
                    break;
                }
                numberBuffer.push(Float.valueOf(stringValue));
            } else {
                if (!(numberBuffer.size() <= 1)) {
                    float result = computeArithmetic(character, numberBuffer.pop(), numberBuffer.pop());
                    if (Float.isNaN(result) || Float.isInfinite(result)) {
                        setError(numberBuffer, csvList, col, row);
                        break;
                    }
                    numberBuffer.push(result);
                } else {
                    setError(numberBuffer, csvList, col, row);
                }
            }
        }
    }

    public static String getValue(String character, ArrayList<String[]> computedCSVList) {
        // a, a1, 1a, #, >, aaa
        if (isNotFloat(character)) {
            try {
                int colValue = getColumnValue(character);
                int rowValue = getRowValue(character);
                if (isItself(character, computedCSVList)) return "#ERR";
                String cellValue = computedCSVList.get(rowValue)[colValue];
                if (isNotFloat(cellValue)) {
                    return getValue(cellValue.trim(), computedCSVList);
                } else {
                    return computedCSVList.get(rowValue)[colValue];
                }
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                return "#ERR";
            }
        } else {
            return character;
        }

    }

    private static void setError(Stack<Float> numberBuffer, ArrayList<String[]> csvList, int col, int row) {
        csvList.get(col)[row] = "#ERR";
        numberBuffer.removeAllElements();
    }

    private static String[] getCells(ArrayList<String[]> csvList, int col, int row) {
        return csvList.get(col)[row].trim().split("\\s+");
    }

    public static float computeArithmetic(String character, float secondValue, float firstValue) {
        float result;
        switch (character) {
            case "+" -> result = firstValue + secondValue;
            case "-" -> result = firstValue - secondValue;
            case "*" -> result = firstValue * secondValue;
            case "/" -> result = firstValue / secondValue;
            default -> {
                return 0.0F;
            }
        }
        return result;
    }

    public static boolean isOperator(String character) {
        return switch (character) {
            case "+", "-", "/", "*" -> true;
            default -> false;
        };
    }


    private static boolean isItself(String character, ArrayList<String[]> computedCSVList) {
        return character.equals(computedCSVList.get(getRowValue(character))[getColumnValue(character)].trim());
    }

    public static boolean isNotFloat(String character) {
        try {
            Float.parseFloat(character);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public static int getColumnValue(String character) {
        return ((int) character.toLowerCase().charAt(0) - 96) - 1;
    }

    public static int getRowValue(String character) {
        return Character.getNumericValue(character.charAt(1)) - 1;
    }

    private static void printCSVContents(ArrayList<String[]> csvList) {
        for (String[] array : csvList) {
            for (int row = 0; row < array.length; row++) {
                System.out.print(wholeOrFraction(array[row]));
                if (row != array.length - 1) System.out.print(",");
            }
            System.out.println();
        }
    }

    private static String wholeOrFraction(String str) {
        if (str.equals("#ERR")) return str;
        float number = Float.parseFloat(str);
        if (number % 1 == 0) {
            return String.valueOf(((int) number));
        } else {
            return str;
        }
    }
}