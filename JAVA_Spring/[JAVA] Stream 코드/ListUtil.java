package com.sungchul.util;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListUtil<T> {
    /**
     * 리스트 내에 일치하는 값 리턴
     * @param baseList 원본 리스트
     * @param targetList 비교 대상 리스트
     * @return List<Object>
     * */
    public List<Object> noneMatchList(List<T> baseList , List<T> targetList){
        List<Object> returnList = baseList.stream()
                .filter(old -> targetList.stream().noneMatch(Predicate.isEqual(old)))
                .collect(Collectors.toList());
        return returnList;
    }

    /**
     * 리스트 내에 일치하지 않는 값 리턴
     * @param baseList 원본 리스트
     * @param targetList 비교 대상 리스트
     * @return List<Object>
     * */
    public List<Object> matchList(List<T> baseList , List<T> targetList){
        List<Object> returnList = baseList.stream()
                .filter(old -> targetList.stream().anyMatch(Predicate.isEqual(old)))
                .collect(Collectors.toList());
        return returnList;
    }
}
