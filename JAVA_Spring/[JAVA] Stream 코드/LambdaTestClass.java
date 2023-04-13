package com.sungchul;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LambdaTestClass {
    public static void main(String[] args) {

        //lambda
        LambdaFunction lambdaFunction = (int a, int b) -> a > b ? a : b;
        System.out.println(lambdaFunction.max(10, 15));

        //Supplier
        Supplier<String> stringSupplier = () -> "Test~~";
        System.out.println(stringSupplier.get());

        //Consumer
        Consumer<String> consumer = (str) -> System.out.println(str.split(" ")[0]);
        consumer.andThen(System.out::println).accept("Hello World");

        //function
        Function<String, Integer> function = str -> str.length();
        System.out.println(function.apply("Hello World"));

        //predicate
        Predicate<String> predicate = (str) -> str.equals("Hello World");
        System.out.println(predicate.test("Hello World"));

    }
}


@FunctionalInterface
interface LambdaFunction {
    int max(int a, int b);

}



