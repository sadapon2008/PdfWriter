import java.io.*;

import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRCsvDataSource;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import au.com.bytecode.opencsv.CSVReader;

public class PdfWriter {

    public static void main(String[] args) {
        new PdfWriter().start(args);
    }

    public void start(String[] args) {
        // ファイル名格納用変数
        String filename_datacsv = "";  // データCSVファイル
        String filename_pdf = "";      // 出力PDFファイル
        String filename_paramcsv = ""; // パラメータCSVファイル
        String filename_templatejrxml = ""; // テンプレートJRXMLファイル
        
        // コマンドライン引数を解析する
        Options opts = new Options();
        opts.addOption("t", "templatejrxml", true, "input template jrxml file");
        opts.addOption("d", "datacsv", true, "input data csv file");
        opts.addOption("p", "paramcsv", true, "input parameter csv file");
        opts.addOption("o", "outpdf", true, "output pdf file");
        BasicParser parser = new BasicParser();
        CommandLine cl;
        HelpFormatter help = new HelpFormatter();
        try {
            // 解析する
            cl = parser.parse(opts, args);
            
            filename_datacsv = cl.getOptionValue("d");
            if(filename_datacsv == null) {
                throw new ParseException("");
            }
            filename_pdf = cl.getOptionValue("o");
            if(filename_pdf == null) {
                throw new ParseException("");                
            }
            filename_paramcsv = cl.getOptionValue("p");
            if(filename_paramcsv == null) {
                throw new ParseException("");                                
            }
            filename_templatejrxml = cl.getOptionValue("t");
            if(filename_templatejrxml == null) {
                throw new ParseException("");                                
            }
        }catch (ParseException e) {
            help.printHelp("PdfWriter", opts);
            System.exit(1);
        }
        
        // パラメータCSVファイルからパラメータを読み込み
        // 1行目はヘッダとしてスキップする
        // 列の区切りは水平タブ
        // 行の区切りは改行
        // 2行目以降から1列目をキー2列目をバリューとして読み込む
        Map<String, Object> parameters = new HashMap<String, Object>();
        try {
            FileInputStream input=new FileInputStream(filename_paramcsv);
            InputStreamReader inReader=new InputStreamReader(input, "UTF-8");
            CSVReader reader = new CSVReader(inReader,'\t','"',1);
            String [] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                if(nextLine.length >= 2) {
                    parameters.put(nextLine[0], nextLine[1]);
                }
            }
            reader.close();            
        } catch(Exception e) {
            System.exit(1);
        }
        
        // JasperReportでPDFを生成する
        try {
            // .jrxmlを読み込んでコンパイルする
            JasperReport jasperReport = JasperCompileManager.compileReport(filename_templatejrxml);
            
            // データCSVファイルをデータソースに設定する
            // 1行目はヘッダとしてスキップ
            // 列の区切りは水平タブ
            // 行の区切りはLF
            JRCsvDataSource ds = new JRCsvDataSource(filename_datacsv, "UTF-8");
            ds.setUseFirstRowAsHeader(true);
            ds.setFieldDelimiter('\t');
            ds.setRecordDelimiter("\n");
            
            // ドキュメントを生成
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, ds);
            
            // PDFファイルを出力
            JasperExportManager.exportReportToPdfFile(jasperPrint, filename_pdf);
        } catch (Exception e) {
            System.exit(1);
        }
    }
}
