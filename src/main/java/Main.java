import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import file_detector.FileWalker;
import file_mapping.MappingResultsWriter;
import file_mapping.MappingTestFile;
import file_mapping.MappingDetector;
import testsmell.*;
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

    public static void detectMappings(String projectDir, List<String> searchStrings, String rootDirectoryName) throws IOException {
        File inputFile = new File(projectDir);
        if(!inputFile.exists() || !inputFile.isDirectory()) {
            System.out.println("Please provide a valid path to the project directory");
            return;
        }

        // The project may have multiple source directories, so list them
        List<File> srcFolders = new ArrayList<>();
        findSrcFolders(inputFile, srcFolders);

        final String rootDirectory = projectDir;
        repoName = rootDirectory.substring(rootDirectory.lastIndexOf(rootDirectoryName) + rootDirectoryName.length() + 1);
        System.out.println("Parsing project: " + repoName);

        // Retrieve test code files
        FileWalker fw = new FileWalker();
        List<Path> files = fw.getJavaTestFiles(rootDirectory, true, searchStrings);

        // Map test code to production code
        testFiles = new ArrayList<>();
        for (File srcFolder : srcFolders) {
            List<Path> specificTestFiles = getTestFilesUnderSrcFolder(srcFolder.toPath(), files);
            for (Path testPath : specificTestFiles) {
                MappingDetector mappingDetector = new MappingDetector();
                String str = srcFolder.getAbsolutePath() + "," + testPath.toAbsolutePath();
                MappingTestFile mappingTestFile = mappingDetector.detectMapping(str);
                testFiles.add(mappingTestFile);
            }
        }

        System.out.println("Saving results. Total lines: " + testFiles.size());
        MappingResultsWriter resultsWriter = MappingResultsWriter.createResultsWriter(repoName);
        List<String> columnValues = null;
        for (int i = 0; i < testFiles.size(); i++) {
            columnValues = new ArrayList<>();
            columnValues.add(0, testFiles.get(i).getTestFilePath());
            columnValues.add(1, testFiles.get(i).getProductionFilePath());
            resultsWriter.writeLine(columnValues);
        }

        System.out.println("Test File Mapping Completed to \n    results/mappings/" + repoName + ".csv");
    }

    // Retrieve test files under a specific source directory
    private static List<Path> getTestFilesUnderSrcFolder(Path srcFolder, List<Path> allTestFiles) {
        List<Path> specificTestFiles = new ArrayList<>();
        for (Path testFile : allTestFiles) {
            if (testFile.startsWith(srcFolder.getParent())) { // HACK: Removing '/main' from the srcFolder path
                specificTestFiles.add(testFile);
            }
        }
        return specificTestFiles;
    }

    // Find 'src/main' directories in the project
    private static void findSrcFolders(File file, List<File> srcFolders) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                if (f.isDirectory()) {
                    if (f.getName().equals("src")) {
                        // When the src folder is found, look for the main directory
                        File mainDir = new File(f, "main"); // NOTE: Change this part manually during analysis (e.g., "org" for argouml)
                        if (mainDir.exists() && mainDir.isDirectory()) {
                            srcFolders.add(mainDir);
                        }
                    }
                    // Recursively call for subdirectories
                    findSrcFolders(f, srcFolders);
                }
            }
        }
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
        SmellRecorder smellRecorder = new SmellRecorder();
        for (TestFile file : testFiles) {
            date = new Date();
            // System.out.println(dateFormat.format(date) + " Processing: " + file.getTestFilePath());
            // System.out.println("Processing: " + file.getTestFilePath());

            //detect smells
            tempFile = testSmellDetector.detectSmells(file);
            smellRecorder.addTestFileData(file);

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
        smellRecorder.recordSmells(repoName);
        System.out.println("Smell Detection Finished to \n    results/smells/" + repoName + ".csv");
    }

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Paths.get("results/mappings"));
        Files.createDirectories(Paths.get("results/smells"));


        String projectDir = args[0];
        String rootDirectoryName = args[1];
        int stringCount = Integer.parseInt(args[2]);
        List<String> searchStrings = new ArrayList<>();
        for (int i = 0; i < stringCount; i++) {
            searchStrings.add(args[i + 3]);
        }

        detectMappings(projectDir,  searchStrings, rootDirectoryName);
        detectSmells();
    }
}
