package com.sungchul;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamTest {

    public static void main(String[] args) {

        //wordProcessTest();

        String[] nameArray = {"kimsungchul", "yanghyeuny", "egiljung","1"};
        List<String> nameList = Arrays.asList(nameArray);


        // 별도의 스트림을 생성하여서 정렬
        Stream<String> nameStream = nameList.stream();
        Stream<String> arrayStream = Arrays.stream(nameArray);


        //스트림 사용
        // 복사한 데이터를 출력
        System.out.println("=============================");
        nameStream.sorted().forEach(System.out::println);
        System.out.println("=============================");
        arrayStream.sorted().forEach(System.out::println);
        // 스트림은 한번 사용하고 닫히므로 한번 더 호출할 경우 오류 발생
        //arrayStream.sorted().forEach(System.out::println);

        System.out.println("=============================");
        //스트림 미사용 (기존방식 ,for , 향상된 for)
        //배열 정렬
        Arrays.sort(nameArray);
        //이름 리스트 정렬
        Collections.sort(nameList);
        //아래 두개의 방식으로 출력
        for(String s : nameArray){
            System.out.println(s);
        }
        System.out.println("=============================");
        for(int i=0;i<nameList.size();i++){
            System.out.println(nameList.get(i));
        }

        System.out.println("=============================");
        List<String> strList = Arrays.asList("A3" , "B2" , "A1" , "B3" , "A2" , "B1", "C3" , "C2" , "D1");
        strList
                .stream()                               //스트림 생성
                .filter(s ->s.startsWith("C"))          //C 로시작하는애들만 필터
                .map(String::toLowerCase)               //필터된 문자에 대해서 소문자로 변환
                .sorted()                               //정렬
                .forEach(System.out::println);          //출력
        System.out.println("=============================");
        for(String a : strList){
            System.out.println("# a : " + a);
        }
        Stream<String> strStream = strList.stream();
        strStream.forEach(System.out::println);

    }

    static void wordProcessTest() {
        final List<String> words = Arrays.asList("TONY", "a", "hULK", "B", "america", "X", "nebula", "Korea");
        String result = words.stream()
                .filter(w -> w.length() > 1)
                .map(String::toUpperCase)
                .map(w -> w.substring(0, 1))
                .collect(Collectors.joining(" "));

        System.out.println("## result : " + result);
    }

}