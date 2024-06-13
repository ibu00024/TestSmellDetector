package file_detector;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;


public class FileWalker {
    private List<Path> files;


    public List<Path> getJavaTestFiles(String directoryPath, boolean recursive, List<String> searchStrings) throws IOException {
        files = new ArrayList<>();
        Path startDir = Paths.get(directoryPath);

        if (recursive) {
            Files.walkFileTree(startDir, new FindJavaTestFilesVisitor(searchStrings));
        } else {
            Files.walk(startDir, 1)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        if (filePath.toString().toLowerCase().endsWith(".java") &&
                                containsSearchString(filePath, searchStrings)) {
                            files.add(filePath);
                        }
                    });
        }
        return files;
    }

    // Determine if the specified string is included in the file path (to detect test code)
    private boolean containsSearchString(Path filePath, List<String> searchStrings) {
        String absolutePath = filePath.toAbsolutePath().toString();
        for (String searchString : searchStrings) {
            if (absolutePath.contains(searchString)) {
                return true;
            }
        }
        return false;
    }

    public class FindJavaTestFilesVisitor extends SimpleFileVisitor<Path> {
        private List<String> searchStrings;

        public FindJavaTestFilesVisitor(List<String> searchStrings) {
            this.searchStrings = searchStrings;
        }

        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attrs)
                throws IOException {
            if (file.toString().toLowerCase().endsWith(".java") &&
                    containsSearchString(file, searchStrings)) {
                files.add(file);
            }
            return FileVisitResult.CONTINUE;
        }
    }

    public List<Path> getJavaFiles(String directoryPath, boolean recursive) throws IOException {
        files = new ArrayList<>();
        Path startDir = Paths.get(directoryPath);

        if (recursive) {
            Files.walkFileTree(startDir, new FindJavaFilesVisitor());
        } else {
            Files.walk(startDir, 1)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        if (filePath.toString().toLowerCase().endsWith(".java")) {
                            files.add(filePath);
                        }
                    });
        }
        return files;
    }

    private class FindJavaFilesVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attrs)
                throws IOException {

            if (file.toString().toLowerCase().endsWith(".java")) {
                files.add(file);
            }

            return FileVisitResult.CONTINUE;
        }
    }
}
