package com.jbhunt.personnel.team.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import com.jbhunt.personnel.team.constants.TeamCommonConstants;
import com.jbhunt.personnel.team.dto.TeamsDTO;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

@Slf4j
@Component
public class TeamManagementReport {

    private final ServletContext context;

    public TeamManagementReport(final ServletContext context) {
        this.context = context;
    }

    public void jasperReport(List<TeamsDTO> list, final HttpServletResponse response) throws JRException, IOException {
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(list);
        Map<String, Object> map = new HashMap<>();
        final String jrxmlPath = context.getRealPath(TeamCommonConstants.TEAM_JRXML_FILE_PATH);
        JasperDesign jasperDesign = JRXmlLoader.load(jrxmlPath);
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, map, beanColDataSource);
        // Export report to excel format
        JRXlsxExporter exporter = new JRXlsxExporter();
        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setIgnoreCellBorder(false);
        configuration.setWhitePageBackground(false);
        configuration.setRemoveEmptySpaceBetweenRows(true);
        configuration.setRemoveEmptySpaceBetweenColumns(true);
        configuration.setDetectCellType(false);
        configuration.setIgnoreGraphics(false);
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        ByteArrayOutputStream excelReportStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(excelReportStream));
        exporter.setConfiguration(configuration);
        exporter.exportReport();
        response.setContentType(TeamCommonConstants.JASPER_CONTENT_TYPE);
        String fileName = "Team" + LocalDate.now() + ".xlsx";
        response.setHeader(TeamCommonConstants.JASPER_CONTENT_DISPOSITION,
                TeamCommonConstants.JASPER_ATTACHMENT_FILENAME + fileName);
        response.setContentLength(excelReportStream.size());
        final ServletOutputStream out = response.getOutputStream();
        excelReportStream.writeTo(out);
        excelReportStream.close();
    }

}
