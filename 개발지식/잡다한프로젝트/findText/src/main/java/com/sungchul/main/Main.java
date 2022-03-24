package com.sungchul.main;

import java.io.*;
import java.nio.charset.Charset;
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
        //채팅에 참여한 사용자 목록
        String memberName;
        ArrayList<String> memberList = new ArrayList<String>();
        //사용자명,채팅횟수 기록
        HashMap<String,Integer> talkCountMap = new HashMap<String, Integer>();

        int totalLine=0;

        int memberCount=0;
        try {
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



            for(int i=0;i<memberList.size();i++){
                talkCountMap.put(memberList.get(i),0);
            }
            br = new BufferedReader(new FileReader(txt));

            while((line=br.readLine()) != null) {

                for(int i=0;i<memberList.size();i++){
                    if(line.indexOf(memberList.get(i))>=0){
                        talkCountMap.put(memberList.get(i),talkCountMap.get(memberList.get(i))+1);
                        totalLine++;
                    }
                }
            }
            //가나다순으로 memberList 정렬
            memberList.sort(Comparator.naturalOrder());

            //대화내용 순으로 talkCount 정렬
            List<Map.Entry<String, Integer>> entryList = new LinkedList<>(talkCountMap.entrySet());
            entryList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

//            for(int i=0;i<memberList.size();i++){
//                System.out.println("### " + memberList.get(i) + " : " + talkCount.get(memberList.get(i)));
//            }

            int rank=1;

            System.out.println("### 말 많은 순위 ###");
            for(Map.Entry<String, Integer> entry : entryList){
                System.out.println(rank +"위. " + entry.getKey() + " : " + entry.getValue() + " ("+getPercent(totalLine,entry.getValue()) + "%)");
                rank++;
            }
            //System.out.println(stringBuffer);
            //System.out.println("### 아 : " + countA);
            System.out.println("### 총 채팅 횟수 : " + totalLine);
            System.out.println("### 대화를 한 사용자 목록: " + memberList);
            System.out.println("### 대화를 한 사용자 수: " + memberList.size());
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
