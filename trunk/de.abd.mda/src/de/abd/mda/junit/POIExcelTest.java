package de.abd.mda.junit;

import java.io.FileOutputStream;


import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class POIExcelTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		FileOutputStream out = new FileOutputStream("workbook.xls");
		Workbook wb = new HSSFWorkbook();
		Sheet s = wb.createSheet();
		Row r = null;
		Cell c = null;
		CellStyle cs = wb.createCellStyle();
		CellStyle cs2 = wb.createCellStyle();
		CellStyle cs3 = wb.createCellStyle();
		DataFormat df = wb.createDataFormat();
		Font f = wb.createFont();
		Font f2 = wb.createFont();
		
		f.setFontHeightInPoints((short) 12);
		f.setColor( (short)0xc );
		f.setBoldweight(Font.BOLDWEIGHT_BOLD);
		
		f2.setFontHeightInPoints((short) 10);
		f2.setColor( (short)Font.COLOR_RED );
		f2.setBoldweight(Font.BOLDWEIGHT_BOLD);

		f2.setStrikeout(true);
		
		cs.setFont(f);
		cs.setDataFormat(df.getFormat("#,##0.0"));
		
		cs2.setBorderBottom(cs2.BORDER_THIN);
		cs2.setFillPattern((short) CellStyle.SOLID_FOREGROUND );
		cs2.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
		
		cs2.setFont(f2);
		
		wb.setSheetName(0, "HSSF Test");
		int rownum;
		for (rownum = (short) 0; rownum < 30; rownum++) {
			r = s.createRow(rownum);
			if ((rownum % 2) == 0 ) {
				r.setHeight((short) 0x249);
			}
		
			for (short cellnum = (short) 0; cellnum < 10; cellnum += 2) {
				c = r.createCell(cellnum);
				c.setCellValue(rownum * 10000 + cellnum
						+ (((double) rownum / 1000)
						+ ((double) cellnum / 10000)));
				
				String cellValue;
				
				c = r.createCell((short) (cellnum + 1));
				
				if ((rownum % 2) == 0) {
					c.setCellStyle(cs);
					c.setCellValue("Test");
				} else {
					c.setCellStyle(cs2);
					c.setCellValue("\u0422\u0435\u0441\u0442");
				}
				
				s.setColumnWidth((short) (cellnum + 1), (short) ((50 * 8) / ((double) 1 / 20)));
			}

		}
		
		rownum++;
		rownum++;
		
		r = s.createRow(rownum);
		
		cs3.setBorderBottom(cs3.BORDER_THICK);
		
		for (short cellnum = (short) 0; cellnum < 50; cellnum++) {
			c = r.createCell(cellnum);
			c.setCellStyle(cs3);
		}
		
		s = wb.createSheet();
		wb.setSheetName(1, "DeletedSheet");
		wb.removeSheetAt(1);
		
		wb.write(out);
		out.close();
	}
	
}
