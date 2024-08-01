package com.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QuotationExcel {
    public void processExcel(String inputFilePath, String outputFilePath) {
        try (FileInputStream fis = new FileInputStream(inputFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet1 = workbook.getSheetAt(0);
            Sheet sheet2 = workbook.getSheetAt(1);

            Map<String, Integer> firstSheetMap = new HashMap<>();
            int rowCountSheet1 = sheet1.getPhysicalNumberOfRows();

            // Reading the first sheet and storing the data in a map
            for (int i = 0; i < rowCountSheet1; i++) {
                Row row = sheet1.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(0);
                    if (cell != null) {
                        firstSheetMap.put(cell.getStringCellValue(), i);
                    }
                }
            }

            int rowCountSheet2 = sheet2.getPhysicalNumberOfRows();

            // Reading the second sheet and appending data from the first sheet
            for (int i = 0; i < rowCountSheet2; i++) {
                Row row = sheet2.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(0);
                    if (cell != null) {
                        String key = cell.getStringCellValue();
                        if (firstSheetMap.containsKey(key)) {
                            int rowIndexSheet1 = firstSheetMap.get(key);
                            Row rowFromSheet1 = sheet1.getRow(rowIndexSheet1);
                            if (rowFromSheet1 != null) {
                                // Appending columns 2, 3, 4, 5 from sheet1 to columns 11, 12, 13, 14 in sheet2
                                for (int j = 1; j <= 4; j++) {
                                    Cell cellFromSheet1 = rowFromSheet1.getCell(j);
                                    if (cellFromSheet1 != null) {
                                        Cell newCell = row.createCell(j + 10);
                                        switch (cellFromSheet1.getCellType()) {
                                            case STRING:
                                                newCell.setCellValue(cellFromSheet1.getStringCellValue());
                                                break;
                                            case NUMERIC:
                                                newCell.setCellValue(cellFromSheet1.getNumericCellValue());
                                                break;
                                            case BOOLEAN:
                                                newCell.setCellValue(cellFromSheet1.getBooleanCellValue());
                                                break;
                                            default:
                                                newCell.setCellValue(cellFromSheet1.toString());
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Write the updated data to a new Excel file
            try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                workbook.write(fos);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        QuotationExcel processor = new QuotationExcel();
        processor.processExcel("C:\\Users\\CalvinYuen\\OneDrive - American Bear Logistics\\Quotation Rate Table Project\\4 COMBINATION\\COMBINED EVERYTHING.xlsx", "output.xlsx");
    }
}
