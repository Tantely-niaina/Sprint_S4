package util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Part;

import javax.naming.CannotProceedException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;

import annotations.AnnotationController;
import annotations.Get;
import annotations.Param;
import annotations.RestApi;
import annotations.Post;

import frameworks.ModelView;
import frameworks.MySession;

import static java.lang.System.out;

public class Methode {

    public List<Class<?>> scanControllers(String packageName) {
        List<Class<?>> controllers = new ArrayList<>();

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            URL resource = classLoader.getResource(path);
            if (resource == null) {
                System.err.println("No resource found for path: " + path);
                return controllers;
            }

            File directory = new File(resource.getFile());
            if (!directory.exists()) {
                System.err.println("Directory does not exist: " + directory.getAbsolutePath());
                return controllers;
            }

            File[] files = directory.listFiles();
            if (files == null) {
                System.err.println("No files found in directory: " + directory.getAbsolutePath());
                return controllers;
            }

            for (File file : files) {
                if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(AnnotationController.class)) {
                        controllers.add(clazz);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error scanning controllers: " + e.getMessage());
        }

        return controllers;
    }

    public List<String> getClassName(List<Class<?>> controllers) {
        List<String> controllersName = new ArrayList<>();
        for (Class<?> clazz : controllers) {
            if (clazz != null) {
                controllersName.add(clazz.getSimpleName());
            }
        }
        return controllersName;
    }

    public HashMap<String, Mapping> urlMethod(List<Class<?>> controllers, String url) {
        HashMap<String, Mapping> hashMap = new HashMap<>();
        for (Class<?> controller : controllers) {
            Method[] declaredMethods = controller.getDeclaredMethods();
            List<VerbMethod> verbMethods = new ArrayList<>();
            for (Method method : declaredMethods) {
                if (method.isAnnotationPresent(annotations.URL.class)) {
                    annotations.URL urlAnnotation = method.getAnnotation(annotations.URL.class);
                    if(urlAnnotation.value().equals(url)) {
                        VerbMethod verbMethod = new VerbMethod();
                        verbMethod.setMethod(method.getName());
                        if (method.isAnnotationPresent(Post.class)) {
                            verbMethod.setVerb("POST");
                        } else if (method.isAnnotationPresent(Get.class)) {
                            verbMethod.setVerb("GET");
                        } else {
                            verbMethod.setVerb("GET"); // Default to GET if no annotation
                        }
                        verbMethods.add(verbMethod);
                    }
                }
            }
            if (!verbMethods.isEmpty()) {
                Mapping mapping = new Mapping(controller.getName(), verbMethods);
                hashMap.put(url, mapping);
            }
        }
        return hashMap;
    }
    public Mapping getMapping(HashMap<String, Mapping> hashMap) {
        for (Map.Entry<String, Mapping> entry : hashMap.entrySet()) {
            return entry.getValue();
        }
        return null;
    }

    public Object execute(Mapping mapping, HttpServletRequest request)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException,
            IllegalAccessException, ServletException{
        if (mapping != null) {
            String className = mapping.getClassName();
            Class<?> clazz = Class.forName(className);
            System.out.println("Executing method in class: " + className);

            // Find the method that matches the HTTP verb
            Method method = null;
            String requestVerb = request.getMethod();
            VerbMethod matchingVerbMethod = null;
            for (VerbMethod verbMethod : mapping.getVerbMethods()) {
                if (verbMethod.getVerb().equalsIgnoreCase(requestVerb)) {
                    method = getMethod(clazz, verbMethod.getMethod(), request);
                    matchingVerbMethod = verbMethod;
                    break;
                }
            }

            if (method == null || matchingVerbMethod == null) {
                return createErrorHtml("400 method not found for: " + requestVerb);
            }

            // Vérifier si c'est une requête multipart
            boolean isMultipart = request.getContentType() != null &&
                    request.getContentType().toLowerCase().startsWith("multipart/form-data");

            Object[] parameterValues = new Object[method.getParameterCount()];
            Parameter[] parameters = method.getParameters();

            try {
                if (isMultipart) {
                    for (int i = 0; i < parameters.length; i++) {
                        Parameter param = parameters[i];
                        Class<?> paramType = param.getType();

                        if (paramType == Part.class) {
                            Param paramAnnotation = param.getAnnotation(Param.class);
                            if (paramAnnotation != null) {
                                String paramName = paramAnnotation.name();
                                Part filePart = request.getPart(paramName);
                                parameterValues[i] = filePart;
                            }
                        }
                    }
                } else {
                    // Traitement des requêtes normales (non-multipart)
                    List<String> formFieldsNames = getFieldsNamesList(request);
                    Employe emp = new Employe();
                    boolean empPopulated = false;

                    for (int i = 0; i < parameters.length; i++) {
                        Parameter param = parameters[i];
                        Class<?> paramType = param.getType();

                        if (paramType == MySession.class) {
                            parameterValues[i] = new MySession(request.getSession());
                        } else if (i < formFieldsNames.size()) {
                            String fieldName = formFieldsNames.get(i);
                            if (fieldName.contains(".")) {
                                // Traitement des objets complexes (comme Employe)
                                while (i < formFieldsNames.size() && formFieldsNames.get(i).contains(".")) {
                                    populateEmploye(request, formFieldsNames.get(i), emp);
                                    empPopulated = true;
                                    i++;
                                }
                                i--; // Ajustement pour la boucle externe
                            } else {
                                // Traitement des paramètres simples
                                parameterValues[i] = request.getParameter(fieldName);
                            }
                        }
                    }

                    // Assigner l'objet Employe si nécessaire
                    if (empPopulated) {
                        for (int i = 0; i < parameterValues.length; i++) {
                            if (parameterValues[i] == null && parameters[i].getType() == Employe.class) {
                                parameterValues[i] = emp;
                                break;
                            }
                        }
                    }
                }

                // Exécution de la méthode
                Object instance = clazz.getDeclaredConstructor().newInstance();
                Object result;

                if (method.getParameterCount() == 0) {
                    result = method.invoke(instance);
                } else {
                    result = method.invoke(instance, parameterValues);
                }

                // Traitement du résultat
                if (method.isAnnotationPresent(RestApi.class)) {
                    if (result instanceof ModelView) {
                        ModelView mv = (ModelView) result;
                        return convertToJson(mv.getData());
                    } else {
                        return convertToJson(result);
                    }
                } else {
                    return result;
                }

            } catch (IOException e) {
                System.err.println("Error processing file upload: " + e.getMessage());
                return createErrorHtml("500 Error processing upload: " + e.getMessage());
            }
        }

        System.out.println("Mapping not found");
        throw new ServletException("No mapping found for the requested URL");
    }

    // Méthode utilitaire pour convertir les valeurs des paramètres
    private Object convertParameterValue(String value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }

        return value;
    }
    private String createErrorHtml(String message) {
        return "<html><body><h1>Error</h1><p>" + message + "</p></body></html>";
    }
    private void populateEmploye(HttpServletRequest request, String parameterName, Employe emp)
            throws IllegalAccessException {
        Class<?> clazzemp = emp.getClass();
        Field[] fields = clazzemp.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            if (parameterName.endsWith(field.getName())) {
                String value = request.getParameter(parameterName);
                if (field.getType() == int.class) {
                    field.setInt(emp, Integer.parseInt(value));
                } else if (field.getType() == String.class) {
                    field.set(emp, value);
                }
            }
        }
    }

    private Method getMethod(Class<?> clazz, String methodName, HttpServletRequest request)
            throws NoSuchMethodException {
        Method[] methods = clazz.getMethods();

        // Vérifier si c'est une requête multipart
        boolean isMultipart = request.getContentType() != null &&
                request.getContentType().toLowerCase().startsWith("multipart/form-data");

        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Parameter[] parameters = method.getParameters();

                // Cas spécial pour les requêtes multipart
                if (isMultipart) {
                    boolean hasPartParameter = false;
                    for (Parameter param : parameters) {
                        if (param.getType() == Part.class) {
                            hasPartParameter = true;
                            break;
                        }
                    }
                    if (hasPartParameter) {
                        return method;
                    }
                    continue;
                }

                // Pour les requêtes non-multipart
                List<String> parameterNames = getFieldsNamesList(request);
                boolean matches = true;
                int nonSessionParamCount = 0;

                // Compter les paramètres non-MySession
                for (Parameter param : parameters) {
                    if (param.getType() != MySession.class) {
                        nonSessionParamCount++;
                    }
                }

                if (parameterNames.size() != nonSessionParamCount) {
                    continue;
                }

                int formParamIndex = 0;
                for (Parameter param : parameters) {
                    if (param.getType() == MySession.class) {
                        continue;
                    }

                    Param paramAnnotation = param.getAnnotation(Param.class);
                    if (paramAnnotation != null) {
                        if (formParamIndex < parameterNames.size()) {
                            String paramValue = request.getParameter(paramAnnotation.name());
                            if (paramValue == null && paramAnnotation.required()) {
                                matches = false;
                                break;
                            }
                            formParamIndex++;
                        }
                    }
                }

                if (matches) {
                    return method;
                }
            }
        }

        throw new NoSuchMethodException("No such method found with the given name and parameter names.");
    }

    public String getUrlAfterSprint(HttpServletRequest request) {
        // Extract the part after /sprint1
        String contextPath = request.getContextPath(); // This should be "/sprint1"
        String uri = request.getRequestURI(); // This should be "/sprint1/hola"
        return uri.substring(contextPath.length()); // This should be "/hola"
    }

    public List<String> getFieldsNamesList(HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();

        List<String> parameterNamesList = new ArrayList<>();

        while (parameterNames.hasMoreElements()) {
            parameterNamesList.add(parameterNames.nextElement());
        }

        return parameterNamesList;
    }

    // Liste des champs du formulaire
    public boolean paramSize(Method method, List<String> FormFieldsNames) {
        Parameter[] parameters = method.getParameters();
        int formFieldCount = FormFieldsNames.size();
        int methodParamCount = parameters.length;
        int specialParamCount = 0;

        for (Parameter param : parameters) {
            if (param.getType() == MySession.class) {
                specialParamCount++;
            }
        }

        if (isObject(FormFieldsNames)) {
            // Logique existante pour les objets
            int argumentCount = 0;
            for (Parameter parameter : parameters) {
                if (parameter.getType().isPrimitive() || parameter.getType() == String.class) {
                    argumentCount++;
                    continue;
                }
                if (parameter.getType() == MySession.class) {
                    continue; // Ne pas compter MySession comme un argument de formulaire
                }
                Class<?> argClass = parameter.getType();
                Field[] fields = argClass.getDeclaredFields();
                argumentCount += fields.length;
            }
            return argumentCount == formFieldCount;
        } else {
            // Comparer le nombre de champs de formulaire au nombre de paramètres
            // de méthode, en excluant les paramètres spéciaux comme MySession
            return formFieldCount == (methodParamCount - specialParamCount);
        }
    }

    public boolean isObject(List<String> FormFieldsNames) {
        boolean isObjet = false;
        for (String paramName : FormFieldsNames) {
            if (paramName.contains(".")) {
                isObjet = true;
                break;
            }
        }
        return isObjet;
    }

    public String convertToJson(Object objet) {
        Gson gson = new Gson();
        return gson.toJson(objet);
    }

    public boolean isJsonResponse(Mapping mapping) throws ClassNotFoundException, NoSuchMethodException {
        if (mapping == null) {
            return false;
        }
        Class<?> clazz = Class.forName(mapping.getClassName());

        for (VerbMethod verbMethod : mapping.getVerbMethods()) {
            Method method = null;
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(verbMethod.getMethod())) {
                    method = m;
                    break;
                }
            }
            if (method != null && method.isAnnotationPresent(RestApi.class)) {
                return true;
            }
        }

        return false;
    }
}
