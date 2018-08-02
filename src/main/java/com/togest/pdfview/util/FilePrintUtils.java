/**
 * 
 */
package com.togest.pdfview.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jodconverter.DocumentConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.togest.pdfview.bean.FileFieldFlags;

/**
 * @author Hurf
 *
 */
@Controller
@RequestMapping("/test")
public class FilePrintUtils {
	
	@Resource
	private DocumentConverter documentConverter;
	
	Logger logger = LoggerFactory.getLogger(FilePrintUtils.class);
	
	@RequestMapping("/test")
    public @ResponseBody void test(HttpServletRequest request,HttpServletResponse response) throws Exception{
	
		List<Map<String, Object>> data = new ArrayList<>();
		Map<String, Object> map1 = new HashMap<>();
		map1.put("name", "小红");
		map1.put("sex", "女");
		map1.put("age", 25);
		map1.put("company", "togest");
		map1.put("worktime", 3);
		map1.put("location", "北京");
		map1.put("fav", "看书");
		map1.put("level", "员工");
		data.add(map1);
		Map<String, Object> map2 = new HashMap<>();
		map2.put("name", "小明");
		map2.put("sex", "男");
		map2.put("age", 25);
		map2.put("company", "togest");
		map2.put("worktime", 2);
		map2.put("location", "北京");
		map2.put("fav", "魔方");
		map2.put("level", "员工");
		data.add(map2);
		Map<String, Object> map3 = new HashMap<>();
		map3.put("name", "小崔");
		map3.put("sex", "男");
		map3.put("age", 27);
		map3.put("company", "togest");
		map3.put("worktime", 4);
		map3.put("location", "北京");
		map3.put("fav", "学习");
		map3.put("level", "经理");
		data.add(map3);
		String file = FileOpsUtils.class.getClassLoader().getResource("static/upload/Book2.xlsx").getPath();
		addDataToExcelTemplate(data, file, true);
	}

	/**
	 * @param data
	 * @param file
	 * @throws Exception
	 */
	private void addDataToExcelTemplate(List<Map<String, Object>> data, String filePath, boolean needMergePdfFlag) throws Exception {
		
		List<File> needMergeFiles = new ArrayList<>();
		List<File> needDeleteFiles = new ArrayList<>();
		List<File> needCovertFiles = new ArrayList<>();
		
		
		for (int i = 0; i < data.size(); i++) {
			Map<String, Object> map = data.get(i);
			File file = new File(filePath);
			File destFile = new File(FileOpsUtils.AddFileNameWithValue(filePath, Integer.toString(i)));
			FileUtils.copyFile(file, destFile);
			//回填
			InputStream is = new FileInputStream(destFile);
			Workbook wb = WorkbookFactory.create(is);
			replaceExcelData(wb, 0, map);
			FileOutputStream fileOut = new FileOutputStream(destFile);   
			wb.write(fileOut);
			fileOut.close();
			is.close();
			wb.close();
			needDeleteFiles.add(destFile);
			needCovertFiles.add(destFile);
		}
		
		for (File file : needCovertFiles) {
			logger.info("------------------开始转换："+file.getName());
			File pdfFile  = convert2pdf(file);
			/*needMergeFiles.add(pdfFile);
			needDeleteFiles.add(pdfFile);*/
		}
		
		//进行文件合并
		if(needMergePdfFlag) {
			
		}

	}

	/**
	 * @param destFile
	 * @throws Exception 
	 */
	private File convert2pdf(File file) throws Exception {
		  
		 String targetFilePath = FileOpsUtils.getfileNameWithPDFSuffix(file.getPath());
		 File targetFile = new File(targetFilePath);
		 documentConverter.convert(file).to(targetFile).execute();
		 return targetFile;
	}

	private void setCellStrValue(int rowIndex, int cellnum, String value, Sheet sheet) {
		if(isMergedRegion(sheet, rowIndex, cellnum)) {
			if( isFirstCellMergedRegion(sheet, rowIndex, cellnum)) {
				Cell cell = sheet.getRow(rowIndex).getCell(cellnum);
				cell.setCellValue(value);
			}
		}else {
			Cell cell = sheet.getRow(rowIndex).getCell(cellnum);
			cell.setCellValue(value);
		}
	}

	/**
	 * 读取excel文件
	 * @param wb
	 * @param sheetIndex sheet页下标：从0开始
	 */
	private void replaceExcelData(Workbook wb, int sheetIndex, Map<String, Object> map) {
		Sheet sheet = wb.getSheetAt(sheetIndex);
		Row row = null;
		
		for (int i = 0; i < sheet.getLastRowNum() + 1; i++) {
			row = sheet.getRow(i);
			for (Cell c : row) {
				String cellValue = null;
				boolean isMerge = isMergedRegion(sheet, i, c.getColumnIndex());
				// 判断是否具有合并单元格
				if (isMerge) {
					cellValue = getMergedRegionValue(sheet, row.getRowNum(), c.getColumnIndex());
				} else {
					cellValue = getCellValue(c);
				}
				for (Entry<String, Object> entry : map.entrySet()) {
					String key = entry.getKey();
					if ((FileFieldFlags.FIELD_PREFIX + key + FileFieldFlags.FIELD_SUFFIX).equals(cellValue)) {
						String value = entry.getValue().toString();
						setCellStrValue(row.getRowNum(), c.getColumnIndex(), value, sheet);
					}
				}
				
			}
		}

	}

	/**
	 * 获取合并单元格的值
	 * 
	 * @param sheet
	 * @param row
	 * @param column
	 * @return
	 */
	public String getMergedRegionValue(Sheet sheet, int row, int column) {
		int sheetMergeCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress ca = sheet.getMergedRegion(i);
			int firstColumn = ca.getFirstColumn();
			int lastColumn = ca.getLastColumn();
			int firstRow = ca.getFirstRow();
			int lastRow = ca.getLastRow();
			if (row == firstRow && row <= lastRow) {
				if (column == firstColumn && column <= lastColumn) {
					Row fRow = sheet.getRow(firstRow);
					Cell fCell = fRow.getCell(firstColumn);
					return getCellValue(fCell);
				}
			}
		}

		return null;
	}
	
	private boolean isFirstCellMergedRegion(Sheet sheet, int row, int column) {
		int sheetMergeCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress ca = sheet.getMergedRegion(i);
			int firstColumn = ca.getFirstColumn();
			int lastColumn = ca.getLastColumn();
			int firstRow = ca.getFirstRow();
			int lastRow = ca.getLastRow();
			if (row == firstRow && row <= lastRow) {
				if (column == firstColumn && column <= lastColumn) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 判断指定的单元格是否是合并单元格
	 * 
	 * @param sheet
	 * @param row 行下标
	 * @param column 列下标
	 * @return
	 */
	private boolean isMergedRegion(Sheet sheet, int row, int column) {

		int sheetMergeCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress range = sheet.getMergedRegion(i);
			int firstColumn = range.getFirstColumn();
			int lastColumn = range.getLastColumn();
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			if (row >= firstRow && row <= lastRow) {
				if (column >= firstColumn && column <= lastColumn) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 合并单元格
	 * 
	 * @param sheet
	 * @param firstRow 开始行
	 * @param lastRow 结束行
	 * @param firstCol 开始列
	 * @param lastCol 结束列
	 */
	private void mergeRegion(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
		sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
	}

	/**
	 * 获取单元格的值
	 * 
	 * @param cell
	 * @return
	 */
	public String getCellValue(Cell cell) {
		if (cell == null)
			return "";
		if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
			return cell.getStringCellValue();
		} else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
			return String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
			return cell.getCellFormula();
		} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			return String.valueOf(cell.getNumericCellValue());
		}
		return "";
	}
	
	
	
}
