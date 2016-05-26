package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import bo.Balloon;
import bo.WindLayer;
import service.World;

public class Canvas extends JPanel implements Runnable{
	
	int delay = 1000;
	int DIAMETER = 5;
	int RANGE = 20;
	int x = 0; 
	int y = 0;
	Color color;
	World world;
	
	ArrayList<Balloon> balloons;
	boolean[][] coverage;
	
    public Canvas() {
		world = new World();
		try {
			world.init('s');
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	balloons = new ArrayList<>();
    	coverage = new boolean[world.WORLD_SIZE][world.WORLD_SIZE];
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
        
        super.removeAll();
        try{
//            for(int x = 0; x < world.WORLD_SIZE; x++){
//            	for(int y = 0; y < world.WORLD_SIZE; y++){
//            	if(coverage[x][y]){
//                    g.setColor(Color.green);
//                    g.fillOval(x-DIAMETER,y-DIAMETER,DIAMETER,DIAMETER);
//            	}
//            	else{
//                    g.setColor(Color.black);
//                    g.fillOval(x-DIAMETER,y-DIAMETER,DIAMETER,DIAMETER);
//            	}
//            }}
        for(Balloon b : balloons){
//            g.setColor(Color.blue);
//            g.fillOval((b.getX()-RANGE),(b.getY()-RANGE),(b.getX()+RANGE),(b.getY()+RANGE));
//            g.setColor(color);
//            g.fillOval((b.getX()-RANGE),(b.getY()-RANGE),(b.getX()+RANGE),(b.getY()+RANGE));
//            
            g.setColor(color);
            g.fillOval(b.getX()-DIAMETER,b.getY()-DIAMETER,DIAMETER,DIAMETER);
            g.setColor(Color.blue);
            g.drawOval(b.getX()+DIAMETER-RANGE, b.getY()-RANGE+DIAMETER, DIAMETER+RANGE, DIAMETER+RANGE);

        }

        }catch(Exception e){
        	System.out.println("Error with balloons in canvas. Size: " + balloons.size());
        }

    }

    
    public void updateGraphics(ArrayList<Balloon> b, boolean[][] cov) {
    	balloons = b;
		coverage = cov;
        repaint();
    }

    public void step(){
		try {
			world.step();
			
			updateGraphics(world.getBalloons(),world.getCoverage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
    }
    @Override
	public void run() {
		
    	while(true){
    	try {
			world.step();
			
			updateGraphics(world.getBalloons(), world.getCoverage());
			
			Thread.sleep(delay);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	}
	
		
		
	}

	public void setDelay(int del) {
		this.delay=del;
		
	}

     
}