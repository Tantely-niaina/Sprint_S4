package controllers;

import annotations.AnnotationController;
import annotations.Get;

@AnnotationController
public class MyController {
    @Get(value = "/bonjour")
    public void bonjour(String value) {
        System.out.println("votre requete :" + value);
    }

    public static void main (String[] args) {
        System.out.println("voici mon controlleur");
    }
}
