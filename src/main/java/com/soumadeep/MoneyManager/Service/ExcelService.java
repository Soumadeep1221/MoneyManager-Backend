package com.soumadeep.MoneyManager.Service;

import com.soumadeep.MoneyManager.DTO.ExpenseDTO;
import com.soumadeep.MoneyManager.DTO.IncomeDTO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class ExcelService {
    public void writeIncomesToExcel(OutputStream os, List<IncomeDTO> incomes) throws IOException {
        try(Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Incomes");
            Row header=sheet.createRow(0);
            header.createCell(0).setCellValue("S.No");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Category");
            header.createCell(3).setCellValue("Amount");
            header.createCell(4).setCellValue("Date");
            IntStream.range(0, incomes.size())
                    .forEach(i -> {
                        IncomeDTO incomeDTO = incomes.get(i);
                        Row row = sheet.createRow(i + 1);
                        row.createCell(0).setCellValue(i + 1);
                        row.createCell(1).setCellValue(incomeDTO.getName()!=null ? incomeDTO.getName() : "N/A");
                        row.createCell(2).setCellValue(incomeDTO.getCategoryId()!=null ? incomeDTO.getCategoryName() : "N/A");
                        row.createCell(3).setCellValue(incomeDTO.getAmount()!=null ? incomeDTO.getAmount().doubleValue() : 0);
                        row.createCell(4).setCellValue(incomeDTO.getDate()!=null ? incomeDTO.getDate().toString() : "N/A");
                    });
            workbook.write(os);
        }
    }

    public void writeExpensesToExcel(OutputStream os, List<ExpenseDTO> expenses) throws IOException {
        try(Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Expenses");
            Row header=sheet.createRow(0);
            header.createCell(0).setCellValue("S.No");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Category");
            header.createCell(3).setCellValue("Amount");
            header.createCell(4).setCellValue("Date");
            IntStream.range(0, expenses.size())
                    .forEach(i -> {
                        ExpenseDTO expenseDTO = expenses.get(i);
                        Row row = sheet.createRow(i + 1);
                        row.createCell(0).setCellValue(i + 1);
                        row.createCell(1).setCellValue(expenseDTO.getName()!=null ? expenseDTO.getName() : "N/A");
                        row.createCell(2).setCellValue(expenseDTO.getCategoryId()!=null ? expenseDTO.getCategoryName() : "N/A");
                        row.createCell(3).setCellValue(expenseDTO.getAmount()!=null ? expenseDTO.getAmount().doubleValue() : 0);
                        row.createCell(4).setCellValue(expenseDTO.getDate()!=null ? expenseDTO.getDate().toString() : "N/A");
                    });
            workbook.write(os);
        }
    }
}
