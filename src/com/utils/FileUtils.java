package com.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import weaver.general.BaseBean;

import java.io.*;


/**
 * @description: 文件相关操作工具类
 * @author: JingChu
 * @createtime :2020-12-21 15:37:44
 **/
public class FileUtils {
    private /*static*/ BaseBean bb = new BaseBean();


    public /*static*/ void main(String[] args) throws Exception {
        String fileStr = getFileStr("/Users/zhaobo/Downloads/workpro/healthcommission/src/com//*static*//q.pdf");
        System.out.println("fileStr ===" + fileStr);
        System.out.println(generateFile(fileStr, "/Users/zhaobo/Downloads/workpro/healthcommission/src/com//*static*//w.pdf"));
        System.out.println("end");
        String file = "/Users/zhaobo/Downloads/workpro/healthcommission/src/com//*static*//q.pdf";
        System.out.println(getFileName(file));
        System.out.println(getFileExtension(file));
        System.out.println(getFileNameAll(file));
        System.out.println(getFilePath(file));

    }


    /**
     * 文件转化成base64字符串
     * 将文件转化为字节数组字符串，并对其进行Base64编码处理
     *
     * @param filePath 文件路径
     * @return base字符串
     */
    public /*static*/ String getFileStr(String filePath) {
        InputStream in = null;
        byte[] data = null;
        // 读取文件字节数组
        try {
            in = new FileInputStream(filePath);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        // 返回 Base64 编码过的字节数组字符串
        return encoder.encode(data);
    }


    /**
     * base64字符串转化成文件，可以是JPEG、PNG、TXT和AVI等等
     *
     * @param base64FileStr base64字符串
     * @param filePath      转换后文件名-包含路径
     * @return
     * @throws Exception
     */
    public /*static*/ boolean generateFile(String base64FileStr, String filePath) throws Exception {
        // 数据为空
        if (base64FileStr == null) {
            System.out.println(" 不行，oops！ ");
            return false;
        }
        BASE64Decoder decoder = new BASE64Decoder();


        // Base64解码,对字节数组字符串进行Base64解码并生成文件
        byte[] byt = decoder.decodeBuffer(base64FileStr);
        for (int i = 0, len = byt.length; i < len; ++i) {
            // 调整异常数据
            if (byt[i] < 0) {
                byt[i] += 256;
            }
        }
        OutputStream out = null;
        InputStream input = new ByteArrayInputStream(byt);
        try {
            // 生成指定格式的文件
            out = new FileOutputStream(filePath);
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = input.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
        } catch (IOException e) {
            bb.writeLog("------------------->INFO:  该文件路径bu存在「" + filePath + "」");
        } finally {
            out.flush();
            out.close();
        }
        return true;
    }

    /** 获取文件名称(不包含路径)
     * @param filePath
     * @return
     */
    public /*static*/ String getFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/")+1, filePath.lastIndexOf("."));
    }

    /**
     * 获取文件后缀名（包含.）
     * @param filePath
     * @return
     */
    public /*static*/ String getFileExtension(String filePath) {
        return filePath.substring(filePath.lastIndexOf("."));
    }

    /**
     * 获取文件名称（包含后缀名）
     * @param filePath
     * @return
     */
    public /*static*/ String getFileNameAll(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/")+1);
    }

    /**
     * 获取文件路径
     * @param filePath
     * @return
     */
    public /*static*/ String getFilePath(String filePath) {
        return filePath.substring(0, filePath.lastIndexOf("/"));
    }

}
