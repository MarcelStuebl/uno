package htl.steyr.uno;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
//        String logPath = System.getProperty("user.home") + "/uno-debug.log";
//        try {
//            java.io.PrintStream logStream = new java.io.PrintStream(new java.io.FileOutputStream(logPath, true));
//            System.setOut(logStream);
//            System.setErr(logStream);
//            System.out.println("=== UNO started: " + java.time.LocalDateTime.now() + " ===");
//        } catch (Exception e) {
//            // ignorieren falls Log nicht erstellt werden kann
//        }

        Application.launch(HelloApplication.class, args);
    }
}
