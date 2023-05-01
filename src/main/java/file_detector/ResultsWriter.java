package file_detector;

import com.opencsv.CSVWriter;
import file_detector.entity.ClassEntity;
import file_detector.entity.MethodEntity;

import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ResultsWriter {
    private CSVWriter classCSVWriter, methodCSVWriter, debtCSVWriter;

    public static ResultsWriter createResultsWriter() throws IOException {
        return new ResultsWriter();
    }

    private ResultsWriter() throws IOException {
        String time = String.valueOf(Calendar.getInstance().getTimeInMillis());
        String classFileName = MessageFormat.format("{0}_{1}_{2}.{3}", "Output", "Class", time, "csv");
        String methodFileName = MessageFormat.format("{0}_{1}_{2}.{3}", "Output", "Method", time, "csv");
        String debtFileName = MessageFormat.format("{0}_{1}_{2}.{3}", "Output", "Debt", time, "csv");
        methodCSVWriter = new CSVWriter(new FileWriter(methodFileName), ',');
        classCSVWriter = new CSVWriter(new FileWriter(classFileName), ',');
        debtCSVWriter = new CSVWriter(new FileWriter(debtFileName), ',');

        createClassFile();
        createMethodFile();
        createDebtFile();
    }
    private void createDebtFile() throws IOException {
        List<String[]> fileLines = new ArrayList<String[]>();
        String[] columnNames = {
                "FilePath",
                "FileName",
                "ClassName",
                "Comment"
        };
        fileLines.add(columnNames);

        debtCSVWriter.writeAll(fileLines, false);
        debtCSVWriter.flush();
    }

    private void createClassFile() throws IOException {
        List<String[]> fileLines = new ArrayList<String[]>();
        String[] columnNames = {
                "FilePath",
                "FileName",
                "ClassName",
                "TotalImports",
                "TotalMethods",
                "TotalMethodStatements",
                "TotalTestMethods",
                "AnnotationCount",
                "TestsWithoutAnnotationCount",
                "HasTestInFileName",
                "HasTestInClassName",
                "junitFrameworkTest",
                "junitFrameworkTestCase",
                "orgJunitTest",
                "androidTestAndroidTestCase",
                "androidTestInstrumentationTestCase",
                "orgJunitAssert",
                "androidTestActivityInstrumentationTestCase2",
                "HasTechnicalDebtComment",
        };
        fileLines.add(columnNames);

        classCSVWriter.writeAll(fileLines, false);
        classCSVWriter.flush();
    }

    private void createMethodFile() throws IOException {
        List<String[]> fileLines = new ArrayList<String[]>();
        String[] columnNames = {
                "FilePath",
                "FileName",
                "ClassName",
                "MethodName",
                "TotalStatements",
                "TotalParameters",
                "ReturnType",
                "AccessModifier",
                "MethodHasAnnotation",
                "MethodHasTestInName",
                "FileHasTestInName",
                "ClassHasTestInName"
        };
        fileLines.add(columnNames);

        methodCSVWriter.writeAll(fileLines, false);
        methodCSVWriter.flush();
    }

    public void outputToCSV(ClassEntity classEntity) throws IOException {
        outputClassDetails(classEntity);
        outputMethodDetails(classEntity);
        outputDebtDetails(classEntity);
    }

    private void outputDebtDetails(ClassEntity classEntity) throws IOException {
        List<String[]> fileLines = new ArrayList<String[]>();
        String[] dataLine;
        for (String comment : classEntity.getTechnicalDebtComments()) {

            dataLine = new String[4];
            dataLine[0] = classEntity.getFilePath();
            dataLine[1] = classEntity.getFileName();
            dataLine[2] = classEntity.getClassName();
            dataLine[3] = "\""+comment.replace('"',' ')+"\"";

            fileLines.add(dataLine);
        }
        debtCSVWriter.writeAll(fileLines, false);
        debtCSVWriter.flush();
    }

    public void closeOutputFiles() throws IOException {
        classCSVWriter.close();
        methodCSVWriter.close();
        debtCSVWriter.close();
    }

    private void outputMethodDetails(ClassEntity classEntity) throws IOException {
        List<String[]> fileLines = new ArrayList<String[]>();
        String[] dataLine;
        for (MethodEntity methodEntity : classEntity.getMethods()) {

            dataLine = new String[12];
            dataLine[0] = classEntity.getFilePath();
            dataLine[1] = classEntity.getFileName();
            dataLine[2] = classEntity.getClassName();
            dataLine[3] = methodEntity.getMethodName();
            dataLine[4] = String.valueOf(methodEntity.getTotalStatements());
            dataLine[5] = String.valueOf(methodEntity.getParameterCount());
            dataLine[6] = methodEntity.getReturnType();
            dataLine[7] = methodEntity.getAccessModifier();
            dataLine[8] = String.valueOf(methodEntity.isHasAnnotation());
            dataLine[9] = String.valueOf(methodEntity.isHasTestInName());
            dataLine[10] = String.valueOf(classEntity.isHasTestInFileName());
            dataLine[11] = String.valueOf(classEntity.isHasTestInClassName());

            fileLines.add(dataLine);
        }
        methodCSVWriter.writeAll(fileLines, false);
        methodCSVWriter.flush();
    }

    private void outputClassDetails(ClassEntity classEntity) throws IOException {
        List<String[]> fileLines = new ArrayList<String[]>();
        String[] dataLine;

        dataLine = new String[19];
        dataLine[0] = classEntity.getFilePath();
        dataLine[1] = classEntity.getFileName();
        dataLine[2] = classEntity.getClassName();
        dataLine[3] = String.valueOf(classEntity.getTotalImports());
        dataLine[4] = String.valueOf(classEntity.getTotalMethods());
        dataLine[5] = String.valueOf(classEntity.getTotalMethodStatement());
        dataLine[6] = String.valueOf(classEntity.getTotalTestMethods());
        dataLine[7] = String.valueOf(classEntity.getTestAnnotationCount());
        dataLine[8] = String.valueOf(classEntity.getTestMethodWithoutAnnotationCount());
        dataLine[9] = String.valueOf(classEntity.isHasTestInFileName());
        dataLine[10] = String.valueOf(classEntity.isHasTestInClassName());
        dataLine[11] = String.valueOf(classEntity.getHas_junitframeworkTest());
        dataLine[12] = String.valueOf(classEntity.getHas_junitframeworkTestCase());
        dataLine[13] = String.valueOf(classEntity.getHas_orgjunitTest());
        dataLine[14] = String.valueOf(classEntity.getHas_androidtestAndroidTestCase());
        dataLine[15] = String.valueOf(classEntity.getHas_androidtestInstrumentationTestCase());
        dataLine[16] = String.valueOf(classEntity.getHas_orgjunitAssert());
        dataLine[17] = String.valueOf(classEntity.getHas_androidtestActivityInstrumentationTestCase2());
        dataLine[18] = String.valueOf(classEntity.getHasTechnicalDebtComments());

        fileLines.add(dataLine);

        classCSVWriter.writeAll(fileLines, false);
        classCSVWriter.flush();
    }
}
