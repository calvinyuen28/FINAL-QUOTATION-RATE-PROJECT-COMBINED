package com.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AverageFinder {

    public static void main(String[] args) {
        String excelFilePath = "C:\\Users\\CalvinYuen\\OneDrive - American Bear Logistics\\Quotation Rate Table Project\\4 COMBINATION\\COMBINED EVERYTHING.xlsx";

        // Map to store total profit and count for each destination-month combination
        Map<String, Map<String, Double>> profitMap = new HashMap<>();
        Map<String, Map<String, Integer>> countMap = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
            boolean header = true;

            for (Row row : sheet) {
                if (header) {
                    header = false;
                    continue;
                }

                Cell destCell = row.getCell(0);
                Cell monthCell = row.getCell(1);
                Cell profitCell = row.getCell(2);

                if (destCell == null || monthCell == null || profitCell == null) {
                    continue;
                }

                String dest = getCellStringValue(destCell);
                String month = getCellStringValue(monthCell);
                double profit = profitCell.getNumericCellValue();

                // Initialize maps if they don't exist
                profitMap.putIfAbsent(dest, new HashMap<>());
                profitMap.get(dest).putIfAbsent(month, 0.0);

                countMap.putIfAbsent(dest, new HashMap<>());
                countMap.get(dest).putIfAbsent(month, 0);

                // Update profit and count for the destination-month combination
                profitMap.get(dest).put(month, profitMap.get(dest).get(month) + profit);
                countMap.get(dest).put(month, countMap.get(dest).get(month) + 1);
            }

            // Adding the calculated averages to the sheet
            int rowNum = 0; // Start from the first row

            for (String dest : profitMap.keySet()) {
                for (String month : profitMap.get(dest).keySet()) {
                    double totalProfit = profitMap.get(dest).get(month);
                    int count = countMap.get(dest).get(month);
                    double averageProfit = totalProfit / count;

                    Row row = sheet.getRow(rowNum);
                    if (row == null) {
                        row = sheet.createRow(rowNum);
                    }

                    Cell destCell = row.createCell(7); // Column H
                    Cell monthCell = row.createCell(8); // Column I
                    Cell avgProfitCell = row.createCell(9); // Column J

                    destCell.setCellValue(dest);
                    monthCell.setCellValue(month);
                    avgProfitCell.setCellValue(averageProfit);

                    rowNum++;
                }
            }

            // Write the output to the file
            try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                workbook.write(fos);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getCellStringValue(Cell cell) {
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
}
