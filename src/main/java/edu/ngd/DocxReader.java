package edu.ngd;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class DocxReader {
    
    public static void main(String[] args) {
        String filePath = "kbms-srs.docx";
        
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            System.out.println("========== 知识库管理系统需求分析书 ==========\n");
            
            // 读取所有段落
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    // 根据段落样式判断标题级别
                    String style = paragraph.getStyle();
                    if (style != null) {
                        if (style.contains("Heading1") || style.equals("1")) {
                            System.out.println("\n========== " + text + " ==========\n");
                        } else if (style.contains("Heading2") || style.equals("2")) {
                            System.out.println("\n【" + text + "】\n");
                        } else if (style.contains("Heading3") || style.equals("3")) {
                            System.out.println("\n  ● " + text);
                        } else {
                            System.out.println(text);
                        }
                    } else {
                        System.out.println(text);
                    }
                }
            }
            
            // 读取表格
            List<XWPFTable> tables = document.getTables();
            if (!tables.isEmpty()) {
                System.out.println("\n========== 表格内容 ==========\n");
                for (int i = 0; i < tables.size(); i++) {
                    System.out.println("表格 " + (i + 1) + ":");
                    XWPFTable table = tables.get(i);
                    for (XWPFTableRow row : table.getRows()) {
                        StringBuilder rowText = new StringBuilder("  | ");
                        for (XWPFTableCell cell : row.getTableCells()) {
                            rowText.append(cell.getText()).append(" | ");
                        }
                        System.out.println(rowText);
                    }
                    System.out.println();
                }
            }
            
        } catch (IOException e) {
            System.err.println("读取文档失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}