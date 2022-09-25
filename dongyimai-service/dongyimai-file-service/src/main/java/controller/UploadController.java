package controller;

import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {
    /*
    当点击文件上传，发送以enctype二进制流的形式上传文件的请求给前端控制器，前端控制器根据enctype属性寻找MVC文件上传组件，
    MVC文件上传组件返回保存文件信息的MultipartFile对象给前端控制器，前端控制器再发送给后端开始上传
    */
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;//文件服务器地址

    @PostMapping("/upload")
    public Result upload(@RequestParam(name = "file") MultipartFile file) {
        //1.取文件的扩展名
        //从保存文件信息的MultipartFile对象中获取文件路径
        String originalFilename = file.getOriginalFilename();
        String extName =originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        try {
            //2.创建FastDFS客户端，读取配置文件，连接服务器checker老大
            FastDFSClient fastDFSClient = new FastDFSClient("D:\\Java学习笔记\\IDEA\\code\\dongyimai-parent-project\\dongyimai-service\\dongyimai-file-service\\src\\main\\resources\\fdfs_client.conf");
            //3.指定上传文件的路径，三个参数（客户端上传的文件内容、文件扩展名、文件扩展信息map集合给我们自己看的，随意存键和值），执行上传操作
            String path = fastDFSClient.uploadFile(file.getBytes(), extName);
            //4.将返回的path：group1 + M00/00/00/wKg0hGLfk6-AHAwxAAr-IjMemRY492.jpg和配置文件中的地址，拼装成完整的url
            String url = FILE_SERVER_URL + path;
            return new Result(true, StatusCode.OK, url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, StatusCode.ERROR, "上传失败");
        }
    }
}
