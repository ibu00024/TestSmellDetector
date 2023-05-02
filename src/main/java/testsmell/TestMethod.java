package testsmell;

import java.util.HashMap;
import java.util.Map;

public class TestMethod extends SmellyElement {

    private String methodName;
    private int beginLine;
    private int endLine;
    private boolean hasSmell;
    private Map<String, String> data;

    public TestMethod(String methodName, int beginLine, int endLine) {
        this.methodName = methodName;
        this.beginLine = beginLine;
        this.endLine = endLine;
        data = new HashMap<>();
    }

    public void setSmell(boolean hasSmell) {
        this.hasSmell = hasSmell;
    }

    public void addDataItem(String name, String value) {
        data.put(name, value);
    }
    @Override
    public int getBeginLine() { return beginLine; }
    @Override
    public int getEndLine() { return endLine; }
    @Override
    public String getElementName() {
        return methodName;
    }

    @Override
    public boolean isSmelly() {
        return hasSmell;
    }

    @Override
    public Map<String, String> getData() {
        return data;
    }
}
