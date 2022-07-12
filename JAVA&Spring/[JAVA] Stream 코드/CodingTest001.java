package com.sungchul;

public class CodingTest001 {
    public static void main(String[]args){

        long z = 40000000;

        long  h=0;
        long  m=0;
        long  s=0;
        z = z/1000;
        h=z/60/60;
        m=z/60%60;
        s=z%60;
        System.out.println("### z : " + z);
        System.out.println("### h : " + h);
        System.out.println("### m : " + m);
        System.out.println("### s : " + s);

        int a[]={44, 1, 0, 0, 31, 25};
        int []aa = {31, 10, 45, 1, 6, 19};
        solution(a,aa);

        int []b= {0, 0, 0, 0, 0, 0};
        int []bb= {38, 19, 20, 40, 15, 25};
        solution(b,bb);

        int []c = {45, 4, 35, 20, 3, 9};
        int [] cc= {20, 9, 3, 45, 4, 35};
        solution(c,cc);
    }
    public static int[] solution(int[] lottos, int[] win_nums) {
        int h=0;
        int l=0;
        int count=0;
        for(int i=0;i<lottos.length;i++){
            for(int j=0;j<win_nums.length;j++){
                if(lottos[i]==win_nums[j]){

                    l++;
                    h++;
                }
            }
            if(lottos[i]==0){
                count++;
            }
        }
        h +=count;
        System.out.println(h);
        System.out.println(count);

        int[] answer = {aa(h),aa(l)};

        return answer;
    }
    public static int aa(int g){
        if(g==6){
            return 1;
        }else if(g==5){
            return 2;
        }else if(g==4){
            return 3;
        }else if(g==3){
            return 4;
        }else if(g==2){
            return 5;
        }else{
            return 6;
        }

    }
}
