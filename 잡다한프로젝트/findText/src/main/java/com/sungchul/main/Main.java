package com.sungchul.main;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[]args){
        getTalk();
    }


    public static int getTalk(){
        File txt = new File("t.txt");
        BufferedReader br = null;
        int count = 0;
        StringBuffer stringBuffer = new StringBuffer();
        String line = null;
        String killUser=null;
        //채팅에 참여한 사용자 목록
        String memberName;
        ArrayList<String> memberList = new ArrayList<String>();
        //사용자명,채팅횟수 기록
        HashMap<String,Integer> talkCountMap = new HashMap<String, Integer>();
        HashMap<String,Integer> killCountMap = new HashMap<String, Integer>();
        HashMap<String,String> killUserListMap = new HashMap<String, String>();

        int totalLine=0;

        int memberCount=0;
        try {
            //사용자 목록 추출
            br = new BufferedReader(new FileReader(txt));
            while((line=br.readLine()) != null) {
                int t=line.indexOf("]");
                if(t>0){
                    memberName = line.substring(0,t+1);

                    for(String name : memberList){
                        if(name.equals(memberName)){
                            memberCount++;
                        }
                    }
                    if(memberCount==0){
                        memberList.add(line.substring(0,t+1));
                    }
                    memberCount=0;
                }
            }

            //사용자세팅
            for(int i=0;i<memberList.size();i++){
                talkCountMap.put(memberList.get(i),0);
                killCountMap.put(memberList.get(i),0);
                killUserListMap.put(memberList.get(i),"");
            }
            br = new BufferedReader(new FileReader(txt));


            //대화횟수저장
            while((line=br.readLine()) != null) {

                if(line.indexOf("나갔습니다")>=0){
                    killCountMap.put(killUser,killCountMap.get(killUser)+1);
                    int a=line.indexOf("님이");
                    if(killUserListMap.get(killUser).length()>0){
                        killUserListMap.put(killUser, killUserListMap.get(killUser) +" ,"  +  line.substring(0,a+1));
                    }else{
                        killUserListMap.put(killUser,killUserListMap.get(killUser) + line.substring(0,a+1));
                    }

                }

                for(int i=0;i<memberList.size();i++){
                    if(line.indexOf(memberList.get(i))>=0){
                        talkCountMap.put(memberList.get(i),talkCountMap.get(memberList.get(i))+1);
                        totalLine++;
                        killUser = memberList.get(i);
                    }
                }
            }
            //가나다순으로 memberList 정렬
            memberList.sort(Comparator.naturalOrder());

            //대화내용 순으로 talkCount 정렬
            List<Map.Entry<String, Integer>> tokeList = new LinkedList<>(talkCountMap.entrySet());
            tokeList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            //사용자를 쫓아낸 순으로 killCount 정렬
            List<Map.Entry<String, Integer>> killList = new LinkedList<>(killCountMap.entrySet());
            killList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

//            for(int i=0;i<memberList.size();i++){
//                System.out.println("### " + memberList.get(i) + " : " + talkCount.get(memberList.get(i)));
//            }

            int rank=1;
            double jibun=0;
            System.out.println("### 사용자를 쫓아낸 순위 ###");
            for(Map.Entry<String, Integer> entry : killList){
                if(entry.getValue()>0) {
                    System.out.println(rank + "위. " + entry.getKey() + " : " + entry.getValue());
                    System.out.println("내보낸 사용자 목록 : " + killUserListMap.get(entry.getKey()));
                }
                rank++;
            }


            System.out.println();
            rank =1;
            System.out.println("### 말 많은 순위 ###");
            for(Map.Entry<String, Integer> entry : tokeList){
                System.out.println(rank +"위. " + entry.getKey() + " : " + entry.getValue() + " ("+getPercent(totalLine,entry.getValue()) + "%)");
                if(rank<=5){
                    jibun = jibun+getPercent(totalLine,entry.getValue());
                }
                rank++;
            }
            //System.out.println(stringBuffer);
            //System.out.println("### 아 : " + countA);
            System.out.println("### 총 채팅 횟수 : " + totalLine);
            System.out.println("### 1위부터 5위까지의 채팅 지분율 : " + jibun +"%");
            System.out.println("### 대화를 한 사용자 수: " + memberList.size());
            System.out.println("### 대화를 한 사용자 목록: " + memberList);
        } catch (FileNotFoundException e) {
           e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return count;
    }

    public static double getPercent(double totalLine , double talkCount){



        return Math.round(talkCount/totalLine*1000000)/10000.0;
    }
}
