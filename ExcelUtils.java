package com.just.athena.dispatch.common.util;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ExcelUtils {

	/**
	 * 导出excl
	 *
	 * @param culomnNames
	 * @param culomnBodys
	 * @param outputStream
	 * @param sheetSize
	 */
	public static void doExport(List<String> culomnNames,
								List<Map<Integer, String>> culomnBodys, OutputStream outputStream,
								int[] sheetSize) {
		HSSFWorkbook workbook = new HSSFWorkbook();
		int sheetPage = 1;
		if ((null == culomnBodys) || (culomnBodys.size() <= 0)
				|| (null == sheetSize) || (sheetSize.length <= 0)
				|| (sheetSize[0] == 0)) {
			sheetSize = new int[]{culomnBodys.size()};
		}
		if (sheetSize[0] != 0) {
			sheetPage = culomnBodys.size() / sheetSize[0];
		}
		for (int m = 0; m < sheetPage; m++) {
			HSSFSheet sheet = workbook.createSheet();

			sheet.setDefaultColumnWidth((short) 15);

			HSSFCellStyle style = workbook.createCellStyle();

			style.setFillForegroundColor((short) 40);
			style.setFillPattern((short) 1);
			style.setBorderBottom((short) 1);
			style.setBorderLeft((short) 1);
			style.setBorderRight((short) 1);
			style.setBorderTop((short) 1);
			style.setAlignment((short) 2);

			HSSFFont font = workbook.createFont();
			font.setColor((short) 20);
			font.setFontHeightInPoints((short) 12);
			font.setBoldweight((short) 700);

			style.setFont(font);

			HSSFCellStyle style2 = workbook.createCellStyle();
			style2.setFillForegroundColor((short) 43);
			style2.setFillPattern((short) 1);
			style2.setBorderBottom((short) 1);
			style2.setBorderLeft((short) 1);
			style2.setBorderRight((short) 1);
			style2.setBorderTop((short) 1);
			style2.setAlignment((short) 2);
			style2.setVerticalAlignment((short) 1);

			HSSFFont font2 = workbook.createFont();
			font2.setBoldweight((short) 400);

			style2.setFont(font2);

			HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

			HSSFComment comment = patriarch.createComment(new HSSFClientAnchor(
					0, 0, 0, 0, (short) 4, 2, (short) 6, 5));

			comment.setString(new HSSFRichTextString("可以在POI中添加注释！"));

			comment.setAuthor("leno");

			HSSFRow row = sheet.createRow(0);
			for (short i = 0; i < culomnNames.size(); i = (short) (i + 1)) {
				HSSFCell cell = row.createCell(i);
				cell.setCellStyle(style);
				HSSFRichTextString text = new HSSFRichTextString(
						(String) culomnNames.get(i));
				cell.setCellValue(text);
			}
			int pageSize = 0;
			if (sheetSize[0] != 0) {
				pageSize = m == sheetPage ? culomnBodys.size() % sheetSize[0]
						: sheetSize[0];
			}
			for (int i = 0; i < pageSize; i++) {
				row = sheet.createRow(i + 1);
				Map targetMap = (Map) culomnBodys.get(m * sheetSize[0] + i);
				for (int j = 0; j < targetMap.size(); j++) {
					HSSFCell cell = row.createCell(j);
					cell.setCellStyle(style2);
					String value = (String) targetMap.get(Integer.valueOf(j));
					HSSFRichTextString text = new HSSFRichTextString(value);
					cell.setCellValue(text);
				}
			}
		}
		try {
			workbook.write(outputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 得到Excel
	 *
	 * @param filePath
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public static Workbook createWorkbook(String filePath)
			throws InvalidFormatException, IOException {
		Workbook workbook = WorkbookFactory.create(new File(filePath));
		return workbook;
	}

	/**
	 * 通过Workbook得到所有sheet名字
	 *
	 * @param workbook
	 * @return
	 */
	public static List<String> getAllSheetName(Workbook workbook) {
		List<String> sheetNameList = new ArrayList<String>();
		int numberOfSheets = workbook.getNumberOfSheets();
		for (int i = 0; i < numberOfSheets; i++) {
			Sheet sheetAt = workbook.getSheetAt(i);
			String sheetName = sheetAt.getSheetName();
			sheetNameList.add(sheetName);
		}
		return sheetNameList;
	}

	/**
	 * 以字符串返回单元格数据
	 *
	 * @param cell
	 * @return
	 */
	public static String getCellValue(Cell cell) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String result = "";
		int cellType = cell.getCellType();
		DecimalFormat df = new DecimalFormat("#.00");// 使用DecimalFormat类对科学计数法格式的数字进行格式化
		switch (cellType) {
			// 字符串
			case HSSFCell.CELL_TYPE_STRING:
				result = cell.getStringCellValue();
				break;
			// 实数
			case HSSFCell.CELL_TYPE_NUMERIC:
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					result = sdf.format(cell.getDateCellValue());
				} else {
					result = df.format(cell.getNumericCellValue());
				}
				break;
			// 公式
			case HSSFCell.CELL_TYPE_FORMULA:
				result = String.valueOf(cell.getNumericCellValue());
				break;
			// 布尔
			case HSSFCell.CELL_TYPE_BOOLEAN:
				result = String.valueOf(cell.getBooleanCellValue());
				break;
			// 空白
			case HSSFCell.CELL_TYPE_BLANK:
				result = "";
				break;
			// 错误
			case HSSFCell.CELL_TYPE_ERROR:
				result = "";
				break;
			default:
				System.out.println("枚举了所有类型");
				break;
		}
		return result;
	}

	public static void export(OutputStream os, String title, String[] headers,
							  List<Map<String, Object>> excelMapList, String[] columnFields) {
		try {
			Workbook wb = new HSSFWorkbook();
			Sheet sheet = wb.createSheet();
			int index = 0;

			// ****************创建表格标题行*******************/
			Row titleRow = sheet.createRow(index++);

			// 标题行样式
			CellStyle titleStyle = wb.createCellStyle();
			// 标题行字体
			Font titleFont = wb.createFont();
			titleFont.setFontName("黑体");
			titleFont.setFontHeightInPoints((short) 16);// 设置字体大小
			titleStyle.setFont(titleFont);
			titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中
			// 标题表高度
			titleRow.setHeight((short) (20 * 20));
			Cell titleCell = titleRow.createCell(0);
			// 为标题行列设置样式
			titleCell.setCellStyle(titleStyle);
			titleCell.setCellValue(title);

			// 合并行
			CellRangeAddress region = new CellRangeAddress(0, 0, (short) 0,
					headers.length - 1);
			sheet.addMergedRegion(region);

			// ****************创建表格表头*******************/

			// 表头样式

			// 背景
			CellStyle headerStyle = wb.createCellStyle();
			headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT
					.getIndex());
			headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headerStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
			headerStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
			headerStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
			headerStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
			headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中

			Row headerRow = sheet.createRow(index++);
			// 表头高度
			headerRow.setHeight((short) (16 * 20));
			for (int i = 0; i < headers.length; i++) {
				Cell headerCell = headerRow.createCell(i);
				headerCell.setCellStyle(headerStyle);
				headerCell.setCellValue(headers[i]);
				if (i == 0) {
					sheet.setColumnWidth(i, 10 * 256);
				} else {
					sheet.setColumnWidth(i, 20 * 256);
				}

			}

			// ****************创建内容*******************/

			// 内容样式
			CellStyle contentStyle = wb.createCellStyle();
			contentStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
			contentStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
			contentStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
			contentStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框

			for (int i = 0; i < excelMapList.size(); i++) {
				Row contentRow = sheet.createRow(i + index);
				// 内容行高度
				contentRow.setHeight((short) (16 * 20));
				Map<String, Object> record = excelMapList.get(i);
				for (int j = 0; j < columnFields.length; j++) {
					Cell contentCell = contentRow.createCell(j);
					contentCell.setCellStyle(contentStyle);
					Object object = record.get(columnFields[j]);
					if (object != null) {
						if (object instanceof Double) {
							contentCell.setCellValue(Double.parseDouble(object
									.toString()));
						} else if (object instanceof Integer) {
							contentCell.setCellValue(Integer.parseInt(object
									.toString()));
						} else if (object instanceof Date) {
							// Date date =
							// DateUtil.convertStringToDate(object.toString(),DateUtil.mm);
							// contentCell.setCellValue(DateUtil.convertDateToString(date,
							// DateUtil.mm));
						} else {
							contentCell.setCellValue(object.toString() + " ");
						}
					} else {
						contentCell.setCellValue("");
					}
				}
			}
			wb.write(os);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void exportToExcel(HttpServletResponse response,
									 String fileName, String title, String[] headers,
									 List<Map<String, Object>> excelMapList, String[] columnFields) {
		ServletOutputStream sos = null;
		try {
			String name = new String((fileName + ".xls").getBytes("UTF-8"),
					"iso8859-1");
			response.setHeader("Content-Disposition", "attachment;filename="
					+ name + "");// 指定下载的文件名
			response.setContentType("applicationnd.ms-excel;charset=UTF-8");
			sos = response.getOutputStream();
			ExcelUtils.export(sos, title, headers, excelMapList, columnFields);
			sos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sos != null)
				try {
					sos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

}