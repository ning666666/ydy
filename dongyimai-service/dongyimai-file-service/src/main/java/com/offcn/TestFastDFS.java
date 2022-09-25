package com.offcn;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

import java.io.IOException;

public class TestFastDFS {
    public static void main(String[] args) throws IOException, MyException {
        //方法需要做的事：图片上传操作
        //1.读取配置文件，连接老大checker
        ClientGlobal.init("D:/Java学习笔记/IDEA/code/dongyimai-parent-project/dongyimai-service/dongyimai-file-service/src/main/resources/fdfs_client.conf");
        //2.操作的是连接服务器的客户端，服务器在虚拟机上，客户端首先去找tracker老大服务器
        TrackerClient trackerClient = new TrackerClient();
        //3.通过客户端获取服务器连接
        TrackerServer trackerServer = trackerClient.getConnection();
        //4.通过trackerServer服务器连接，返回storage的ip地址和端口号，得到storageServer服务器连接
        //  再操作客户端再到指定的storageServer服务器进行文件上传
        //需要两个参数，trackerServer和storageServer，storageServer是tracker老大分配的而不是我们自定义的
        StorageClient storageClient = new StorageClient(trackerServer,null);
        //组名和虚拟磁盘路径+数据两级目录+文件名分开返回
        //group1  M00/00/00/wKg0hGLfk6-AHAwxAAr-IjMemRY492.jpg
        String[] strings = storageClient.upload_file("D:/upload/16577261450262.jpg","jpg",null);
        //处理结果
        for (String string : strings) {
            System.out.println("string = " + string);
        }
    }
}
