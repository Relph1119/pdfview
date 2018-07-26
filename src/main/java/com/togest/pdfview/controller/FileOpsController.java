package com.togest.pdfview.controller;

import java.io.File;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/file")
public class FileOpsController {
	
	private final Logger logger = LoggerFactory.getLogger(FileOpsController.class);
	
    /**
     * 上传文件
     * @param request
     * @param response
     * @param file 上传的文件，支持多文件
     * @throws Exception
     */
    @RequestMapping("/upload")
    public @ResponseBody String insert(HttpServletRequest request,HttpServletResponse response
            ,@RequestParam("file") MultipartFile file) throws Exception{
    	String basePath = FileOpsController.class.getResource("/").getPath();
//        String basePath = request.getServletContext().getRealPath("templates/upload-files/");
        File directory = new File(basePath+File.separator + "templates" + File.separator + "upload-files");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        if (!file.isEmpty()) {
            try {
            	file.transferTo(new File(directory.getPath() + File.separator +file.getOriginalFilename()));
            } catch (Exception e) {
                logger.info("You failed to upload => " + e.getMessage());
            }
        } else {
        	logger.info("You failed to upload because the file was empty.");
        }
        
        logger.info("upload successful");
        return "{}";
    }
}