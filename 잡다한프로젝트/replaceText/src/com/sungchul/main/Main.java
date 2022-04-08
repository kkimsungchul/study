package com.sungchul.main;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        StringBuffer sb = new StringBuffer();
        int textLength = 0;

        while (true){

            System.out.print("#### 문자열을 입력해주세요 : ");
            String str = scanner.nextLine();
            String beforeReplaceText[] = {"영","일","이","삼","사","오","육","칠","팔","구","십"};
            String afterReplaceText[] = {"0","1","2","3","4","5","6","7","8","9","10"};

            System.out.println("# 입력한 문자열 : " + str);
            str = str.trim();
            str = str.replaceAll("\\s" , "");

            for(int i=0;i<beforeReplaceText.length;i++){
                str = str.replaceAll(beforeReplaceText[i],afterReplaceText[i]);
            }
            textLength = str.length();

            sb.append(str);

            for(int i=textLength-1;i>0;i--){
                sb.insert(i," ");
            }
            System.out.println("# 변경한 문자열 : "+sb);
            System.out.println();
            System.out.println();
            sb.delete(0,sb.length());


        }





    }
}
