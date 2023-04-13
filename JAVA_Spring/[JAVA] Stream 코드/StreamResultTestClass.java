package com.sungchul;

import javax.xml.transform.sax.SAXSource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StreamResultTestClass {
    public static void main(String[] args) {

        //Min 최소값
        OptionalInt min = IntStream.of(3, 5, 6, 7, 8, 3, 2, 1).min();
        System.out.print("최소값 : ");
        System.out.println(min);

        //Max 최대값
        int max = IntStream.of().max().orElse(0);
        System.out.print("최대값 : ");
        System.out.println(max);

        //Average 평균값
        System.out.print("평균값 : ");
        IntStream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).average().ifPresent(System.out::println);


        //sum 총합
        long sum = IntStream.of(1, 2, 3, 4, 5, 6, 7, 8).sum();
        System.out.print("총합 : ");
        System.out.println(sum);


        //count 갯수
        long count = IntStream.of(1, 2, 3, 4, 5, 6, 7, 8).count();
        System.out.print("갯수 : ");
        System.out.println(count);

        //리스트 생성
        List<TestVO1> test01List = new ArrayList<>();
        List<TestVO1> test02List = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int temp = i + 2;
            if (i % 3 == 0) {
                temp = i + 100;
            }
            TestVO1 test01 = new TestVO1(i, i + "데이터");
            TestVO1 test02 = new TestVO1(i + 2, temp + "데이터");
            test01List.add(test01);
            test02List.add(test02);
        }

        //value 값만 뽑아서 리스트 생성
        List<String> resultValueList = test01List.stream()
                .map(TestVO1::getValue)
                .collect(Collectors.toList());
        System.out.println(resultValueList);
        //seq 값만 뽑아서 리스트 생성
        List<Integer> resultSeqList = test01List.stream()
                .map(TestVO1::getSeq)
                .collect(Collectors.toList());
        System.out.println(resultSeqList);

        //결과값을 하나의 스트링으로 생성
        String listToString01 = test01List.stream()
                .map(TestVO1::getValue)
                .collect(Collectors.joining());
        System.out.println(listToString01);

        //결과값을 하나의 스트링으로 생성하면서, 중간에 공백을 추가함
        String listToString02 = test01List.stream()
                .map(TestVO1::getValue)
                .collect(Collectors.joining(" "));
        System.out.println(listToString02);


        //결과값을 하나의 스트링으로 생성하면서, 중간에 콤마와 시작과 끝지점에  ( ) 를 추가함
        String listToString03 = test01List.stream()
                .map(TestVO1::getValue)
                .collect(Collectors.joining(",", "(", ")"));
        System.out.println(listToString03);

        Double average1 = test01List.stream()
                .collect(Collectors.averagingInt(TestVO1::getSeq));
        System.out.println("### average1 : " + average1);

        Integer sum1 = test01List.stream()
                .collect(Collectors.summingInt(TestVO1::getSeq));
        System.out.println("### sum1 : " + sum1);

        //리스트 생성
        List<TestVO1> test03List = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TestVO1 test01 = new TestVO1(i, i + "데이터" + i);
            TestVO1 test02 = new TestVO1(i, i + "데이터");

            test03List.add(test01);
            test03List.add(test02);
        }

        //seq가 같은 값인 VO끼리 묶음
        Map<Integer, List<TestVO1>> collectorMap1 = test03List.stream()
                .collect(Collectors.groupingBy(TestVO1::getSeq));
        System.out.println("### collectorMap1");
        System.out.println(collectorMap1);

        Map<Boolean, List<TestVO1>> collectorMap2 = test03List.stream()
                .collect(Collectors.partitioningBy(p -> p.getSeq() > 5));
        System.out.println("### collectorMap2");
        System.out.println(collectorMap2);


        List<String> strList = Arrays.asList("Hi", "Hellow", "Kimsungchul");
        boolean anyMatch = strList.stream().anyMatch(name -> name.contains("H"));
        boolean allMatch = strList.stream().allMatch(name -> name.length() > 3);
        boolean noneMatch = strList.stream().noneMatch(name -> name.endsWith("s"));
        System.out.println("### anyMatch : " + anyMatch);
        System.out.println("### allMatch : " + allMatch);
        System.out.println("### noneMatch : " + noneMatch);

        strList.stream().forEach(System.out::println);
        test03List.stream().forEach(TestVO1::getSeq);


    }
}

class TestVO1 {

    int seq;
    String value;

    public TestVO1(int seq, String value) {
        this.seq = seq;
        this.value = value;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "TestVO{" +
                "seq=" + seq +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        TestVO t = (TestVO) o;
        if (this.value.equals(t.getValue())) {
            return true;
        } else {
            return false;
        }
    }
}