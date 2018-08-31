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

/**
 * Class for creating ball objects
 * @author Nick Vocaire
 */
public class Ball {
    public int ballDiameter = nPong.DEFAULT_BALL_DIAMETER;
    public int ballXSpeed = nPong.BALL_XSPEED_START;
    public int ballYSpeed = nPong.BALL_YSPEED_START; //All ball vars probably going to have to be in an array
    public int ballX = nPong.BALL_X_START;
    public int ballY = nPong.BALL_Y_START;
    public int changeX = 0; //Used to stop ball from getting stuck in wall
    
    /**
     * Default constructor for a ball
     */
    public Ball() {}
    
    /**
     * Ball constructor that allows a diameter to be set
     * @param d diameter of ball in pixels
     */
    public Ball(int d) {
        ballDiameter = d;
    }
    
    /**
     * Ball constructor that allows a x and y coordinate to be set
     * @param x x position of ball
     * @param y y position of ball
     */
    public Ball(int x, int y) {
        ballX = x;
        ballY = y;
    }
    
    /**
     * Ball constructor that allows everything but ball diameter to be set
     * @param x x position of ball
     * @param y y position of ball
     * @param xs x speed of ball
     * @param ys y speed of ball
     * @param cx direction of ball so it doesn't get stuck in walls
     */
    public Ball(int x, int y, int xs, int ys, int cx) {
        ballX = x;
        ballY = y;
        ballXSpeed = xs;
        ballYSpeed = ys;
        changeX = cx;
    }
    
    /**
     * Ball constructor that allows a diameter to be set
     * @param d diameter of ball in pixels
     * @param x x position of ball
     * @param y y position of ball
     * @param xs x speed of ball
     * @param ys y speed of ball
     * @param cx direction of ball so it doesn't get stuck in walls
     */
    public Ball(int d, int x, int y, int xs, int ys, int cx) {
        ballDiameter = d;
        ballX = x;
        ballY = y;
        ballXSpeed = xs;
        ballYSpeed = ys;
        changeX = cx;
    }
}