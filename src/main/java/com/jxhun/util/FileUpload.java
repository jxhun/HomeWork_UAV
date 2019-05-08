package com.jxhun.util;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: Jxhun
 * Date: 2019/05/8
 * Description: 文件上传工具类
 * Version: V1.0
 */
public class FileUpload {

    /**
     * 这个文件用来上传文件
     *
     * @param articleFile 接收上传文件
     * @param request     得到session
     * @return 成功返回路径
     */
    public static String executeImport(MultipartFile articleFile, HttpServletRequest request) {
        String originalFilename = articleFile.getOriginalFilename();
        String jueDuiLuJing = request.getSession().getServletContext().getRealPath(File.separator) + File.separator;  // 得到文件绝对路径
        File diroffice = new File(jueDuiLuJing + "text");
        if (!diroffice.exists() && !diroffice.isDirectory()) {  // 如果offie目录不存在
            System.out.println("//不存在");
            diroffice.mkdir();  // 创建目录
        }
        String luJingQianZui = "text" + File.separator;  // 图片上传路径前缀
        File file = new File(jueDuiLuJing + luJingQianZui + originalFilename);  // 组装上传路径得到文件路径
        try {
            articleFile.transferTo(file);      // 上传
            return jueDuiLuJing + luJingQianZui + originalFilename;   // 上传成功return地址路径
        } catch (Exception e) {
//            e.printStackTrace();
            return null;   // 文件上传失败返回null
        }
    }
}
