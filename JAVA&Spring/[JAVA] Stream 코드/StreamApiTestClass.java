package com.sungchul;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class StreamApiTestClass {
    public static void main(String []args){
        //스트림 생성
        List<String> list = Arrays.asList("A","B","C","D","E");
        Stream<String> stream0 = list.stream();
        Stream<String> stream1 = Stream.of("A","B","C","D","E");
        Stream<String> stream2 = Stream.of(new String []{"A","B","C","D","E"});
        Stream<String> stream3 = Arrays.stream(new String[]{"A","B","C","D","E"});
        Stream<String> stream4 = Arrays.stream(new String[]{"A","B","C","D","E"},0,4);

        //원시 스트림 생성 ,int,long,double 가능
        IntStream intStream = IntStream.range(10,100);
        LongStream longStream = LongStream.range(10,100);

        //stream 가공, Filter 필터링
        Stream<String> filterStream = list.stream()
                .filter(name ->name.contains("A"));
        filterStream.forEach(System.out::println);


        //stream 가공, Map 데이터변환
        Stream<String> mapStream = list.stream()
                .map(s ->s.toLowerCase());
        mapStream.forEach(System.out::println);

        //stream 가공 , Sorted 정렬
        List<String> sortList = Arrays.asList("AAAAA" , "ZZZZZ" , "EEEEE" , "QQQQQ" , "CCCCC");
        Stream<String> sortStream1= sortList.stream()
                .sorted();
        sortStream1.forEach(System.out::println);

        Stream<String> sortStream2= sortList.stream()
                .sorted(Comparator.reverseOrder());
        sortStream2.forEach(System.out::println);

        //stream 가공 , Distinct 중복제거
        List<String> distinctList = Arrays.asList("AA","BB","CC","AA","CC","DD");
        Stream<String> distinctStream = distinctList.stream()
                .distinct();
        distinctStream.forEach(System.out::println);

        //stream 가공 , Peek 특정 연산하기
        int sum = IntStream.of(1,2,3,4,5,6,7,8,9,10)
                .peek(System.out::println)
                .sum();
        System.out.println(sum);

        //stream 가공 , Stream < - > 원시 Stream 변환
        Stream<String> changeStream1 = IntStream.range(1,10)
                .mapToObj(i->"NUMBER : "+i);
        changeStream1.forEach(System.out::println);

        Stream<String> changeStream2 = Stream.of(1.0,2.0,3.0)
                .mapToInt(Double::intValue)
                .mapToObj(i->"NUMBER : " + i);
        changeStream2.forEach(System.out::println);


        //stream0.forEach(System.out::println);
        //stream0.forEach(System.out::println);
        //stream4.forEach(System.out::println);


    }

}
