package com.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelProcessor {

    public static void main(String[] args) {
        ExcelProcessor processor = new ExcelProcessor();
        processor.processExcel("C:\\Users\\CalvinYuen\\OneDrive - American Bear Logistics\\Quotation Rate Table Project\\4 COMBINATION\\COMBINED EVERYTHING.xlsx",
                              "output.xlsx");
    }

    public void processExcel(String inputFilePath, String outputFilePath) {
        try {
            // Read data from input Excel file and get processed rows
            List<String[]> processedRows = readAndProcessExcelFile(inputFilePath);

            // Write processed data to output Excel file
            writeExcelFile(outputFilePath, processedRows);

            System.out.println("Excel file processed successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String[]> readAndProcessExcelFile(String filePath) throws IOException {
        List<String[]> processedRows = new ArrayList<>();
        Pattern pattern = Pattern.compile("^[A-Z]{3}\\d$");

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell cell = row.getCell(0);  // Assuming data is in the first column
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue();
                    String[] codes = cellValue.split("/");

                    for (String code : codes) {
                        code = code.trim();
                        Matcher matcher = pattern.matcher(code);
                        if (matcher.matches()) {
                            // Create a new row with the matched code
                            String[] newRow = new String[row.getLastCellNum()];
                            newRow[0] = code;

                            // Copy other cell values from the original row
                            for (int i = 1; i < row.getLastCellNum(); i++) {
                                Cell originalCell = row.getCell(i);
                                if (originalCell != null) {
                                    newRow[i] = getCellValueAsString(originalCell);
                                }
                            }

                            processedRows.add(newRow);
                        }
                    }
                }
            }
        }

        return processedRows;
    }

    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private void writeExcelFile(String filePath, List<String[]> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(filePath)) {

            Sheet sheet = workbook.createSheet("Processed Data");

            int rowIndex = 0;
            for (String[] rowData : rows) {
                Row row = sheet.createRow(rowIndex++);
                for (int colIndex = 0; colIndex < rowData.length; colIndex++) {
                    Cell cell = row.createCell(colIndex);
                    cell.setCellValue(rowData[colIndex]);
                }
            }

            workbook.write(fos);
        }
    }
}
