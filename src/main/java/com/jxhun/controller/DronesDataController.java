package com.jxhun.controller;

import com.jxhun.service.DronesDataServvice;
import com.jxhun.util.FileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Jxhun
 * Date: 2019/05/07
 * Description:
 * Version: V1.0
 */
@Controller
public class DronesDataController {

    @Autowired
    DronesDataServvice dronesDataServvice;


    /**
     * 无人机传入无人机id，移动的X,Y,Z坐标值来调用service层方法写入到文本文档中
     *
     * @param plane 无人机id
     * @param X     移动的X坐标
     * @param Y     移动的Y坐标
     * @param Z     移动的Z坐标
     * @return 返回结果JSON
     */
    @ResponseBody
    @RequestMapping(value = {"/yiDong"}, method = RequestMethod.POST)
    public Map<String, Object> drones(@RequestParam String plane, String X, String Y, String Z) {
        System.out.println(plane);
        Map<String, Object> map = new HashMap<>();  // 这个map来装返回结果
        boolean action = dronesDataServvice.drones(plane, X, Y, Z);  // 如果操作成功返回true
        if(action){
            map.put("returnCode",200); // 成功返回状态码200
            map.put("msg","成功"); // 成功信息
        } else {
            map.put("returnCode",-1); // 失败返回状态码-1
            map.put("msg","失败"); // 失败信息
        }
        return map;
    }


    /**
     * 导入无人机信息后传入对应的序号
     * @param articleFile 无人机信息文件
     * @param xuHao 序号
     * @param request request对象
     * @return 返回对应的信息到前端json
     */
    @ResponseBody
    @RequestMapping(value = {"/executeImport"}, method = RequestMethod.POST)
    public Map<String, Object> daoRu(MultipartFile articleFile, String xuHao, HttpServletRequest request) {
        String flieUri = FileUpload.executeImport(articleFile, request);  // 得到文件的存储路径
        File file = new File(flieUri);  // 拿到这个文件
        Map<String, Object> map = new HashMap<>();  // 这个map来装返回结果
        try {
            ArrayList<String> list = dronesDataServvice.out(file);  // 得到文件中数据封装为list
            ArrayList<Integer> xhList = new ArrayList<>(); // 这个list用来存储序号
            for (int i = 1; i < list.size(); i++) {
                // 通过得到list最后一个元素取出序号，序号为第一个\t前面的数据，为正整数，所以可以直接转换为int值
                xhList.add(Integer.parseInt(list.get(i).substring(0, list.get(i).indexOf("\t"))));
            }
            int xH = Integer.parseInt(xuHao); // 得到传入的序号
            int action = xhList.indexOf(xH);  // 查看传入的序号是否在list存在
            if (action != -1) {  // 如果存在，那么判断是否出故障，如果没有出故障，输出无人机id 序号 坐标  如果出了故障输出 Error 序号
                // 因为取序号是从第二行开始取得，第一行为行头，那么当前下标+1就是对应数据list的下标
                String msg = list.get(action + 1); //得到对应的数据
                if (msg.contains("故障")) { //如果是故障
                    map.put("msg", "Error：" + xH); // 返回给页面
                } else {  // 如果不是故障
                    String[] coordinate = msg.substring(msg.indexOf("（") + 1, msg.indexOf("）")).split("，");
                    // 下面就是通过拆分字符串得到无人机id，无人机坐标
                    map.put("msg", msg.substring(msg.indexOf("\t\t") + 2, msg.indexOf(" ")) + " " + xH + " " +
                            coordinate[0] + " " + coordinate[1] + " " + coordinate[2]); // 返回给页面
                }
            } else {  // 如果不存在这个序号
                map.put("msg", "Cannot find " + xH); // 返回给页面
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {  // 如果抛出这个异常，说明字符串转换int数值失败
            map.put("msg", "序号格式有问题"); // 提示序号格式错误
        }
        return map;
    }
}
