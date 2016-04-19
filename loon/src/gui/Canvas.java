package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import bo.Balloon;

public class Canvas extends JPanel{
	
	int DIAMETER = 5;
	int x = 0; 
	int y = 0;
	Color color;
	ArrayList<Balloon> balloons;
    public Canvas() {
    	balloons = new ArrayList<>();
    	color = Color.RED;
    	setBackground(Color.BLACK);
    	setBorder(BorderFactory.createLineBorder(Color.RED));
    	
    	
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
              if(color.equals(Color.RED)){
            	  color = Color.GREEN;
              }
              else{
            	  color = color.RED;
              }
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);   
        
        for(Balloon b : balloons){
            g.setColor(color);
            g.fillOval(b.getX(),b.getY(),DIAMETER,DIAMETER);
            g.setColor(color);
            g.fillOval(b.getX(),b.getY(),DIAMETER,DIAMETER);
        }
 

    }

    
    public void updateGraphics(ArrayList<Balloon> b) {
    	balloons = b;
 
        repaint();
    }

     
}