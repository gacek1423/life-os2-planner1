package com.budget.ui;

public class Launcher {
    public static void main(String[] args) {
        // To oszukuje maszynę wirtualną Javy.
        // Najpierw ładuje tę klasę (która nie używa JavaFX),
        // dzięki czemu zdąży załadować biblioteki z Classpath,
        // a dopiero potem odpala właściwą aplikację.
        App.main(args);
    }
}