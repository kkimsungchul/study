package com.sungchul;

import java.io.*;
import java.util.*;


public class OptionalTestClass {

    public static void main(String[] args) throws NumberFormatException, IOException {
        ClassName<String,Integer> className = new ClassName<>();


        //빈 객체 생성
        Optional<ClassName> optionalClassNameTest1 = Optional.empty();

        //값에 null이 오면 빈객체가, null 이 아니면 지정한 값이 들어간 객체가 생성
        Optional<ClassName> optionalClassNameTest3 = Optional.ofNullable(className);

        //값에 null이 오면 NullPointerException 발생
        //Optional<String> optionalClassNameTest2 = Optional.of(className.getTest1());


        //orElse , orElseGet 테스트
        //orElse , orElseGet 의 차이점은 인자로 값을 받는냐, 함수를 받느냐의 차이
        className.set("문자열",12345);
        className.setTest1("김성철");
        Optional<ClassName> optionalClassNameTest4 = Optional.ofNullable(className);
        //orElse 는 값이 없다면 다음괄호 안에 있는 값을 사용한다
        String test = optionalClassNameTest4.map(ClassName::getTest1).orElse("값없음");
        System.out.println("### test : " + test);


        //아래와같이 ifPresent 함수를 사용하여서 null일경우 값을 넣어줄수 있다
        // null 출력
        Optional.of(className)
                .map(ClassName::getTest2)
                .ifPresent(n-> className.setTest2(n));
        System.out.println("### Null test2 : "+ className.getTest2());

        //test1의 값을 test2에 입력
        Optional.of(className)
                .map(ClassName::getTest1)
                .ifPresent(n-> className.setTest2(n));
        System.out.println("### Not null test2 : "+ className.getTest2());

        ClassName<String,String> a = new ClassName<>();
        a.setTest2("aa");
        System.out.println(Optional.ofNullable(a.getTest1()).orElse("비었음"));
        System.out.println(Optional.ofNullable(a.getTest2()).orElse("비었음"));


        String [] checkMail ={"kt.com" , "ktfriend.com" , "ktcs.co.kr"};

        List<String> ba = Arrays.asList("");
        System.out.println(ba);

        for(String bb :ba){
            System.out.println("kt.com".contains(bb));
        }


    }
}

class ClassName<K,V> {
    private K first;
    private V second;
    private String test1;
    private String test2;

    void set(K first , V second){
        this.first=first;
        this.second=second;
    }

    K getFirst(){
        return first;
    }

    V getSecond(){
        return second;
    }

    void setTest1(String test1){
        this.test1 = test1;
    }

    String getTest1(){
        return test1;
    }

    public String getTest2() {
        return test2;
    }

    public void setTest2(String test2) {
        this.test2 = test2;
    }

    String toLowerCase(){

        return "소문자변환";
    }
}