package com.sungchul.camping.common;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;


public class DateUtil {
    /*yyyymmdd 로 현재 날짜 리턴*/
    public String getDate() {

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return now.format(dateTimeFormatter);
    }

    /*yyyy 로 현재 연도 리턴*/
    public String getYear() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy");
        return now.format(dateTimeFormatter);
    }
    /*HHmmss 로 현재 시간 리턴*/
    public String getTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HHmmss");
        return now.format(dateTimeFormatter);
    }
    /*지정한 형식으로 출력*/
    public String getTime(String strformat){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(strformat);
        return now.format(dateTimeFormatter);
    }

    public String getMonth(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMM");
        return now.format(dateTimeFormatter);
    }

    public String addMonth(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMM");
        return now.format(dateTimeFormatter);
    }
    public String addWeek(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMM");
        return now.format(dateTimeFormatter);
    }


    /**
     * 입력받은 날짜의 요일을 구함
     * @param day yyyy-MM-dd 형식으로 입력
     * @return result , 1부터 시작하며 1은 월요일 7은 일요일
     * */
    public int  getDayOfWeek(String day){
        String[] splitString = day.split("-");
        LocalDate now = LocalDate.of(
                Integer.parseInt(splitString[0])
                ,Integer.parseInt(splitString[1])
                ,Integer.parseInt(splitString[2]));

        return now.getDayOfWeek().getValue();
    }

    /**
     * 현재 날짜가 입력받은 날짜보다 이전인지 구함
     * @param day yyyy-MM-dd 형식으로 입력
     * @return Boolean , true 이전임 , false 지났음
     * */
    public boolean dayEqual(String day){
        String[] splitString = day.split("-");
        LocalDate getDate = LocalDate.of(
                Integer.parseInt(splitString[0])
                ,Integer.parseInt(splitString[1])
                ,Integer.parseInt(splitString[2]));
        LocalDate now = LocalDate.now();
        return now.isBefore(getDate);

    }

    /**
     * 이번달과 다음달의 매주 토요일,일요일 날짜를 리턴
     * @return ArrayList<HashMap<String,String>>  ,
     * */
    public ArrayList<HashMap<String,String>> getSaturdays(int addMonths){
        ArrayList<HashMap<String,String>> saturdays = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        //다음달 마지막날
        String nextMonthLastDay = now.with(TemporalAdjusters.lastDayOfMonth()).plusMonths(addMonths).format(dateTimeFormatter);
        for(int i=0;;i++){

            if(Integer.parseInt(nextMonthLastDay) < Integer.parseInt(now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).plusWeeks(i).format(dateTimeFormatter))){
                break;
            }else{
                HashMap<String,String> map = new HashMap<>();
//                map.put("saturday",now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).plusWeeks(i).format(dateTimeFormatter));
//                map.put("sunday",now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).plusWeeks(i).plusDays(1).format(dateTimeFormatter));
                map.put("saturday",now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).plusWeeks(i).format(dateTimeFormatter));
                map.put("sunday",now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).plusWeeks(i).plusDays(1).format(dateTimeFormatter));
                map.put("today",now.format(dateTimeFormatter));
                saturdays.add(map);
            }

        }
        return saturdays;
    }


}
