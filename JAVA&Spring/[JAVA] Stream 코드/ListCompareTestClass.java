package com.sungchul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListCompareTestClass {
    public static void main(String[]args) {

        //리스트 생성
        List<TestVO> newList = new ArrayList<>();
        List<TestVO> oldList = new ArrayList<>();
        List<HashMap<String,Object>> mapList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int temp = i+2;
            if(i%3==0){
                temp=i+100;
            }
            TestVO oldTestVO = new TestVO(i, i + "데이터");
            TestVO newTestVO = new TestVO(i + 2, temp + "데이터");
            HashMap<String,Object> tempMap = new HashMap<>();
            tempMap.put("seq",i+2);
            tempMap.put("value",i+"데이터");
            oldList.add(oldTestVO);
            newList.add(newTestVO);
            mapList.add(tempMap);
        }
        Stream.of(oldList).forEach(System.out::println);
        Stream.of(newList).forEach(System.out::println);


        //seq만을 가져와서 비교, oldList만 존재하는 데이터
        List<TestVO> noneMatchList = oldList.stream()
                .filter(n -> newList.stream()
                        .noneMatch(o -> n.getSeq() == o.getSeq()))
                .collect(Collectors.toList());

        //seq만을 가져와서 비교, oldList만 존재하는 데이터
        List<TestVO> anyMatchList = newList.stream()
                .filter(o -> oldList.stream()
                        .noneMatch(n -> o.getSeq() == n.getSeq()))
                .collect(Collectors.toList());

        System.out.println();
        System.out.println("삭제해야할 기존 데이터");
        for (TestVO t1 : noneMatchList) {
            System.out.println(t1);
        }
        System.out.println();
        System.out.println("생성해야할 신규 데이터");
        for (TestVO t2 : anyMatchList) {
            System.out.println(t2);
        }

        //seq(키값)이 일치하는 애들만 조회
        List<TestVO> tempList = newList.stream()
                .filter(o -> oldList.stream()
                        .anyMatch(n -> o.getSeq() == n.getSeq()))
                .collect(Collectors.toList());

        System.out.println();
        System.out.println("seq 일치하는 데이터");
        for(TestVO t3 : tempList){
            System.out.println(t3);
        }

        //seq가 일치하는 데이터중 value가 일치하지 않는 데이터만 필터
        List<TestVO> notCompareList = tempList.stream()
                                      .filter(old -> oldList.stream().noneMatch(Predicate.isEqual(old)))
                                      .collect(Collectors.toList());

        System.out.println();
        System.out.println("seq는 일치하면서 , value는 일치하지 않는 데이터(수정해야 할 기존 데이터)");
        for(TestVO t4 : notCompareList){
            System.out.println(t4);
        }

        //seq와 value가 둘다 일치하는 데이터
        List<TestVO> compareList = tempList.stream()
                .filter(old -> oldList.stream().anyMatch(Predicate.isEqual(old)))
                .collect(Collectors.toList());
        System.out.println();
        System.out.println("seq도 일치하고, value도 일치하는 데이터");
        for(TestVO t5 : compareList){
            System.out.println(t5);
        }

        List<?> addList = Stream.of(oldList,mapList)
                                .flatMap(x->x.stream())
                                .collect(Collectors.toList());

        System.out.println(addList);


        System.out.println("#######################################################");
        System.out.println(oldList.get(1));
        System.out.println(mapList.get(1));




    }
}

class TestVO{

    int seq;
    String value;
    boolean status;

    public TestVO(int seq , String value){
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

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TestVO{" +
                "seq=" + seq +
                ", value=" + value  +
                ", status=" + status +
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
