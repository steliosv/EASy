package org.sv.easy.remote;

import java.io.BufferedReader;
import java.io.DataOutputStream;
/**
 * @file org.sv.easy.remote.EASYRemote.java
 * @brief Client app to remotely control EASY service
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EASYRemote {

    /**
     * @brief shut-down hook
     */
    private static Socket clientSocket;

    private static class shutDown extends Thread {

        public void run(String[] args) {
            try {
                clientSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(EASYRemote.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {

        String command;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        if (args.length == 1) {
            while (true) {
                clientSocket = new Socket(args[0], 22212);
                System.out.println("Type a command or type HELP: ");
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                Runtime r = Runtime.getRuntime();
                r.addShutdownHook(new shutDown());
                command = inFromUser.readLine();
                if (command.equals("HELP")) {
                    System.out.println("EOWARN removes the issued warning from the end devices");
                    System.out.println("TESTMAIL sends a test email");
                    System.out.println("TESTXBEE sends a test xbee packet");
                    System.out.println("TESTIOT tests the IOT service");
                    System.out.println("STOPR terminates ringserver");
                    System.out.println("STARTR starts ringserver");
                    System.out.println("GPIOEN enables GPIOs");
                    System.out.println("GPIODIS disables GPIOs");
                    System.out.println("DELF Deletes files and logs older than 30days");
                    System.out.println("EXIT terminates properly the application\n");
                }
                outToServer.writeBytes(command + '\n');
                outToServer.close();
                clientSocket.close();
                if (command.equals("EXIT")) {
                    System.exit(0);
                }
            }
        }
        if (args.length == 2) {
            command = args[1];
            clientSocket = new Socket(args[0], 22212);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes(command + '\n');
            outToServer.close();
            clientSocket.close();
            System.exit(0);
        }
    }
}
