package com.jxhun.service;

import com.jxhun.controller.DronesDataController;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Jxhun
 * Date: 2019/05/08
 * Description:
 * Version: V1.0
 */
@Service
public class DronesDataServvice {


    /**
     * 接收到传递过来的无人机id，移动的X,Y,Z坐标值，首先判断这个id对应的文件是否存在
     * 如果文件不存在那么说明这个时候无人机是第一次开启，那么写入行头和第一条数据
     * 如果存在那说明不是第一次，那么就去读取文本文档每行的数据封装到一个list，取最后一条信息
     * 判断上一条信息是否故障，得到上一个序号，这条信息序号+1
     * 如果传入的无人机id和上一条信息的无人机id不匹配，那么我们就判断这个无人机发生故障
     * 如果传入的无人机id或者坐标值不是相应的格式，那么我判断这个无人机发生故障
     * @param plane 无人机id
     * @param X  移动的X坐标
     * @param Y 移动的Y坐标
     * @param Z 移动的Z坐标
     * @return 成功返回true，失败返回false
     */
    public boolean drones(String plane, String X, String Y, String Z) {
        // 这个正则用来判断无人机id是否为字母和数字组成，如果不为那就为false
        boolean planeAction = plane.matches("[0-9]+[a-zA-Z]+[0-9a-zA-Z]*|[a-zA-Z]+[0-9]+[0-9a-zA-Z]*");
        try {
            File file = new File("C:" + File.separator + "test" +
                    File.separator + plane + "Data.txt"); // 拿到File对象
            FileOutputStream fos;
            System.out.println("!file.exists() == " + !file.exists());
            if (!file.exists()) {  // 如果文件不存在，说明无人机第一次进来
                fos = new FileOutputStream(file, true);  // 字节写入流,追加
                String str = "消息序号\t消息\t\t\t\t当前坐标\t\t状态\r\n";
                String str2;
                if (planeAction) {  // 如果id格式正确
                    str2 = "0\t\t" + plane + " 1 1 1\t\t\t（1，1，1）\t\t正常\r\n";
                } else {  // 如果id格式不正确，那么直接判断无人机发生故障
                    str2 = "0\t\t" + plane + " 1 1 1\t\t\t（NA,NA,NA）\t\t故障\r\n";
                }
                fos.write(str.getBytes());
                fos.write(str2.getBytes());
                fos.close();
                return true;
            } else {
                fos = new FileOutputStream(file, true);  // 字节写入流,追加
                ArrayList<String> list = out(file);
                System.out.println(list);
                String lastElement = list.get(list.size() - 1); // 的到最后一个元素
                // 通过得到list最后一个元素取出序号，序号为第一个\t前面的数据，为正整数，所以可以直接转换为int值
                int xu = Integer.parseInt(lastElement.substring(0, lastElement.indexOf("\t")));
                // 传入数据进行正则判断，判断如果为整数返回true
                boolean action = match("^(0|[1-9][0-9]*|-[1-9][0-9]*)$", X, Y, Z);
                // 得到上一次的坐标
                String coordinates = lastElement.substring(lastElement.indexOf('（') + 1, lastElement.indexOf('）'));
                int[] coor = new int[3];  // 这个数组用来装上一个坐标
                if (!lastElement.contains("故障")) {  // 如果上一组不是故障
                    coor = conversion(coordinates);  // 得到上一个坐标数组
                }
                // 得到以前的无人机id
                String yPlane = lastElement.substring(lastElement.indexOf("\t\t") + 2, lastElement.indexOf(" "));
                String str2;
                System.out.println("判断条件" + action + !lastElement.contains("故障") + yPlane.equals(plane));
                // 如果传入的数据格式正确并且上一条信息不存在故障并且传入的id和前面的id匹配
                if (action && !lastElement.contains("故障") && yPlane.equals(plane)) {
                    str2 = xu + 1 + "\t\t" + plane + " " + coor[0] + " " + coor[1] + " " + coor[2] + " " + X + " " + Y + " " + Z +
                            "\t\t（" + (coor[0] + Integer.parseInt(X)) + "，" + (coor[1] + Integer.parseInt(Y)) + "，" +
                            (coor[2] + Integer.parseInt(Z)) + "）\t\t正常\r\n";
                } else {
                    if (lastElement.contains("故障")) {  // 如果是因为前条是故障，那么固定格式,这里注意id用前面的id
                        str2 = xu + 1 + "\t\t" + yPlane + " 1 1 1 " + X + " " + Y + " " + Z + "\t\t（NA，NA，NA）\t\t故障\r\n";
                    } else {  // 如果是因为传来的数据有问题，那么就是错误
                        str2 = xu + 1 + "\t\t" + yPlane + " " + coor[0] + " " + coor[1] + " " + coor[2] + "\t\t\t（NA，NA，NA）\t\t故障\r\n";
                    }
                }
                fos.write(str2.getBytes()); // 写入
                fos.close();
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param regex 正则表达式字符串
     * @param str   要匹配的字符串
     * @return 如果str 符合 regex的正则表达式格式,返回true, 否则返回 false;
     */
    private boolean match(String regex, String... str) {
        Pattern pattern = Pattern.compile(regex);
        boolean aciton = true;  // 给个状态机，默认为true
        for (String s : str) {  // 遍历取出
            Matcher matcher = pattern.matcher(s);   // 得到Matcher对象
            if (!matcher.matches()) {    // 如果有一个参数判断不过，就直接将状态机赋值为false
                aciton = false;
            }
        }
        return aciton;  // 返回这个状态机，如果比对不通过就是false，如果通过就是true
    }

    /**
     * 这个方法用来转换字符换为int型数组，主要转换上一个坐标
     *
     * @param coordinates 上一个坐标
     * @return 返回转换后的int数组
     */
    private int[] conversion(String coordinates) {
        String[] arrString = coordinates.split("，");  // 拆分为String数组
        int[] arrInt = new int[3];   // 定义一个int数组来存放坐标
        for (int i = 0; i < arrString.length; i++) {
            arrInt[i] = Integer.parseInt(arrString[i]);  // 转换为int类型数据存入int数组中
        }
        return arrInt;  // 返回结果
    }

    /**
     * 这个方法将文本文档中的数据按行取出存入list
     *
     * @param file 文本文档对应的File对象
     * @return 返回封装好的list
     * @throws IOException 在此过程中可能会抛出IO异常，调用此方法时处理
     */
    public ArrayList<String> out(File file) throws IOException {
        ArrayList<String> list = new ArrayList<>(); // 使用list来存储读取到的数据
        FileReader fr = new FileReader(file); // 字符流读取文件
        BufferedReader bf = new BufferedReader(fr);
        String str;
        // 按行读取字符串,readLine读取一个文本行
        while ((str = bf.readLine()) != null) {
            list.add(str);  // 读取得到的字符串存入list，一行一个元素
        }
        bf.close();  //又开有关
        fr.close();
        return list;
    }

    public static void main(String[] args) {
        DronesDataController dronesData = new DronesDataController();
        dronesData.drones("wuren123", "1", "1", "1");


//        System.out.println("a123".matches("[0-9]+[a-zA-Z]+[0-9a-zA-Z]*|[a-zA-Z]+[0-9]+[0-9a-zA-Z]*"));

    }

}
