package controllers;

import annotations.AnnotationController;
import annotations.Get;
import annotations.GetFonction;

@AnnotationController
public class MyController {
    @GetFonction(value = "/bonjour")
    public void bonjour() {
        System.out.println("bonjour mon ami");
    }

    public static void main (String[] args) {
        System.out.println("voici mon controlleur");
    }
}
