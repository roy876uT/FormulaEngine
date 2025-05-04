package com.example.engine;


import com.example.mutableDecimal.MutableDecimal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class FormulaCalculator {

    private final Operation operation;
    public enum OPERATION_IMPLEMENTATION {
        BIGDECIMAL,
        MUTABLEDECIMAL
    }

    public FormulaCalculator(){
        operation = new MutableDecimalOperation();
    }

    public FormulaCalculator(OPERATION_IMPLEMENTATION operationImplementation) {
        operation = switch (operationImplementation) {
            case BIGDECIMAL -> new BigDecimalOperation();
            case MUTABLEDECIMAL -> new MutableDecimalOperation();
            default -> throw new UnsupportedOperationException("new operation implementation needed");
        };
    }

    interface Operation {
        String multiply(String first, String second);
        String divide(String first, String second);
        String sumDeque(Deque<String> deque);
        String sumList(List<String> inputList);
    }

    static class BigDecimalOperation implements Operation {

        @Override
        public String multiply(String first, String second) {
            return (new BigDecimal(first).multiply(new BigDecimal(second))).toString();
        }

        @Override
        public String divide(String first, String second) {
            return (new BigDecimal(first).divide(new BigDecimal(second), 2, RoundingMode.HALF_UP)).toString();
        }

        @Override
        public String sumDeque(Deque<String> deque) {
            BigDecimal result = BigDecimal.ZERO;
            while (!deque.isEmpty()){
                result = result.add(new BigDecimal(deque.pop()));
            }
            return result.toString();
        }

        @Override
        public String sumList(List<String> inputList){
            return inputList.stream().map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add).toString();
        }
    }

    static class MutableDecimalOperation implements Operation {

        @Override
        public String multiply(String first, String second) {
            return (new MutableDecimal(first).multiply(new MutableDecimal(second))).toString();
        }

        @Override
        public String divide(String first, String second) {
            return (new MutableDecimal(first).divide(new MutableDecimal(second))).toString();
        }

        @Override
        public String sumDeque(Deque<String> deque) {
            MutableDecimal result = MutableDecimal.ZERO;
            while (!deque.isEmpty()){
                result = result.add(new MutableDecimal(deque.pop()));
            }
            return result.toString();
        }

        @Override
        public String sumList(List<String> inputList){
            return inputList.stream().map(MutableDecimal::new).reduce(MutableDecimal.ZERO, MutableDecimal::add).toString();
        }
    }

    private String evaluate(char operator, String first, String second) {
        return switch (operator) {
            case '+' -> {
                yield first;
            }
            case '-' -> {
                yield "-" + first;
            }
            case '*' -> {
                yield this.operation.multiply(first, second);
            }
            case '/' -> {
                yield this.operation.divide(first, second);
            }
            default -> throw new IllegalArgumentException(String.format("invalid operator (%s)", operator));
        };
    }

    public String calculate(String s) {
        Deque<String> stack = new ArrayDeque<>();
        String curr = "";
        char previousOperator = '+';
        s += "@";
        Set<String> operators = new HashSet<>(List.of("+", "-", "*", "/"));

        for (char c : s.toCharArray()) {
            if (Character.isDigit(c) || c == '.') {
                curr += c;
            } else if (c == '(') {
                stack.push("" + previousOperator); // convert char to string before pushing
                previousOperator = '+';
            } else {
                if (previousOperator == '*' || previousOperator == '/') {
                    stack.push(evaluate(previousOperator, stack.pop(), curr));
                } else {
                    stack.push(evaluate(previousOperator, curr, "0"));
                }

                curr = "";
                previousOperator = c;
                if (c == ')') {
                    List<String> numberList = new ArrayList<>();
                    while (!operators.contains(stack.peek())) {
                        numberList.add(stack.pop());
                    }
                    curr = this.operation.sumList(numberList);
                    previousOperator = stack.pop().charAt(0); // convert string from stack back to char
                }
            }
        }

        return this.operation.sumDeque(stack);
    }
}

