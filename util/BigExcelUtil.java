package com.signs.util;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class BigExcelUtil {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String filename;
    private SheetContentsHandler handler;
    private InputStream tempInputStream;

    public BigExcelUtil(String filename) {
        this.filename = filename;
    }

    public BigExcelUtil(InputStream inputStream) {
        this.tempInputStream = inputStream;
    }

    public BigExcelUtil setHandler(SheetContentsHandler handler) {
        this.handler = handler;
        return this;
    }

    public void parse() {
        OPCPackage pkg = null;
        InputStream sheetInputStream = null;
        try {
            if (tempInputStream != null)
                pkg = OPCPackage.open(tempInputStream);
            else
                pkg = OPCPackage.open(filename, PackageAccess.READ);
            XSSFReader xssfReader = new XSSFReader(pkg);
            StylesTable styles = xssfReader.getStylesTable();
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            sheetInputStream = xssfReader.getSheetsData().next();
            processSheet(styles, strings, sheetInputStream);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (sheetInputStream != null) {
                try {
                    sheetInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (pkg != null) {
                try {
                    pkg.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void processSheet(StylesTable styles, ReadOnlySharedStringsTable strings, InputStream sheetInputStream) throws SAXException, ParserConfigurationException, IOException {
        XMLReader sheetParser = SAXHelper.newXMLReader();
        Assert.notNull(handler, "请调用setHandler方法");
        sheetParser.setContentHandler(new XSSFSheetXMLHandler(styles, strings, handler, false));
        sheetParser.parse(new InputSource(sheetInputStream));
    }

	public static void main(String[] args) throws AWTException {

		File file  = new File("");
		List<Integer> errorList = new ArrayList<>();
        List<Integer> temp = new ArrayList<>();
        try {
            String name = file.getOriginalFilename();
            if (!name.endsWith(".xls") && !name.endsWith(".xlsx")) {
                result.setError("请上传excel文件");
                return result;
            }
            new BigExcelUtil(file.getInputStream()).setHandler(new BigSheetContentsHandler(WatermeterExcel.class) {
                @Override
                public void endRow(int i) {
                    try {
                        if (flag) {
                            errorList.add(i);
                        } else if (i > 0) { //一般第一行都是标题，所以直接从第二行开始 从0开始计数
                            WatermeterExcel watermeterExcel = (WatermeterExcel) model;  //将读取的那列数据转换成指定实体类，字段必须一一对应
                            if (service.isHaveCode(watermeterExcel.getCode()) || (watermeterExcel.getCode().length()!=14 || watermeterExcel.getCode().length()!=10)) {
                                errorList.add(i);
                            } else {
                                // 业务操作
                            }
                        }
                    } catch (Exception e) {
                        errorList.add(i); //如果报错 记录报错行数
                    }
                }

            }).parse();
        } catch (Exception e) {
            e.printStackTrace();
        }
	} 
}

import com.signs.util.ExcelNull;

public class WatermeterExcel {

    private String code;

    @ExcelNull  //有这个注解时 读取excel该字段不能为空
    private String totalCode;

    private String collectorCode;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTotalCode() {
        return totalCode;
    }

    public void setTotalCode(String totalCode) {
        this.totalCode = totalCode;
    }

    public String getCollectorCode() {
        return collectorCode;
    }

    public void setCollectorCode(String collectorCode) {
        this.collectorCode = collectorCode;
    }
}