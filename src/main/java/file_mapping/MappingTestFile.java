package file_mapping;

public class MappingTestFile {
    String[] data;
    private String testFilePath, productionFilePath, srcDirectory;

    public MappingTestFile(String srcDirectory, String testFilePath) {
        this.testFilePath = testFilePath;
        this.srcDirectory = srcDirectory;
        if (testFilePath.contains("\\")) {
            data = this.testFilePath.split("\\\\");
        } else {
            data = this.testFilePath.split("/");
        }

//        System.out.println("test data " + data[1]);
    }

    public String getFileName() {
        return data[data.length - 1];
    }

    public String getTestFilePath() {
        return testFilePath;
    }

    public String getProductionFilePath() {
        return productionFilePath;
    }

    public void setProductionFilePath(String productionFilePath) {
        this.productionFilePath = productionFilePath;
    }

    public String getProjectRootFolder() {
        return srcDirectory;
    }

}

