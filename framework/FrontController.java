package frameworks;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    public HashMap<String, Mapping> mapping;
    public List<Class<?>> controllers;
    public List<String> controllersName;
    public Methode methode;
    public String url;
      public Object[] params;
    public String parametre;
    public String value;
    public Object valeur;

    @Override
    public void init() throws ServletException {
        super.init();
        methode = new Methode();
        String packageName = getControllerPackageName();
        controllers = methode.scanControllers(packageName);
        controllersName = methode.getClassName(controllers);
          parametre = "mercie de votre attention";
        valeur = "coucou";
        value = "mon amie";
        url = "/sprint.jsp";
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {

        String urlString = request.getRequestURL().toString();
        Object result = null;
        url = methode.getUrlAfterSprint1(request);
      

        mapping = methode.urlMethod(controllers, url);
        
      

                if (url.equals("/bonjour")) {
            params = new Object[]{parametre};
        } else if (url.equals("/bonsoir")) {
            params = new Object[]{valeur, value, url};
        }

        result = methode.execute(methode.getMapping(mapping), params);

        if(result instanceof String) {
            request.setAttribute("value", result);
        } else if (result instanceof ModelView) {
            request.setAttribute("data", ((ModelView) result).getdata());
            request.getRequestDispatcher(((ModelView) result).geturl()).forward(request, response);
        } else {
            throw new NoSuchMethodException("No such method found with the given name and parameter count.");
        }

        

        request.setAttribute("mapping", mapping);
        request.setAttribute("url", urlString);
        request.setAttribute("controllers", controllersName);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
        
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private String getControllerPackageName() {
        ServletConfig cg = getServletConfig();
        return cg.getInitParameter("controller-package");
    }

    @Override
    public String getServletInfo() {
        return "FrontController Servlet";
    }
}
