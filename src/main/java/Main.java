import file_detector.FileWalker;
import file_mapping.MappingResultsWriter;
import file_mapping.MappingTestFile;
import file_mapping.MappingDetector;
import testsmell.AbstractSmell;
import testsmell.ResultsWriter;
import testsmell.TestFile;
import testsmell.TestSmellDetector;
import thresholds.DefaultThresholds;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Main {
    static List<MappingTestFile> testFiles;
    static String repoName;

    public static void detectMappings(String projectDir, String srcDir) throws IOException {
        File inputFile = new File(projectDir);
        if(!inputFile.exists() || !inputFile.isDirectory()) {
            System.out.println("Please provide a valid path to the project directory");
            return;
        }

        File srcFolder;
        if(!Objects.equals(srcDir, "")){
            srcFolder = new File(srcDir);
        } else {
            srcFolder = new File(inputFile, "src/main");
        }
        if(!srcFolder.exists() || !srcFolder.isDirectory()) {
            System.out.println("Please provide a valid path to the source directory");
            return;
        }
        final String rootDirectory = projectDir;
        repoName = rootDirectory.substring(rootDirectory.lastIndexOf("repos/")+6);
        MappingDetector mappingDetector;
        FileWalker fw = new FileWalker();
        List<Path> files = fw.getJavaTestFiles(rootDirectory, true);
        testFiles = new ArrayList<>();
        for (Path testPath : files) {
            mappingDetector = new MappingDetector();
            String str =  srcFolder.getAbsolutePath()+","+testPath.toAbsolutePath();
            testFiles.add(mappingDetector.detectMapping(str));
        }

        System.out.println("Saving results. Total lines:" + testFiles.size());
        MappingResultsWriter resultsWriter = MappingResultsWriter.createResultsWriter(repoName);
        List<String> columnValues = null;
        for (int i = 0; i < testFiles.size(); i++) {
            columnValues = new ArrayList<>();
            columnValues.add(0, testFiles.get(i).getTestFilePath());
            columnValues.add(1, testFiles.get(i).getProductionFilePath());
            resultsWriter.writeLine(columnValues);
        }

        System.out.println("Test File Mapping Completed!");
    }

    public static void detectSmells() throws IOException {
        TestSmellDetector testSmellDetector = new TestSmellDetector(new DefaultThresholds());
        String inputFile = MessageFormat.format("{0}/{1}.{2}", "results/mappings", repoName, "csv");

        /*
          Read the input file and build the TestFile objects
         */
        BufferedReader in = new BufferedReader(new FileReader(inputFile));
        String str;

        String[] lineItem;
        TestFile testFile;
        List<TestFile> testFiles = new ArrayList<>();
        while ((str = in.readLine()) != null) {
            // use comma as separator
            lineItem = str.split(",");
//            System.out.println("line: " + lineItem[0] + " - " + lineItem[1]);
            //check if the test file has an associated production file
            if (lineItem.length == 2) {
                testFile = new TestFile(lineItem[0], lineItem[1], "");
            } else {
                testFile = new TestFile(lineItem[0], lineItem[1], lineItem[2]);
            }

            testFiles.add(testFile);
        }

        /*
          Initialize the output file - Create the output file and add the column names
         */
        ResultsWriter resultsWriter = ResultsWriter.createResultsWriter(repoName);
        List<String> columnNames;
        List<String> columnValues;

        columnNames = testSmellDetector.getTestSmellNames();
        columnNames.add(0, "App");
        columnNames.add(1, "TestClass");
        columnNames.add(2, "TestFilePath");
        columnNames.add(3, "ProductionFilePath");
        columnNames.add(4, "RelativeTestFilePath");
        columnNames.add(5, "RelativeProductionFilePath");
        columnNames.add(6, "NumberOfMethods");

        resultsWriter.writeColumnName(columnNames);

        /*
          Iterate through all test files to detect smells and then write the output
        */
        TestFile tempFile;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date;
        for (TestFile file : testFiles) {
            date = new Date();
            System.out.println(dateFormat.format(date) + " Processing: " + file.getTestFilePath());
            System.out.println("Processing: " + file.getTestFilePath());

            //detect smells
            tempFile = testSmellDetector.detectSmells(file);

            //write output
            columnValues = new ArrayList<>();
            columnValues.add(file.getApp());
            columnValues.add(file.getTestFileName());
            columnValues.add(file.getTestFilePath());
            columnValues.add(file.getProductionFilePath());
            columnValues.add(file.getRelativeTestFilePath());
            columnValues.add(file.getRelativeProductionFilePath());
            columnValues.add(String.valueOf(file.getNumberOfTestMethods()));
            for (AbstractSmell smell : tempFile.getTestSmells()) {
                try {
                    columnValues.add(String.valueOf(smell.getNumberOfSmellyTests()));
                } catch (NullPointerException e) {
                    columnValues.add("");
                }
            }
            resultsWriter.writeLine(columnValues);
        }

        System.out.println("Smell Detection Finished");
    }

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Paths.get("results/mappings"));
        Files.createDirectories(Paths.get("results/smells"));

        if (args == null || args.length == 0) {
            System.out.println("Please provide the path to the project directory");
            return;
        }
        if (!args[0].isEmpty()) {
            if(args.length>1 && !args[1].isEmpty()){
                detectMappings(args[0], args[1]);
            } else {
                detectMappings(args[0], "");
            }
        }
        detectSmells();
    }
}
