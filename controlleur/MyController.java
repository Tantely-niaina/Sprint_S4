package controllers;

import annotations.*;
import frameworks.ModelView;
import frameworks.MySession;
import javax.servlet.http.Part;
import util.Employe;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

@AnnotationController
public class MyController {

    @Get
    @URL(value = "/login")
    @RestApi
    public ModelView login(@Param(name = "username") String username,
            @Param(name = "password") String password,
            MySession session) {
        ModelView mv = new ModelView();
        if ("user1".equals(username) && "pass1".equals(password) ||
                "user2".equals(username) && "pass2".equals(password)) {
            session.add("user", username);
            mv.setUrl("/userList.jsp");
            // mv.addObject("userList", userLists.get(username));
        } else {
            mv.setUrl("/index.jsp");
            mv.addObject("error", "Invalid username or password");
        }
        return mv;
    }
    @Post
    @URL(value = "/upload")
    public ModelView uploadFile(@Param(name = "file") Part filePart) {
        ModelView mv = new ModelView();

        try {
            if (filePart == null) {
                mv.setUrl("/index.jsp");
                mv.addObject("error", "Aucun fichier n'a été sélectionné");
                return mv;
            }

            String fileName = getFileName(filePart);
            if (fileName.isEmpty()) {
                mv.setUrl("/index.jsp");
                mv.addObject("error", "Nom de fichier invalide");
                return mv;
            }

            // Définir le chemin absolu pour le dossier d'upload
            String uploadPath = System.getProperty("user.home") + File.separator + "uploads";
            File uploadDir = new File(uploadPath);

            // Créer le répertoire s'il n'existe pas
            if (!uploadDir.exists()) {
                if (!uploadDir.mkdirs()) {
                    throw new IOException("Impossible de créer le dossier d'upload");
                }
            }

            // Construire le chemin complet du fichier
            String filePath = uploadPath + File.separator + fileName;

            // Sauvegarder le fichier
            filePart.write(filePath);

            mv.setUrl("/test.jsp");
            mv.addObject("message", "Le fichier " + fileName + " a été téléchargé avec succès dans " + filePath);

        } catch (IOException e) {
            mv.setUrl("/index.jsp");
            mv.addObject("error", "Erreur lors du téléchargement: " + e.getMessage());
        }

        return mv;
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp == null) {
            return "";
        }

        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                // Nettoyer le nom du fichier
                String fileName = token.substring(token.indexOf("=") + 2, token.length() - 1);
                // Éviter les chemins malveillants
                return new File(fileName).getName();
            }
        }
        return "";
    }


    @URL(value = "/logout")
    public ModelView logout(MySession session) {
        ModelView mv = new ModelView();
        session.delete("user");
        mv.setUrl("/index.jsp");
        mv.addObject("message", "Vous avez été déconnecté avec succès.");
        return mv;
    }




  @Get
  @RestApi
  @URL(value = "/hola")
    public String hola() {
        return "Ohatra fotsiny";
    }



    @Post
    @URL(value = "/hole")
    public ModelView hole() {
        String url = "/test.jsp";
        String variableName = "Mika&Davis";
        Object value = "tsekijoby";
        ModelView modelView = new ModelView(url);
        modelView.addObject(variableName, value);
        return modelView;
    }


    @Get
    @RestApi
    @URL(value = "/liste")
    public String liste(@Param(name = "emp") Employe emp) {
        return "Nom de l'employe: " + emp.getNom() + " et Age de l'employe: " + emp.getAge();
    }

    // @Get(value = "/form")
    // public String form(@Param(name = "username") String username, @Param(name =
    // "password") String password) {
    // return "Votre pseudo: " + username + ", votre mot de passe: " + password;
    // }

}
