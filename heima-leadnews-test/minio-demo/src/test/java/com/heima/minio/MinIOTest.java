package com.heima.minio;

import com.heima.file.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MinIOTest {

    @Autowired
    private FileStorageService fileStorageService;


//    @Test
//    public void testUpdateImgFile() {
//        try {
//            FileInputStream fileInputStream = new FileInputStream("E:\\tmp\\ak47.jpg");
//            String filePath = fileStorageService.uploadImgFile("", "ak47.jpg", fileInputStream);
//            System.out.println(filePath);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    public static void main(String[] args) {
        //上传
        try {
            FileInputStream fileInputStream = new FileInputStream("D:\\list.html");
            //minio客户端
            MinioClient minioClient = MinioClient.builder().credentials("minio", "minio123").endpoint("http://192.168.200.130:9000").build();
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object("list.html")
                    .contentType("text/html")
                    .bucket("leadnews")
                    .stream(fileInputStream, fileInputStream.available(), -1).build();
            minioClient.putObject(putObjectArgs);

            //访问路径
            System.out.println("http://192.168.200.130:9000/leadnews/ak47.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
