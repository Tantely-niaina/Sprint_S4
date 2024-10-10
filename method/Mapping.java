package util;

import java.util.List;

public class Mapping {
    private String className;

    private List<VerbMethod> verbMethods;

    public Mapping (String className, List<VerbMethod> verbMethods) {
        this.className = className;
        this.verbMethods = verbMethods;
    }

    public String getClassName() {
        return className;

    }

    public List<VerbMethod> getVerbMethods() {
        return verbMethods;
    }

    public void setVerbMethods(List<VerbMethod> verbMethods) {
        this.verbMethods = verbMethods;
    }
}
