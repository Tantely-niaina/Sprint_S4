package controllers;

import annotations.AnnotationController;
import annotations.Get;
import annotations.Param;
import annotations.RestApi;
import frameworks.ModelView;
import frameworks.MySession;
import util.Employe;

@AnnotationController
public class MyController {

    @Get(value = "/login")
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

    @Get(value = "/logout")
    public ModelView logout(MySession session) {
        ModelView mv = new ModelView();
        session.delete("user");
        mv.setUrl("/index.jsp");
        mv.addObject("message", "Vous avez été déconnecté avec succès.");
        return mv;
    }

    @Get(value = "/hola")
    public String hola() {
        return "Ohatra fotsiny";
    }

    @Get(value = "/hole")
    public ModelView hole() {
        String url = "/test.jsp";
        String variableName = "Mika&Davis";
        Object value = "tsekijoby";
        ModelView modelView = new ModelView(url);
        modelView.addObject(variableName, value);
        return modelView;
    }

    @Get(value = "/liste")
    @RestApi
    public String liste(@Param(name = "emp") Employe emp) {
        return "Nom de l'employe: " + emp.getNom() + " et Age de l'employe: " + emp.getAge();
    }

    // @Get(value = "/form")
    // public String form(@Param(name = "username") String username, @Param(name =
    // "password") String password) {
    // return "Votre pseudo: " + username + ", votre mot de passe: " + password;
    // }

}
