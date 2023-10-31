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

    public String plusMonths(int monthsToAdd) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMM");
        return now.plusMonths(monthsToAdd).format(dateTimeFormatter);
    }

    public String addWeek(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMM");
        return now.format(dateTimeFormatter);
    }


//    /**
//     * 입력받은 날짜의 요일을 구함
//     * @param day yyyy-MM-dd 형식으로 입력
//     * @return result , 1부터 시작하며 1은 월요일 7은 일요일
//     * */
//    public int  getDayOfWeek(String day){
//        String[] splitString = day.split("-");
//        LocalDate now = LocalDate.of(
//                Integer.parseInt(splitString[0])
//                ,Integer.parseInt(splitString[1])
//                ,Integer.parseInt(splitString[2]));
//
//        return now.getDayOfWeek().getValue();
//    }

    /**
     * 입력받은 날짜의 요일을 구함
     * @param day yyyy-MM-dd 형식으로 입력
     * @return result , 1부터 시작하며 1은 월요일 7은 일요일
     */
    public int getDayOfWeek(String day) {
        LocalDate date = LocalDate.parse(day);
        return date.getDayOfWeek().getValue();
    }


//    /**
//     * 현재 날짜가 입력받은 날짜보다 이전인지 구함
//     * @param day yyyy-MM-dd 형식으로 입력
//     * @return Boolean , true 이전임 , false 지났음
//     * */
//    public boolean dayEqual(String day){
//        String[] splitString = day.split("-");
//        LocalDate getDate = LocalDate.of(
//                Integer.parseInt(splitString[0])
//                ,Integer.parseInt(splitString[1])
//                ,Integer.parseInt(splitString[2]));
//        LocalDate now = LocalDate.now();
//        return now.isBefore(getDate);
//
//    }
    /**
     * 현재 날짜가 입력받은 날짜보다 이전인지 구함
     * @param inputDate yyyy-MM-dd 형식으로 입력
     * @return Boolean , true 이전임 , false 지났음
     */
    public boolean isDayBeforeNow(String inputDate) {
        LocalDate getDate = LocalDate.parse(inputDate);
        LocalDate now = LocalDate.now();
        return getDate.isAfter(now);
    }



    /**
     * 이번달과 다음달의 매주 토요일,일요일 날짜를 리턴
     * @return ArrayList<HashMap<String,String>>  ,
     * */
//    public ArrayList<HashMap<String,String>> getSaturdays(int addMonths){
//        ArrayList<HashMap<String,String>> saturdays = new ArrayList<>();
//        LocalDateTime now = LocalDateTime.now();
//        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//        //다음달 마지막날
//        String nextMonthLastDay = now.with(TemporalAdjusters.lastDayOfMonth()).plusMonths(addMonths).format(dateTimeFormatter);
//        for(int i=0;;i++){
//
//            if(Integer.parseInt(nextMonthLastDay) < Integer.parseInt(now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).plusWeeks(i).format(dateTimeFormatter))){
//                break;
//            }else{
//                HashMap<String,String> map = new HashMap<>();
////                map.put("saturday",now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).plusWeeks(i).format(dateTimeFormatter));
////                map.put("sunday",now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).plusWeeks(i).plusDays(1).format(dateTimeFormatter));
//                map.put("saturday",now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).plusWeeks(i).format(dateTimeFormatter));
//                map.put("sunday",now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).plusWeeks(i).plusDays(1).format(dateTimeFormatter));
//                map.put("today",now.format(dateTimeFormatter));
//                saturdays.add(map);
//            }
//
//        }
//        return saturdays;
//    }

    /**
     * 이번달과 다음달의 매주 토요일,일요일 날짜를 리턴
     * @param addMonths 추가할 개월 수
     * @return ArrayList<HashMap<String,String>>
     */
    public ArrayList<HashMap<String,String>> getSaturdays(int addMonths){
        ArrayList<HashMap<String,String>> saturdays = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        for(int i=0;;i++){
            LocalDateTime saturday = now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).plusWeeks(i);
            String saturdayStr = saturday.format(dateTimeFormatter);
            if(Integer.parseInt(saturdayStr) > Integer.parseInt(now.with(TemporalAdjusters.lastDayOfMonth()).plusMonths(addMonths).format(dateTimeFormatter))){
                break;
            }else{
                HashMap<String,String> map = new HashMap<>();
                LocalDateTime sunday = saturday.plusDays(1);
                String sundayStr = sunday.format(dateTimeFormatter);
                map.put("saturday", saturdayStr);
                map.put("sunday", sundayStr);
                map.put("today", now.format(dateTimeFormatter));
                saturdays.add(map);
            }
        }
        return saturdays;
    }

    /**
     * 입력받은 날짜와 포맷을, 원하는 포맷으로 변경
     * @param date 입력받은 날짜
     * @param nowFormat 입력받은 날짜의 데이터 포맷
     * @param changeFormat 변경할 데이터 포맷
     * @return String 변경된 데이터 포맷으로 리턴
     * */
    public static String changeDateFormat(String date, String nowFormat, String changeFormat) {
        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern(nowFormat);
        LocalDateTime dateTime = LocalDateTime.parse(date, dtFormatter);

        DateTimeFormatter newDtFormatter = DateTimeFormatter.ofPattern(changeFormat);
        return dateTime.format(newDtFormatter);
    }
}
