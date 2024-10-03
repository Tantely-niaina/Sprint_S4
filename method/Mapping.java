package util;

public class Mapping {
    private String className;

    private String methodName;

    private String verb;

    public Mapping (String className, String methodName, String verb) {
        this.className = className;
        this.methodName = methodName;
        this.verb = verb;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getVerb() {
        return verb;
    }

}
