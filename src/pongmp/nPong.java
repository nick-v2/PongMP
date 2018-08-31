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

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.imageio.*;
import java.io.*;
import java.net.*;

/**
 * Main class for initializing the GUI and other elements for multiplayer pong
 * @author Nick Vocaire
 * Date Started: 11/24/2017
 */
public class nPong extends Thread{
    public static final int MAX_PLAYERS = 20;
    public static final int MIN_PLAYERS = 2;
    public static final int DEFAULT_PORT = 22333;
    public static final int DEFAULT_MODE = 0; //Default server mode (Clients are lined up for ball)
    //Mode -1: Singleplayer, Mode 0: Client Hopper (Experimental), Mode 1: 1v1v1 (Random Client)

    public static int windowWidth = 800; //Includes border which is 30 pixels on top
    public static int windowHeight = 600;

    public static final int T_FPS = 60; //Works best if T_UPDATES is a multiple of T_FPS
    public static final int T_UPDATES = 60;
    public static final int deltaYConverter = 40; //Converts from pixels moved to 1 y speed

    public static int paddleWidth = 20;
    public static int paddleHeight = 100;

    public static final int MAX_BALL_SPEED = 15;
    public static final int BALL_X_START = 5;
    public static final int BALL_Y_START = windowHeight/2;
    public static final int BALL_XSPEED_START = 5;
    public static final int BALL_YSPEED_START = 3;
    public static final int DEFAULT_BALL_DIAMETER = 20;
    public static final int PADDLE_X_START = windowWidth - 70;
    public static final int PADDLE_Y_START = (windowHeight/2) - (paddleHeight/2);

    public static int paddleX = PADDLE_X_START; //Top left corner
    public static int paddleY = PADDLE_Y_START; //Top left corner

    public static int maxBalls = 10000000;
    public static Ball balls[] = new Ball[maxBalls]; //Makes an array of balls

    public static volatile boolean running, connected;
    public static boolean paused;
    public static int secoundFPS, updateCount, paddleYUpdateCount, prevY, deltaY,
    score, finalScore, currentBalls, numPlayers, clientNum, mode;
    public static String lanIp, ip, error, modeString;
    public static Robot r;
    public static Cursor blankCursor;
    public static Thread gameT;
    public static BufferedImage mIcon, sIcon, cIcon;
    public static JFrame game, menu ,serverMenu;
    public static JPanel pControls, pMenu, pServer;
    public static JTextField getIp, players;
    public static JButton start, setPlayers, stop, addBall;
    public static JComboBox setMode;
    public static Thread getServer;
    public static Server server;
    public static Socket client;
    public static PrintWriter out;
    public static BufferedReader in;
    
    /**
     * Main method for starting the program
     * @param a arguments that can be used when the program is run from the console
     * @throws IOException
     * @throws AWTException 
     */
    public static void main(String a[]) throws IOException, AWTException{ 
        InetAddress loc = InetAddress.getLocalHost();
        lanIp = loc.getHostAddress();

        r = new Robot(); //Used for keeping mouse in game

        game = new JFrame("|Pong|");
        game.addMouseMotionListener(new mouseM());
        game.addMouseListener(new mouseA());
        blankCursor = game.getToolkit().createCustomCursor(
            new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB ), new Point(), "Blank");

        menu = new JFrame("Menu");

        serverMenu = new JFrame("Server");

        JPanel pGame = new gPanel();

        pMenu = new mPanel();
        pMenu.setLayout(null);
        JButton controls = new JButton("Controls");
        JButton sPlayer = new JButton("Single Player");
        JButton serButton = new JButton("Open Server");
        JButton connect = new JButton("Connect");
        getIp = new JTextField("0.0.0.0");
        serButton.addActionListener(new handler());
        connect.addActionListener(new handler());
        controls.addActionListener(new handler());
        sPlayer.addActionListener(new handler());
        pMenu.add(serButton);
        pMenu.add(connect);
        pMenu.add(getIp);
        pMenu.add(controls);
        pMenu.add(sPlayer);
        serButton.setBounds(240,225,125,20);
        controls.setBounds(240,250,125,20);
        sPlayer.setBounds(240,275,125,20);
        connect.setBounds(40,250,100,20);
        getIp.setBounds(45,225,90,20);

        pControls = new cPanel();
        pControls.setLayout(null);
        JButton cBack = new JButton("Back");
        cBack.addActionListener(new handler());
        pControls.add(cBack);
        cBack.setBounds(320,345,70,20);

        pServer = new sPanel();
        String modes[] = {"Client Hopper", "1v1v1..."};
        modeString = modes[0];
        pServer.setLayout(null);
        stop = new JButton("Stop");
        start = new JButton("Start");
        players = new JTextField("# of players");
        setPlayers = new JButton("Set Players");
        addBall = new JButton("Add Ball");
        setMode = new JComboBox(modes);
        setMode.setActionCommand("Set Mode");
        stop.addActionListener(new handler());
        start.addActionListener(new handler());
        setPlayers.addActionListener(new handler());
        addBall.addActionListener(new handler());
        players.addActionListener(new handler());
        setMode.addActionListener(new handler());
        pServer.add(start);
        pServer.add(players);
        pServer.add(setPlayers);
        pServer.add(setMode);
        start.setBounds(155,140,90,20);
        stop.setBounds(155,140,90,20);
        players.setBounds(165,175,70,20);
        setPlayers.setBounds(150,200,100,20);
        setMode.setBounds(125,230,150,20);
        addBall.setBounds(155,175,90,20);
        
        game.setResizable(false);
        menu.setResizable(false);
        serverMenu.setResizable(false);

        game.setSize(windowWidth,windowHeight + 30);
        menu.setSize(400,400);
        serverMenu.setSize(400,400);

        game.getContentPane().add(pGame);
        menu.getContentPane().add(pMenu);
        serverMenu.getContentPane().add(pServer);

        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mIcon = ImageIO.read(new File("img/Pong.png"));
        sIcon = ImageIO.read(new File("img/Server.png"));
        cIcon = ImageIO.read(new File("img/Controls.png"));

        menu.setBackground(Color.WHITE);
        game.setBackground(Color.WHITE);
        serverMenu.setBackground(Color.WHITE);

        menu.setVisible(true);
    }
    
    /**
     * Method for sending data to the server
     * @param d diameter of the ball
     * @param x x position of the ball
     * @param y y position of the ball
     * @param xs x speed of the ball
     * @param ys y speed of the ball
     * @param cx variable for direction of ball so it doesn't get stuck in walls
     */
    public static void sendData(int d, int x, int y, int xs, int ys, int cx) {
        out.println(d + " " + x + " " + y + " " + xs + " " + ys + " " + cx);
    }
    
    /**
     * Method that is invoked when a Game thread is initialized an ran
     */
    public void run() {
        long lastLoopTime = System.nanoTime();
        long fpsTimer = 0;
        int fps = 0;
        final long OPTIMAL_TIME = 1000000000 / T_FPS; //Optimal time between each loop
        running = true;
        paused = true; //Start paused

        while(running) {
            long now = System.nanoTime();
            long loopTime = now - lastLoopTime; //How much time it took the loop to cycle, includes sleeping
            lastLoopTime = now; //Last time loop took place
            double deltaLoop = loopTime / ((double)OPTIMAL_TIME); //How close the loop was the optimal time 
            //(Value of 1 is perfect, slow>1, fast<1)
            fpsTimer += loopTime;
            fps++;

            if (fpsTimer >= 1000000000) { //If its been 1 sec, shows the frames in that secound
                secoundFPS = fps;
                fpsTimer = 0;
                fps = 0;
            }

            update(deltaLoop); //Update, pass in the delta loop to change calculations based on lag if necessary

            now = System.nanoTime();
            long updateTime = now - lastLoopTime; //Similar to loop time but does not include the sleep,
            //only the update method call and the small code before

            long timeToSleep = (OPTIMAL_TIME - updateTime)/1000000;
            //Takes how long the update took, subtracts it from the optimal time and divides by 1000000 to get
            //how long to sleep in millisecounds (depending on lag, changes loop speed. More lag, smaller sleep).
            //Because of percition lost from dividing a long, fps is usually over what is set

            if(timeToSleep > 0) {//If update is taking super long, dont sleep
                try {
                    Thread.sleep(timeToSleep);
                } catch (InterruptedException i) {
                    System.err.println("Loop could not sleep");
                }
            }
        }
    }
    
    /**
     * Method that is ran T_UPDATES per second
     * @param d the delta of how close the loop was to optimal time between updates
     */
    public static void update(double d) {
        if(!paused) {
            updateCount++;
            paddleYUpdateCount++;
            if(updateCount >= (T_FPS / T_UPDATES)){
                //Code
                paddleY = mouseM.y - 70;
                if (paddleYUpdateCount == T_UPDATES/6) { //How many updates to wait before finding a delta
                    deltaY = paddleY - prevY;//For changing y speed depending on how fast the paddle is moving
                    prevY = paddleY;
                    paddleYUpdateCount = 0;
                }
                int i = -1; //Sets current ball to zero (is -1 because the inside for loop adds 1)
                for(int l = 0; l < currentBalls; l++) { //Loops through amount of balls in system
                    for(int j = i+1; j < maxBalls; j++) { //Finds next ball
                        //boolean skip = false;
                        //System.out.println(currentThread().toString());
                        //if(getServer.getState().equals("TIMED_WAITING")) { //check to see if getServer thread is messing with array
                        //    j--;
                        //    skip = true;
                        //}
                        //if(!skip) {
                        if(balls[j].changeX != -1) { //Checks to see if ball is on screen
                            i = j;
                            break; //breaks out of search loop
                        }
                        //}
                    }
                    if(mode != 0 && balls[i].ballX > paddleX + paddleWidth + 60) {
                        //If the ball passes your paddle (ENDGAME) (NOT IN MODE 0)
                        if(mode == 1) {//If playing 1v1v1 mode, must send data to server
                            sendData(balls[i].ballDiameter, 0, balls[i].ballY, balls[i].ballXSpeed, balls[i].ballYSpeed, balls[i].changeX);
                            out.println("close");
                            connected = false;
                            try {
                                client.close();
                            }catch (IOException io) {
                                System.err.println("Cant close connection");
                            }
                        }
                        running = false;
                        game.setVisible(false);

                        currentBalls = 0;
                        balls[i].ballX = BALL_X_START;
                        balls[i].ballY = BALL_Y_START;
                        balls[i].changeX = 0;
                        balls[i].ballXSpeed = BALL_XSPEED_START; //Resets game variables
                        balls[i].ballYSpeed = BALL_YSPEED_START;
                        mouseM.pressed = false;
                        mouseM.y = PADDLE_Y_START + 70;

                        game.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); //Shows cursor again
                        finalScore = score; //Sets your score
                        score = 0;
                        //Arrays.fill(balls, null);

                        menu.setVisible(true);
                    }

                    if(mode == 0 && balls[i].ballX <= paddleX + paddleWidth && balls[i].ballX >= paddleX + balls[i].ballXSpeed && balls[i].ballY + balls[i].ballDiameter >= paddleY && balls[i].ballY <= paddleY + paddleHeight && balls[i].changeX == 1) {
                        //Paddle from right, only in mode 0 <----------------
                        balls[i].ballYSpeed += deltaY/deltaYConverter;
                        if(balls[i].ballYSpeed > 0) //if ball is moving down
                            balls[i].ballXSpeed += deltaY/deltaYConverter;
                        else
                            balls[i].ballXSpeed -= deltaY/deltaYConverter;
                        if(balls[i].ballXSpeed >= -MAX_BALL_SPEED)
                            balls[i].ballXSpeed--;
                        if(balls[i].ballYSpeed >= 15) {//Stops ball from going too vertical
                            balls[i].ballYSpeed -= 5;
                            if(balls[i].ballXSpeed > 0)
                                balls[i].ballXSpeed += 5;
                            else
                                balls[i].ballXSpeed -= 5;
                        }
                        if(balls[i].ballYSpeed <= -15) {//Same as above
                            balls[i].ballYSpeed += 5;
                            if(balls[i].ballXSpeed > 0)
                                balls[i].ballXSpeed += 5;
                            else
                                balls[i].ballXSpeed -= 5;
                        }
                        if(balls[i].ballXSpeed >= 10 && balls[i].ballYSpeed <=2) { //When ball is moving too horizontal
                            balls[i].ballXSpeed -= 3;
                            if(balls[i].ballYSpeed > 0)
                                balls[i].ballYSpeed += 3;
                            else
                                balls[i].ballYSpeed -= 3;
                        }
                        balls[i].ballXSpeed = -balls[i].ballXSpeed;
                        balls[i].changeX = 0;
                        score++;
                    }

                    if(balls[i].ballX + balls[i].ballDiameter >= paddleX && balls[i].ballX + balls[i].ballDiameter <= paddleX + balls[i].ballXSpeed && balls[i].ballY + balls[i].ballDiameter >= paddleY && balls[i].ballY <= paddleY + paddleHeight && balls[i].changeX == 0) {
                        //Paddle from left <------------------
                        balls[i].ballYSpeed += deltaY/deltaYConverter; 
                        if(balls[i].ballYSpeed > 0) //if ball is moving up
                            balls[i].ballXSpeed -= deltaY/deltaYConverter;
                        else
                            balls[i].ballXSpeed += deltaY/deltaYConverter;
                        if(balls[i].ballXSpeed <= MAX_BALL_SPEED)
                            balls[i].ballXSpeed++;
                        if(balls[i].ballYSpeed >= 15) {//Stops ball from going too vertical
                            balls[i].ballYSpeed -= 5;
                            if(balls[i].ballXSpeed > 0)
                                balls[i].ballXSpeed += 5;
                            else
                                balls[i].ballXSpeed -= 5;
                        }
                        if(balls[i].ballYSpeed <= -15) {//Same as above
                            balls[i].ballYSpeed += 5;
                            if(balls[i].ballXSpeed > 0)
                                balls[i].ballXSpeed += 5;
                            else
                                balls[i].ballXSpeed -= 5;
                        }
                        if(balls[i].ballXSpeed >= 10 && balls[i].ballYSpeed <=2) { //When ball is moving too horizontal
                            balls[i].ballXSpeed -= 3;
                            if(balls[i].ballYSpeed > 0)
                                balls[i].ballYSpeed += 3;
                            else
                                balls[i].ballYSpeed -= 3;
                        }
                        balls[i].ballXSpeed = -balls[i].ballXSpeed;
                        balls[i].changeX = 1; //Used to stop ball from getting stuck in paddle, and for knowing
                        //its direction
                        score++;
                    }

                    if(balls[i].ballY <= 0) {//Top
                        balls[i].ballYSpeed = -balls[i].ballYSpeed;
                    }

                    if(balls[i].ballY  + balls[i].ballDiameter >= windowHeight) {//Bottom
                        balls[i].ballYSpeed = -balls[i].ballYSpeed;
                    }

                    if(mode == -1 && balls[i].ballX <= 0 && balls[i].changeX == 1) {//Only in single player left wall
                        balls[i].ballXSpeed = -balls[i].ballXSpeed;
                        balls[i].changeX = 0;
                    }

                    if(mode != -1 && balls[i].ballX < 0-balls[i].ballDiameter) {
                        //Only Multiplayer(ANYMODE), if ball hits left side of screen
                        balls[i].changeX = 1; //incase ChangeX is wrong, fixes it
                        if(mode == 0)
                            sendData(balls[i].ballDiameter, windowWidth, balls[i].ballY, balls[i].ballXSpeed, balls[i].ballYSpeed, balls[i].changeX);
                        else if(mode == 1) {
                            balls[i].ballXSpeed = -balls[i].ballXSpeed;
                            balls[i].changeX = 0;
                            sendData(balls[i].ballDiameter, 0 - balls[i].ballDiameter, balls[i].ballY, balls[i].ballXSpeed, balls[i].ballYSpeed, balls[i].changeX);
                        }
                        currentBalls--;
                        balls[i].changeX = -1; //An indicator that the ball is off the client
                    }

                    if(mode == 0 && balls[i].ballX > windowWidth) {
                        //Only Multiplayer(MODE 0), if ball hits right side of screen
                        balls[i].changeX = 0; //incase ChangeX is wrong, fixes it
                        sendData(balls[i].ballDiameter, 0-balls[i].ballDiameter, balls[i].ballY, balls[i].ballXSpeed, balls[i].ballYSpeed, balls[i].changeX);
                        currentBalls--;
                        balls[i].changeX = -1; //An indicator that the ball is off the client
                    }

                    if(Math.abs(balls[i].ballXSpeed) <= 2) //If for some reason it stops moving side-to-side, fix it.
                        balls[i].ballXSpeed += 3;
                    balls[i].ballX += balls[i].ballXSpeed * d;
                    balls[i].ballY += balls[i].ballYSpeed * d;
                }
                updateCount = 0;
            }

            game.setTitle("FPS: " + secoundFPS + "    |Pong|    " + "DeltaY: " + deltaY/deltaYConverter);
            game.repaint();
        }
    }
    
    /**
     * Class for handling the actions on button clicks
     */
    private static class handler implements ActionListener {
        
        /**
         * Method that is invoked every time a button is hit
         * @param e ActionEvent for the button clicked
         */
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            //System.out.println(cmd);
            switch(cmd) {
                case "Open Server":
                numPlayers = MIN_PLAYERS;
                serverMenu.setVisible(true);
                break;

                case "Set Players":
                try{
                    numPlayers = Integer.parseInt(players.getText());
                    error = null;
                    serverMenu.repaint();
                } catch (NumberFormatException ex) {
                    error = "MUST BE A NUMBER";
                    serverMenu.repaint();
                    break;
                }
                if(numPlayers > MAX_PLAYERS) {
                    error = "OVER MAX PLAYERS: " + MAX_PLAYERS;
                    numPlayers = 0;
                }
                if(numPlayers < MIN_PLAYERS) {
                    error = "NEED MIN PLAYERS: " + MIN_PLAYERS;
                    numPlayers = 0;
                }
                break;

                case "Set Mode":
                JComboBox cb = (JComboBox)e.getSource();
                modeString = (String)cb.getSelectedItem();
                if(modeString.equals("Client Hopper"))
                    mode = 0;
                if(modeString.equals("1v1v1..."))
                    mode = 1;
                serverMenu.repaint();
                break;

                case "Start":
                server = new Server(DEFAULT_PORT, MAX_PLAYERS, numPlayers, mode);
                pServer.remove(start);
                pServer.remove(players);
                pServer.remove(setPlayers);
                pServer.remove(setMode);
                pServer.add(stop);
                pServer.add(addBall);
                serverMenu.revalidate();
                serverMenu.repaint();
                break;

                case "Stop":
                try {
                    server.stopServer();
                } catch (IOException io) {
                    System.err.println("Server could not stop");
                }
                pServer.add(start);
                pServer.add(players);
                pServer.add(setPlayers);
                pServer.add(setMode);
                pServer.remove(addBall);
                pServer.remove(stop);
                serverMenu.revalidate();
                serverMenu.repaint();
                break;

                case "Add Ball":
                int ballD = 20;
                try{
                    server.addBall(ballD);
                } catch (IOException io) {
                    System.err.println("Cant add ball");
                }
                break;

                case "Connect":
                ip = getIp.getText();
                try {
                    client = new Socket(ip, DEFAULT_PORT);
                    error = null;
                    connected = true;
                    menu.repaint();
                } catch (UnknownHostException u) {
                    error = "Server not found";
                    menu.repaint();
                } catch (IOException io) {
                    error = "Server not found";
                    menu.repaint();
                }
                if(client != null) {
                    try {
                        out = new PrintWriter(client.getOutputStream(), true);
                        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                        clientNum = Integer.parseInt(in.readLine());
                        mode = Integer.parseInt(in.readLine());
                    } catch (IOException io) {
                        //Dont need to do anything
                    }
                    if(clientNum == 1) {
                        currentBalls = 1;
                        balls[0] = new Ball();
                    }
                    game.setVisible(true);
                    menu.setVisible(false);
                    gameT = new nPong();
                    gameT.start();

                    getServer = new Thread() { //For constantly getting newly fed info
                        public void run() {
                            String stuff[] = null;
                            int j = 0,d = 0,x = 0,y = 0,xs = 0,ys = 0,cx = 0;
                            while(connected) {
                                try {
                                    stuff = in.readLine().split("\\s+");

                                    d = Integer.parseInt(stuff[0]);
                                    x = Integer.parseInt(stuff[1]);
                                    y = Integer.parseInt(stuff[2]);
                                    xs = Integer.parseInt(stuff[3]);
                                    ys = Integer.parseInt(stuff[4]);
                                    cx =Integer.parseInt(stuff[5]);
                                    currentBalls++;
                                    for(j = 0; j < maxBalls; j++) { //Finds an available spot
                                        if(balls[j].changeX == -1) {
                                            balls[j] = new Ball(d,x,y,xs,ys,cx);
                                            break; //breaks out of search loop
                                        }
                                    }
                                } catch (IOException io) { 
                                } 
                                catch (NullPointerException n) { //If no spots
                                    balls[j] = new Ball(d,x,y,xs,ys,cx);
                                } 
                                try{
                                    getServer.sleep(2);
                                } catch (InterruptedException i) {
                                    //HUh
                                }
                            }
                        }
                    };
                    getServer.setName("getThread");
                    getServer.start();
                    if(mode == 0) {
                        paddleX = (windowWidth/2) - (paddleWidth/2);
                    }
                }
                break;

                case "Single Player":
                mode = -1;
                currentBalls = 1;
                balls[0] = new Ball();
                gameT = new nPong();
                gameT.start();
                game.setVisible(true);
                menu.setVisible(false);
                break;

                case "Controls":
                menu.getContentPane().remove(pMenu);
                menu.getContentPane().add(pControls);
                menu.revalidate();
                menu.repaint();
                break;

                case "Back":
                menu.getContentPane().remove(pControls);
                menu.getContentPane().add(pMenu);
                menu.revalidate();
                menu.repaint();
                break;

            }
        }
    }
    
    /**
     * Class for drawing to the game panel of the program
     */
    private static class gPanel extends JPanel {
        
        /**
         * Method that is invoked every time a repaint is called
         * @param g Graphics object that is passed in from java
         */
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.fillRect(paddleX,paddleY,paddleWidth,paddleHeight);
            g2.setFont(new Font("SERIF", Font.PLAIN, 20));
            g2.drawString("Score: " + score ,5 ,20);
            int i = -1; //Sets current ball to zero (is -1 because the inside for loop adds 1)
            for(int l = 0; l < currentBalls; l++) { //Loops through amount of balls in system
                for(int j = i+1; j < maxBalls; j++) { //Finds next ball
                    if(balls[j].changeX != -1) {
                        i = j;
                        break; //breaks out of search loop
                    }
                }
                g2.fillOval(balls[i].ballX,balls[i].ballY,balls[i].ballDiameter,balls[i].ballDiameter);
            }
        }
    }
    
    /**
     * Class for drawing to the controls panel of the program
     */
    private static class cPanel extends JPanel {
        
        /**
         * Method that is invoked every time a repaint is called
         * @param g Graphics object that is passed in from java
         */
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(cIcon,25,-10, null);
        }
    }
    
    /**
     * Class for drawing to the controls panel of the program
     */
    private static class mPanel extends JPanel {
        
        /**
         * Method that is invoked every time a repaint is called
         * @param g Graphics object that is passed in from java
         */
        public void paintComponent(Graphics g) { 
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(mIcon,65,-10, null);
            g2.setFont(new Font("SERIF", Font.PLAIN, 20));
            g2.drawString("Your Single Player Score: " + finalScore ,130 ,365);
            if (error != null) {
                g2.setFont(new Font("SERIF", Font.PLAIN, 15));
                g2.drawString(error, 35, 290);
            }
        }
    }
    
    /**
     * Class for drawing to the controls panel of the program
     */
    private static class sPanel extends JPanel {
        
        /**
         * Method that is invoked every time a repaint is called
         * @param g Graphics object that is passed in from java
         */
        public void paintComponent(Graphics g) { 
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(sIcon,65,-10, null);
            g2.setFont(new Font("SERIF", Font.PLAIN, 20));
            g2.drawString("Number of players: " + numPlayers, 105, 95);
            g2.drawString("Mode: " + modeString, 105, 125);
            g2.drawString("Lan IP: " + lanIp ,110 ,365);
            if (error != null) {
                g2.setFont(new Font("SERIF", Font.PLAIN, 10));
                g2.drawString(error, 240, 170);
            }
        }
    }
    
    /**
     * Class for dealing with mouse motion
     */
    private static class mouseM implements MouseMotionListener {
        public static int y = nPong.PADDLE_Y_START + 70;
        public static boolean pressed = false;
        
        /**
         * Method invoked when the mouse is moved
         * @param e MouseEvent object passed in from java
         */
        public void mouseMoved(MouseEvent e) {
            if(pressed)
                y = e.getY();
        } 

        public void mouseDragged(MouseEvent e) {}
    }
    
    /**
     * Class for dealing with mouse actions
     */
    private static class mouseA implements MouseListener {
        
        /**
         * Method invoked when the mouse is clicked
         * @param e MouseEvent object passed in from java
         */
        public void mouseClicked(MouseEvent e) {} 
        
        /**
         * Method invoked when the mouse is pressed
         * @param e MouseEvent object passed in from java
         */
        public void mousePressed(MouseEvent e) {
            if(mouseM.pressed) {
                mouseM.pressed = false;
                paused = true;
                game.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                game.setTitle("PAUSED");
            } else {
                mouseM.pressed = true;
                paused = false;
                mouseM.y = e.getY();
                game.setCursor(blankCursor);
            }
        }
        
        /**
         * Method invoked when the mouse is released
         * @param e MouseEvent object passed in from java
         */
        public void mouseReleased(MouseEvent e) {}
        /**
         * Method invoked when the mouse enters the program
         * @param e MouseEvent object passed in from java
         */
        public void mouseEntered(MouseEvent e) {}
        /**
         * Method invoked when the mouse exits the program
         * @param e MouseEvent object passed in from java
         */
        public void mouseExited(MouseEvent e) {
            if(mouseM.pressed) {
                Point p = game.getLocation(); //Top left corner of JFrame
                r.mouseMove((int)p.getX() + windowWidth/2, (int)p.getY() + mouseM.y);
            }
        }
    }
}