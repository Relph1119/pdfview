package com.togest.pdfview.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jodconverter.DocumentConverter;
import org.jodconverter.office.OfficeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;

@Controller
@RequestMapping("/file")
public class FileOpsController {
	@Resource
	private DocumentConverter documentConverter;
	private final Logger logger = LoggerFactory.getLogger(FileOpsController.class);
	private final String BASEPATH = FileOpsController.class.getResource("/").getPath();
	
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
        File directory = new File(BASEPATH+File.separator + "static" + File.separator + "upload-files");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        if (!file.isEmpty()) {
            try {
            	file.transferTo(new File(directory.getPath() + File.separator +file.getOriginalFilename()));
            	
            	//convert to pdf
            	convert2pdf(file.getOriginalFilename(), directory.getPath(), directory.getPath());
            	
            } catch (Exception e) {
                logger.info("You failed to upload => " + e.getMessage());
            } 
        } else {
        	logger.info("You failed to upload because the file was empty.");
        }
        
        logger.debug("file upload successful");
        
        return "{}";
    }
    
   
    @RequestMapping("/list")
    public @ResponseBody JSONObject getFilesList(HttpServletRequest request,
    			HttpServletResponse response, 
    			@RequestParam("limit") Integer limit,
    			@RequestParam("offset") Integer offset,
    			@RequestParam("fileName") String fileName) throws Exception{ 
    	JSONObject json = new JSONObject();
    	List<Map<String, Object>> fileList = new ArrayList<Map<String,Object>>();
    	File directory = new File(BASEPATH+File.separator + "static" + File.separator + "upload-files");
    	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	if (directory.isDirectory() && directory.listFiles().length>0) {
    		File[] files = directory.listFiles();
    		for (File file : files) {
    			if(file.getName().lastIndexOf(".pdf") > 0) {
    				HashMap<String, Object> map = new HashMap<String, Object>();
        			map.put("fileName", file.getName());
        			String fileUploadDate = sdf.format(new Date(file.lastModified()));   // 时间戳转换成时间
        			map.put("uploadDate", fileUploadDate);
        			fileList.add(map);
    			}
    		}
    	}
    	
    	List<Map<String, Object>> subfileList = new ArrayList<>();
    	if(offset * limit > fileList.size()) {
    		subfileList = fileList.subList(limit * (offset - 1), fileList.size());
    	}else {
    		subfileList = fileList.subList(limit * (offset - 1), offset * limit);
    	}
    	
    	json.put("rows", subfileList);
    	json.put("total", fileList.size());
    	
        return json;
    }
    
    private void convert2pdf(String fileName, String sourceDir, String targetDir) throws OfficeException {
    	String fileNameWithSuffix = fileName.substring(0, fileName.lastIndexOf('.'));
		
		String sourceFilePath = sourceDir + File.separator + fileName;
		String targetFilePath = targetDir + File.separator + fileNameWithSuffix + ".pdf";
		
		File sourceFile = new File(sourceFilePath);
		File targetFile = new File(targetFilePath);
		
		documentConverter.convert(sourceFile).to(targetFile).execute();
    }

}
