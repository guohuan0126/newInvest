package com.jiuyi.test;


import com.google.common.collect.Lists;
import com.jiuyi.ndr.BatchApplication;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;


@SpringBootTest(classes = BatchApplication.class)
public class ArithmeticTest {

   /* 题目：判断101-200之间有多少个素数，并输出所有素数。*/
    @Test
    public void test1(){
        List<Integer> list = Lists.newArrayList();
        int count = 0;
        for (int i = 101 ; i<= 200 ; i++ ){
            if (a(i)){
                count++;
                list.add(i);
            }
        }
        System.out.println("素数的个数为-"+count+"-分别为-"+list.toString());

    }

    public static boolean a(int a){
        int z = 0 ;
        for (int i = 1 ; i <=a ; i++ ){
            if (a%i==0) {
                z++;
            }
        }
        if (z> 2 ){
            return false;
        }
        return true;
    }

    /*打印出所有的 水仙花数 ，所谓 水仙花数 是指一个三位数，其各位数字立方和等于该数本身。例如：153是一个 水仙花数 ，因为153=1的三次方＋5的三次方＋3的三次方。
            1.程序分析：利用for循环控制100-999个数，每个数分解出个位，十位，百位。*/
    @Test
    public void test2(){
        for (int i = 100 ; i <= 1000 ; i ++){
            int a = b(i);
            if (a!=0){
                System.out.println(a);
            }
        }
    }

    public static int b(int b){
        int x ;
        int y ;
        int z ;

        x = b/100;
        y = (b%100)/10;
        z = b%10;

        if (b == x*x*x + y*y*y + z*z*z){
            return b;
        }
        return 0;
    }
    @Test
    public void test3(){
        int[] array = {1, 15 , 30 ,18 ,2 ,6 ,48};
        insertSort(array);

    }


    public static void insertSort(int[] array){

        for (int i = 1 ; i < array.length ; i++){
            int temp = array[i];

            int j = i-1 ;
            for ( ; j >= 0 && array[j] > temp ; j--){
                array[j+1] = array[j];
            }
            array[j+1] = temp;
        }
        System.out.println(Arrays.toString(array));
    }
}

