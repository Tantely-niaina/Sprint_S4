package frameworks;

import annotations.Get;
import util.Mapping;
import util.Methode;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    public HashMap<String, Mapping> hashmap;
    public List<Class<?>> controllers;
    public List<String> controllersName;
    public Methode methode;
    public String url;
    public Object result;

    public  Mapping mapping;

    @Override
    public void init() throws ServletException {
        super.init();
        methode = new Methode();
        String packageName = getControllerPackageName();
        controllers = methode.scanControllers(packageName);
        if (controllers.isEmpty()) {
            throw new ServletException("No controllers found in the package 'controllers'");
        }
        controllersName = methode.getClassName(controllers);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        //String urlString = request.getRequestURL().toString();
        url = methode.getUrlAfterSprint(request);
        hashmap = methode.urlMethod(controllers, url);
        mapping = methode.getMapping(hashmap);
        if (mapping == null) {
            throw new ServletException("No mapping found for URL: " + url);
        }

        result = methode.execute(mapping, request);

        if (result instanceof String && ((String) result).startsWith("<html>")) {
            // C'est notre réponse HTML d'erreur
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print((String) result);
            out.flush();
            return;
        }

        if (result == null) {
            throw new ServletException("No result returned from controller method for URL: " + url);
        }
        if (result instanceof String) {
            if (methode.isJsonResponse(methode.getMapping(hashmap))) {
                sendJsonResponse(response, (String) result);
            } else {
                request.setAttribute("value", result);
                forwardToJsp(request, response);
            }
        } else if (result instanceof ModelView) {
            ModelView mv = (ModelView) result;
            if (methode.isJsonResponse(methode.getMapping(hashmap))) {
                sendJsonResponse(response, methode.convertToJson(mv.getData()));
            } else {
                request.setAttribute("data", mv.getSingleValue());
                request.getRequestDispatcher(mv.getUrl()).forward(request, response);
            }
        } else if (result != null) {
            // Assume any other non-null result from a @Restapi method should be sent as JSON
            if (methode.isJsonResponse(methode.getMapping(hashmap))) {
                sendJsonResponse(response, methode.convertToJson(result));
            } else {
                throw new ServletException("Unexpected return type for non-Restapi method");
            }
        } else {
            throw new ServletException("No result returned from controller method");
        }
    }

    private void sendJsonResponse(HttpServletResponse response, String jsonContent) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(jsonContent);
        out.flush();
    }

    private void forwardToJsp(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("hashmap", hashmap);
        request.setAttribute("url", request.getRequestURL().toString());
        request.setAttribute("controllers", controllersName);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException e) {
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
