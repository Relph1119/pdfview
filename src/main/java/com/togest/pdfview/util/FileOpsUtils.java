/**
 * 
 */
package com.togest.pdfview.util;

/**
 * @author Hurf
 *
 */
public class FileOpsUtils {
	
	public static String AddFileNameWithValue(String filePath, String addValue) {
		String sname = filePath.substring(filePath.lastIndexOf("."));
		String fileName = filePath.substring(0,filePath.lastIndexOf("."));
		StringBuffer sb = new StringBuffer();
		sb.append(fileName).append("-").append(addValue).append(sname);
		return sb.toString();
		
	}
	
	public static String getfileNameWithPDFSuffix(String filePath) {
		String fileName = filePath.substring(0,filePath.lastIndexOf("."));
		StringBuffer sb = new StringBuffer();
		sb.append(fileName).append(".").append("pdf");
		return sb.toString();
	}
	
	
}
