package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.ManagerService;
import io.swagger.annotations.Api;
import org.csource.common.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @title: FileUploadController
 * @Author LiuXianKun
 * @Date: 2020/11/16 18:04
 */
@Api(tags = "上传管理接口")
@RestController
@RequestMapping("/admin/product")
public class FileUploadController {
    @Autowired
    private ManagerService managerService;


    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) throws IOException, MyException {
        String  imgUrl  = managerService.fileUpload(file);

        return Result.ok(imgUrl);
    }



}
