package com.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class RateTransfer {

    public static void main(String[] args) {
        RateTransfer rateTransfer = new RateTransfer();
        String inputFilePath = "C:\\Users\\CalvinYuen\\OneDrive - American Bear Logistics\\Quotation Rate Table Project\\4 COMBINATION\\COMBINED EVERYTHING.xlsx";
        String outputFilePath = "output.xlsx";
        rateTransfer.processExcel(inputFilePath, outputFilePath);
    }

    public void processExcel(String inputFilePath, String outputFilePath) {
        try {
            // Create a map from the first sheet
            Map<String, List<CodeRangeInfo>> codeMap = createCodeMap(inputFilePath);

            // Process the second sheet using the code map and create an output file
            processSecondSheet(inputFilePath, outputFilePath, codeMap);

            System.out.println("Excel file processed successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, List<CodeRangeInfo>> createCodeMap(String filePath) throws IOException {
        Map<String, List<CodeRangeInfo>> codeMap = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet1 = workbook.getSheetAt(0); // First sheet

            Iterator<Row> rowIterator = sheet1.iterator();
            rowIterator.next(); // Skip header row

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell codeCell = row.getCell(0);
                Cell itemValueCell = row.getCell(1);
                Cell rangeStartCell = row.getCell(2);
                Cell rangeEndCell = row.getCell(3);

                if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                    String code = codeCell.getStringCellValue().trim();
                    String itemValue = getCellValueAsString(itemValueCell);
                    int rangeStart = (int) getNumericCellValue(rangeStartCell);
                    int rangeEnd = (int) getNumericCellValue(rangeEndCell);

                    CodeRangeInfo codeRangeInfo = new CodeRangeInfo(itemValue, rangeStart, rangeEnd);

                    codeMap.computeIfAbsent(code, k -> new ArrayList<>()).add(codeRangeInfo);
                    System.out.println("Added to map: " + code + " with range " + rangeStart + "-" + rangeEnd + " and value " + itemValue);
                }
            }
        }

        return codeMap;
    }

    public void processSecondSheet(String inputFilePath, String outputFilePath, Map<String, List<CodeRangeInfo>> codeMap) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFilePath);
             Workbook workbook = new XSSFWorkbook(fis);
             FileOutputStream fos = new FileOutputStream(outputFilePath)) {

            Sheet sheet2 = workbook.getSheetAt(1); // Second sheet

            Iterator<Row> rowIterator = sheet2.iterator();
            rowIterator.next(); // Skip header row

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell codeCell = row.getCell(1); // Second column
                Cell palletNumberCell = row.getCell(4); // Fifth column

                if (codeCell != null && codeCell.getCellType() == CellType.STRING &&
                    palletNumberCell != null && palletNumberCell.getCellType() == CellType.NUMERIC) {
                    String code = codeCell.getStringCellValue().trim();
                    int palletNumber = (int) palletNumberCell.getNumericCellValue();
                    System.out.println("Processing row with code: " + code + " and pallet number: " + palletNumber);

                    if (codeMap.containsKey(code)) {
                        for (CodeRangeInfo codeRangeInfo : codeMap.get(code)) {
                            if (codeRangeInfo.isInRange(palletNumber)) {
                                Cell priceCell = row.createCell(14); // Fifteenth column (index 14)
                                priceCell.setCellValue(codeRangeInfo.getItemValue());
                                System.out.println("Updated price for code: " + code + " with pallet number: " + palletNumber + " to " + codeRangeInfo.getItemValue());
                                break;
                            }
                        }
                    } else {
                        System.out.println("Code: " + code + " not found in map.");
                    }
                }
            }

            workbook.write(fos);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
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

    private double getNumericCellValue(Cell cell) {
        if (cell == null) {
            return 0;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
