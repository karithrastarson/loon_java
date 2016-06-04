package gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bo.Balloon;

import javax.swing.JFrame;


import service.World;

public class MainWindow {

	private JFrame mainFrame;
//	private static World world;
	private static boolean run;
	private static Canvas canvas;
	private static int steps = 1;
	private static int delay = 1000;
	private static Thread canvasThread;
	public MainWindow(){

	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					createAndShowGUI();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	private static void createAndShowGUI() throws FileNotFoundException, IOException {
		System.out.println("Created GUI on EDT? "+
				SwingUtilities.isEventDispatchThread());
		JFrame f = new JFrame("Simulator");
//		world = new World();
//		world.init();
		run = false;

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new BorderLayout());

		//f.setLayout(new GridLayout(2, 4, 2, 2));
		
		canvas = new Canvas();
		canvas.setPreferredSize(new Dimension(1000, 1000));
		canvas.setDelay(delay);
		Thread canvasThread = new Thread(canvas);
		canvasThread.start();
//		canvasThread.suspend();
		
		
		JButton btnStep = new JButton("Step");
		JButton btnMultiStep = new JButton("Multi-Step");
		JButton btnAuto = new JButton("Auto");
		
		JSlider sliderNumSteps = new JSlider(1,200,1);
		JSlider sliderDelay = new JSlider(1,2000,1);
		//Create the label table
		Hashtable labelTable = new Hashtable();
		labelTable.put( new Integer( 100 ), new JLabel("Number of steps") );
		sliderNumSteps.setLabelTable( labelTable );
		sliderNumSteps.setPaintLabels(true);
		//Create the label table
		Hashtable labelTable2 = new Hashtable();
		labelTable2.put( new Integer( 1000 ), new JLabel("Speed") );
		sliderDelay.setLabelTable( labelTable2 );
		sliderDelay.setPaintLabels(true);
//	
		JPanel buttons = new JPanel();
		buttons.add(btnStep);
		buttons.add(btnMultiStep);
		buttons.add(btnAuto);
		buttons.add(sliderNumSteps);
		buttons.add(sliderDelay);

		f.add(canvas, BorderLayout.CENTER);
		f.add(buttons,BorderLayout.PAGE_START);
		f.pack();
		f.setResizable(false);

		//Add action listeners

		sliderNumSteps.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
            	run = false;
                steps = sliderNumSteps.getValue();
                
            
            }
        });
		
		sliderDelay.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
            	if(canvasThread.isAlive()){
            	canvasThread.suspend();}
                delay = sliderDelay.getValue();
                canvas.setDelay(2000/delay);
                canvasThread.resume();
                
            
            }
        });
		
		btnStep.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				canvasThread.suspend();
				try {
					updateCanvas();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				f.repaint();
			}
		});

		
		btnMultiStep.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				canvasThread.suspend();
				for(int i = 0; i<steps; i++){
					try {
						
						updateCanvas();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				f.repaint();
			}
		});
		btnAuto.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
	
						canvasThread.resume();
						canvasThread.run();
		
				f.repaint();
			}
		});

		f.setVisible(true);
	}

	private static void updateCanvas() throws IOException{
		
		canvas.step();
		
//		world.step2();
//		canvas.updateGraphics(world.getBalloons());
	}


}

