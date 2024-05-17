package controller;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Enumeration;
import java.net.URL;

import javax.servlet.*;
import javax.servlet.http.*;
public class FrontController extends HttpServlet{

    public static List<String> scanControllers(String packageName) {
        List<String> controllersName = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equals("file")) {
                    File dir = new File(resource.getFile());
                    for (File file : Objects.requireNonNull(dir.listFiles())) {
                        String className = file.getName().replaceAll(".class$", "");
                        Class<?> clazz = Class.forName(packageName + "." + className);
                        if (clazz.isAnnotationPresent(Annotation.class)) {
                            controllersName.add(clazz.getName());
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return controllersName;
    }
    private String getControllerPackageName() {
        return getServletContext().getInitParameter("controller");
    }   

    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException { 
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        out.println("URL:" + req.getRequestURL().toString());
        
        out.close();
}
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        processRequest(req, res);
        String packageName = getControllerPackageName();
        List<String> controllers = scanControllers(packageName);

    }    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         processRequest(request, response); 
         String packageName = getControllerPackageName();
         List<String> controllers = scanControllers(packageName);

    
}
}