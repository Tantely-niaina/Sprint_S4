package util;

import annotations.AnnotationController;
import annotations.Get;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            for (Method method : declaredMethods) {
                if (method.isAnnotationPresent(Get.class)) {
                    Get getAnnotation = method.getAnnotation(Get.class);
                    if (getAnnotation.value().equals(url)) {
                        Mapping mapping = new Mapping(controller.getName(), method.getName());
                        hashMap.put(getAnnotation.value(), mapping);
                    }
                }
            }
        }
        return hashMap;
    }

    public Mapping getMapping(HashMap<String, Mapping> hashMap) {
        for (Map.Entry<String, Mapping> entry : hashMap.entrySet()) {
            Mapping map = entry.getValue();
            return map;
        }
        return null;
    }

    public String getUrlAfterSprint1(HttpServletRequest request) {
        // Extract the part after /sprint1
        String contextPath = request.getContextPath(); // This should be "/sprint1"
        String uri = request.getRequestURI(); // This should be "/sprint1/holla"
        return uri.substring(contextPath.length()); // This should be "/holla"
    }

    public void execute(Mapping mapping) throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        if (mapping != null) {
            String classname = mapping.getClassName();
            String methodName = mapping.getMethodName();

            Class<?> clazz = Class.forName(classname);
            Method method = clazz.getMethod(methodName, String.class);

            Object instance = clazz.getDeclaredConstructor().newInstance();

            method.invoke(instance, "bonjour tous le monde");
        } else {
            System.out.println("mapping not found");
        }
    }

}
