/*
 * Copyright (C) 2018 Nick Vocaire
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pongmp;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Class for initializing a pong server
 * @author Nick Vocaire
 */
public class Server extends Thread{
    public static int maxConnections, port, players; //Variables for when server is made
    public static int mode = 0; //Default server mode (Clients are lined up for ball)
    public static volatile boolean sRunning = true; //Boolean to stop threads of server if stopServer method is called
    public static Socket clients[]; //Array of clients connected
    public static Thread threads[]; //Array of threads for each client
    public static volatile ServerSocket serverSocket; //Server Socket

    public int client; //Local variable for every Thread created storing position in client array
    public boolean running = true;
    
    /**
     * Basic constructor for starting a server
     * @param p the port of the server
     * @param max the max amount of people that can ever join the server
     * @param pl the amount of player that can join the server
     * @param m the gamemode of the server
     */
    public Server(int p, int max, int pl, int m) { //Constructor for starting server
        port = p;
        maxConnections = max; //Max Players (set by game)
        players = pl; //Players (set by game when a user makes a server)
        mode = m;

        if(players > maxConnections) //Just in case
            players = maxConnections;

        threads = new Server[maxConnections];
        clients = new Socket[maxConnections];

        Thread findClients = new Thread() { //Make a thread to find connections with this code
                public void run() {
                    try{
                        serverSocket = new ServerSocket(port);
                    } catch (IOException io) {
                        System.err.println("That port is already used");
                    }
                    for(int i=0; i<players; i++){ //Loop through all the clients
                        if(sRunning) {
                            try{
                                clients[i] = serverSocket.accept(); //Wait for a client to connect and set a Socket in the array to

                                PrintWriter out = new PrintWriter(clients[i].getOutputStream(), true);
                                //Sets a writer to print out to the client that just connected

                                BufferedReader in = new BufferedReader(new InputStreamReader(clients[i].getInputStream()));
                                //Sets a reader to get input from the client that just connected

                                out.println(i+1); //Send back the clients number
                                out.println(mode); //Send back the game being played

                                threads[i] = new Server(i); //Make a Thread using the postion in the array of the 
                                //client and the name of the client
                                threads[i].start(); //Start that Thread
                            } catch (IOException io) {
                                System.err.println("Server was stopped");
                            }
                        }
                    }
                }
            };
        findClients.start();
    }
    
    /**
     * Constructor used for each client that connects to the server
     * @param c the client number
     */
    public Server(int c) { //Constructer for each Thread
        if(port == 0)
            System.err.println("Please create a server with the server creation constructor");
        else
            client = c;
    }
    
    /**
     * Method for stopping the server
     * @throws IOException 
     */
    public void stopServer() throws IOException{
        sRunning = false;
        serverSocket.close();
    }
    
    /**
     * Method for adding balls to the game
     * @param d the diameter of the ball
     * @throws IOException 
     */
    public void addBall (int d) throws IOException{
        if(clients[0] != null) {
            PrintWriter out = new PrintWriter(clients[0].getOutputStream(), true);
            out.println(d + " " + (0-d) + " " + nPong.BALL_Y_START + " " + nPong.BALL_XSPEED_START + " " + nPong.BALL_YSPEED_START + " " + 0);
        }
    }
    
    /**
     * Method that is called when a server thread is initialized and ran
     */
    public void run() { // Code to run for each client connected to server (CHANGE THIS)
        PrintWriter out = null, outNext = null; //out.println to send to server
        BufferedReader in = null; //in.readLine to get info (WAITS FOR INFORMATION)
        String input = null; //Input from client

        Random numGen = new Random();
        while(running && sRunning){
            try {
                out = new PrintWriter(clients[client].getOutputStream(), true);
                //Sets a writer to print out to the client on this thread

                in = new BufferedReader(new InputStreamReader(clients[client].getInputStream()));
                //Sets a reader to get input from the client on this thread

                input = in.readLine(); //Waits for and gets the input

                if(input.equals("close")) {//If client disconnects by losing
                    System.out.println("Client " + (client + 1) + " LOST");
                    try{
                        clients[client] = serverSocket.accept(); //Thread looks for another connection
                        System.out.println("Here");
                        running = false; //Stops current thread
                        threads[client] = new Server(client);
                        threads[client].start();
                    } catch (IOException io2) {
                        System.out.println("Server was stopped");
                    }
                }

            } catch (IOException io) { //If the client disconnects by closing game
                if(running) {
                    System.out.println("Client " + (client + 1) + " disconnected, looking for another");
                    try{
                        clients[client] = serverSocket.accept(); //Thread looks for another connection
                        running = false; //Stops current thread
                        threads[client] = new Server(client);
                        threads[client].start();
                    } catch (IOException io2) {
                        System.out.println("Server was stopped");
                    }
                }
            }

            if(running) {
                if(mode == 0) { //First game mode (Clients are lined up for ball)
                    String stuff[] = input.split("\\s+");
                    if(stuff[5].equals("0")){ //Search Right
                        int currentClient = client;
                        for(int i = 0; i<players; i++) { //Look for next client to send too
                            if(currentClient + 1 == players) { //Reaches current max players that will join
                                currentClient = -1; //Sets back to begginging for next if statement
                            }
                            if(clients[currentClient + 1] != null) {
                                try{
                                    outNext = new PrintWriter(clients[currentClient + 1].getOutputStream(), true);
                                } catch (IOException io) {
                                    System.err.println("Next client does not exist");
                                }
                                outNext.println(input);
                                break;
                            }
                            currentClient++;
                        }
                    }

                    if(stuff[5].equals("1")) { //Search Left
                        int currentClient = client;
                        for(int i = 0; i<players; i++) { //Look for next client to send too
                            try {
                                if(clients[currentClient - 1] != null) {
                                    try{
                                        outNext = new PrintWriter(clients[currentClient - 1].getOutputStream(), true);
                                    } catch (IOException io) {
                                        System.err.println("Next client does not exist");
                                    }
                                    outNext.println(input);
                                    break;
                                }
                                currentClient--;
                            } catch (ArrayIndexOutOfBoundsException e){
                                currentClient = players - 1; //Reaches the first spot in array [0]

                                if(clients[currentClient] != null) {
                                    try{
                                        outNext = new PrintWriter(clients[currentClient].getOutputStream(), true);
                                    } catch (IOException io) {
                                        System.err.println("Next client does not exist");
                                    }
                                    outNext.println(input);
                                    break;
                                }
                            }
                        }
                    }
                }
                if(mode == 1) {
                    int connected = 0, random = 0;
                    for(int i = 0; i<players; i++) {
                        if(clients[i] != null) {
                            connected++;
                        }
                    }
                    if(connected > 1) {
                        for(int i = 0; i<players; i++) { //Look for next client to send too {
                            random = numGen.nextInt(players);
                            System.out.println(random);
                            if(random != client && clients[random] != null) {
                                try{
                                    outNext = new PrintWriter(clients[random].getOutputStream(), true);
                                } catch (IOException io) {
                                    System.err.println("Next client does not exist");
                                }
                                outNext.println(input);
                                break; //break out of for loop
                            }
                        }
                    } else { //If only one person is connected
                        try{
                            outNext = new PrintWriter(clients[random].getOutputStream(), true);
                        } catch (IOException io) {
                            System.err.println("Next client does not exist");
                        }
                        outNext.println(input);
                    }
                }
            }
        }
    }
}