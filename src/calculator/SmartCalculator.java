package calculator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SmartCalculator {

    private static Pattern identifierPattern = Pattern.compile("^\\s*([a-zA-z])+\\s*(?==)");
    private static Pattern exspressionPattern = Pattern.compile("(?<==)[ -+*/^a-zA-z0-9]+$"); //Pattern.compile("(?<==)([ -+*/^]*(([a-zA-z])+|(\\d)+)\\s*)+$");
    private static Pattern assignmentPattern = Pattern.compile("^\\s*([a-zA-z])+\\s*=[ -+*/^a-zA-z0-9]+$");

    private static Map<String, Integer> variables = new HashMap<>();
    static void runCalculator() {

        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();

        while (!line.equals("/exit")) {

            try {
                parseLine(line);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }

            line = scanner.nextLine();

        }

        System.out.print("Bye!");

        Map<String, Integer> precedence = new HashMap<>();

    }

    private static void parseLine(String line) {

        if (line.length() == 0) {

            //doing nothing

        } else if (isAssignment(line)) {

            processAssingment(line);

        } else if (isVariable(line)) {

            printVariable(line);

        } else if (isCommand(line)) {

            executeCommand(line);

        } else {

            System.out.println(processExpression(convertToPostfix(line)));

        }

    }

    private static boolean isAssignment(String line) {

        return line.contains("=");

    }

    private static boolean isVariable(String line) {

        return Pattern.compile("^\\s*([a-zA-z])+\\s*$").matcher(line).find();

    }

    private static boolean isCommand(String line) {

        return Pattern.compile("^/\\w+$").matcher(line).find();

    }

    private static void processAssingment(String line) throws IllegalArgumentException {

        Matcher identifierMatcher = identifierPattern.matcher(line);
        Matcher expressionMatcher = exspressionPattern.matcher(line);
        Matcher assignmentMatcher = assignmentPattern.matcher(line);

        boolean isCorrectIdentifier = identifierMatcher.find();
        boolean isCorrectValue = expressionMatcher.find();
        boolean isCorrectAssignment = assignmentMatcher.find();

        if (!isCorrectIdentifier) {

            throw new IllegalArgumentException("Invalid identifier");

        } else if (!isCorrectValue) {

            throw new IllegalArgumentException("Invalid value");

        } else if (!isCorrectAssignment) {

            throw new IllegalArgumentException("Invalid assignment");

        } else {

            variables.put(identifierMatcher.group().trim(),calculateExpression(expressionMatcher.group()));

        }

    }

    private static void printVariable(String line) {

        System.out.println(getNumberFromVariable(line));

    }

    private static void executeCommand(String line) throws IllegalArgumentException {

        if (line.equals("/help")) {
            System.out.println("Any help here");
        }
        else {
            throw new IllegalArgumentException("Unknown command");
        }

    }

//    private static void processExpression(String line) {
//
//        int result = calculateExpression(line);
//
//        System.out.println(result);
//
//    }

    private static int calculateExpression(String line) {

        Matcher matcher = Pattern.compile("(^|[ +-])+(\\d|[a-zA-z])+").matcher(line);

        return processExpression(convertToPostfix(line));

    }

    private static int getNumberFromVariable(String group) throws IllegalArgumentException {

        boolean isNegative;

        String withSign = group.trim();
        String withoutSign = withSign.replaceAll("[ +-]","");

        Matcher matcher = Pattern.compile("-").matcher(withSign);
        isNegative = false;
        while (matcher.find()) isNegative = !isNegative;

        if (!variables.containsKey(withoutSign)) {
            throw new IllegalArgumentException("Unknown variable");
        }

        int numberFromVariables = variables.get(withoutSign);

        return  isNegative?-numberFromVariables:numberFromVariables;

    }


    private static int getNumberFromString(String group) {

        char[] charArray = group.toCharArray();
        boolean isNegative = false;
        int charPositionOfZero = (int)'0';
        int result = 0;

        for (char symbol:
             charArray) {
            if (symbol == '-') isNegative = !isNegative;
            if (symbol >= charPositionOfZero&& symbol < charPositionOfZero+10) {
                result = result*10+(int)symbol-charPositionOfZero;
            }
        }

        if (isNegative) result = -result;
        return result;
    }

    private static String convertToPostfix(String infixExpression) throws IllegalArgumentException {

        StringBuilder postfixExpression = new StringBuilder();

        Map<String, Integer> precedence = new HashMap<>();
        precedence.put("(",0);
        precedence.put("^",1);
        precedence.put("/",2);
        precedence.put("*",2);
        precedence.put("+",3);
        precedence.put("-",3);

        Stack<String> partsStack = new Stack<>();

        Pattern expressionPart = Pattern.compile("[(){}\\[\\]]|(?<=^)-?\\d+|(?<=\\D)-?\\d+|[a-zA-Z]+|[+-/*^]|\\S+");

        Matcher expressionParts = expressionPart.matcher(infixExpression);

        while (expressionParts.find()) {

            String currentPart = expressionParts.group();

            boolean isNumberOrIdentifier = isNumber(currentPart)
                    || isIdentifier(currentPart);
            boolean isOperator = currentPart.matches("[+-/*^]");
            boolean isLeftParenthesis = currentPart.matches("[({\\[]");
            boolean isRightParenthesis = currentPart.matches("[)}\\]]");

            if (isNumberOrIdentifier) {
                postfixExpression.append(currentPart).append(" ");

            } else if (partsStack.empty()|| partsStack.peek().equals("(")) {
                partsStack.push(currentPart);

            } else if (isOperator) {
                if (precedence.get(currentPart) >= precedence.get(partsStack.peek())) {
                    postfixExpression.append(partsStack.pop()).append(" ");

                }
                partsStack.push(currentPart);

            } else if (isLeftParenthesis) {
                partsStack.push("(");

            } else if (isRightParenthesis) {

                while (!(partsStack.empty() || partsStack.peek().equals("("))) {
                    postfixExpression.append(partsStack.pop()).append(" ");
                }

                if (partsStack.peek().equals("(")) {
                    partsStack.pop();

                } else {
                    throw new IllegalArgumentException("Invalid expression");

                }


            }

        }

        while (!partsStack.empty()) {

            String currentPart = partsStack.pop();

            boolean isLeftParenthesis = currentPart.matches("[({\\[]");
            boolean isRightParenthesis = currentPart.matches("[)}\\]]");

            if (isLeftParenthesis || isRightParenthesis) {
                throw new IllegalArgumentException("Invalid expression");
            } else {
                postfixExpression.append(currentPart).append(" ");

            }
        }

        return postfixExpression.toString().trim();

    }


    private static int processExpression(String s) {


        Stack<String> elementsStack = new Stack<>();

        String[] spittedExpression = s.split(" ");

        for (String part :
                spittedExpression) {
            if (isIdentifier(part) || isNumber(part)) {
                elementsStack.push(part);
            } else if (isOperator(part)) {
                int b = toNumber(elementsStack.pop());
                int a = toNumber(elementsStack.pop());
                switch (part) {
                    case "+":
                        elementsStack.push(String.valueOf(a + b));
                        break;
                    case "-":
                        elementsStack.push(String.valueOf(a - b));
                        break;
                    case "*":
                        elementsStack.push(String.valueOf(a * b));
                        break;
                    case "/":
                        elementsStack.push(String.valueOf(a / b));
                        break;
                    case "^":
                        elementsStack.push(String.valueOf((int) Math.pow(a, b)));
                        break;
                }
            }
        }

        return toNumber(elementsStack.pop());

    }

    private static int toNumber(String partToParse) throws IllegalArgumentException {

        if (isIdentifier(partToParse)) {
            return getNumberFromVariable(partToParse);
        } else if (isNumber(partToParse)) {
            return getNumberFromString(partToParse);
        }

        throw new IllegalArgumentException("Invalid expression");

    }

    private static boolean isOperator(String part) {
        return part.matches("[+-/*^]");
    }

    private static boolean isNumber(String part) {
        return part.matches("-?\\d+");
    }

    private static boolean isIdentifier(String part) {
        return part.matches("[a-zA-Z]+");
    }

}
