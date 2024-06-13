package file_mapping;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class MappingDetector {

    MappingTestFile testFile;
    String productionFileName, productionFilePath, productionFileFolder;
    boolean ignoreFile;

    public MappingDetector() {
        productionFileName = "";
        productionFilePath = "";
        productionFileFolder = "";
        ignoreFile = false;
    }


    public MappingTestFile detectMapping(String testFilePath) throws IOException {
        String[] data = testFilePath.split(",");
        testFile = new MappingTestFile(data[0], data[1]);
        String[] testData = data[1].split("/");
        productionFileFolder = testData[testData.length - 2];
        int index = testFile.getFileName().toLowerCase().lastIndexOf("test");
        // System.out.println("index: " + testFile.getFileName().toLowerCase());

        if (index == 0) {
            //the name of the test file starts with the name 'test'
            productionFileName = testFile.getFileName().substring(4);
        } else if (index > 0) {
            //the name of the test file ends with the name 'test'
            productionFileName = testFile.getFileName().substring(0, index) + ".java";
        } else {
            // test file name does not contain the name 'test'
            testFile.setProductionFilePath("");
            return testFile;
        }

        Path startDir = Paths.get(testFile.getProjectRootFolder());
        Files.walkFileTree(startDir, new FindJavaTestFilesVisitor());

        if (isFileSyntacticallyValid(productionFilePath)) {
            testFile.setProductionFilePath(productionFilePath);
        }
        else {
            testFile.setProductionFilePath("");
        }

        return testFile;
    }

    /**
     * Determines if the identified production file is syntactically correct by parsing it and generating its AST
     *
     * @param filePath of the production file
     */
    private boolean isFileSyntacticallyValid(String filePath) {
        boolean valid = false;
        ignoreFile = false;

        if (filePath.length() != 0) {
            try {
                FileInputStream fTemp = new FileInputStream(filePath);
                CompilationUnit compilationUnit = JavaParser.parse(fTemp);
                ClassVisitor classVisitor;
                classVisitor = new ClassVisitor();
                classVisitor.visit(compilationUnit, null);
                valid = !ignoreFile;
            } catch (Exception error) {
                valid = false;
            }

        }

        return valid;
    }

    public class FindJavaTestFilesVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attrs)
                throws IOException {
            String parentFolderPath = file.getParent().toString();
            String parentFolderName = parentFolderPath.substring(parentFolderPath.lastIndexOf("/")+1);
            if (file.getFileName().toString().toLowerCase().equals(productionFileName.toLowerCase())
                    && parentFolderName.toLowerCase().equals(productionFileFolder.toLowerCase())) {
//                System.out.println(parentFolderName.toLowerCase() + " --- " + productionFileFolder.toLowerCase());
                productionFilePath = file.toAbsolutePath().toString();
                return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Visitor class
     */
    private class ClassVisitor extends VoidVisitorAdapter<Void> {

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            ignoreFile = n.isInterface();
            super.visit(n, arg);
        }

        @Override
        public void visit(AnnotationDeclaration n, Void arg) {
            ignoreFile = true;
            super.visit(n, arg);
        }
    }

}