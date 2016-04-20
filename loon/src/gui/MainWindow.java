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

import javax.swing.*;
import javax.swing.SwingUtilities;

import bo.Balloon;

import javax.swing.JFrame;


import service.World;

public class MainWindow {

	private JFrame mainFrame;
	private static World world;
	private static Canvas canvas;
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
		world = new World();
		world.init();

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new BorderLayout());

		canvas = new Canvas();
		canvas.setPreferredSize(new Dimension(world.WORLD_SIZE, world.WORLD_SIZE));

		JButton btnStep = new JButton("Step");
		JButton btnStep10 = new JButton("Step 100");
		JButton btnAuto = new JButton("Auto");
		JButton btnStop = new JButton("Stop");
		JButton btnReset = new JButton("Reset");

		JPanel buttons = new JPanel();
		buttons.add(btnStep);
		buttons.add(btnStep10);
		buttons.add(btnAuto);
		buttons.add(btnStop);
		buttons.add(btnReset);

		f.add(canvas, BorderLayout.CENTER);
		f.add(buttons,BorderLayout.PAGE_START);
		f.pack();
		f.setResizable(false);
		//Add action listeners

		btnStep.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateCanvas();
				f.repaint();
			}
		});
		btnStep10.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for(int i = 0; i<100; i++){
					updateCanvas();
					
				}
				f.repaint();
			}
		});


		f.setVisible(true);
	}

	private static void updateCanvas(){
		world.step();
		canvas.updateGraphics(world.getBalloons());

	}


}
