package gui;

import java.io.FileNotFoundException;
import java.io.IOException;

import service.World;

public class TextGUI {

	public static void main(String[] args) throws FileNotFoundException, IOException {

		/*UNCOMMENT TO RUN SIMULATION WITH ALGORITHM 1 */
		World world = new World();
		world.init("1");
		world.simulate();


		System.out.println("Run 1 finished: " + new java.util.Date());

		/*	UNCOMMENT TO RUN SIMULATION WITH ALGORITHM 2 */
		World world2 = new World();
		world2.init("2");
		world2.simulate();


		System.out.println("Run 2 finished: " + new java.util.Date());

		/*UNCOMMENT TO RUN SIMULATION WITH ALGORITHM 3*/
		World world3 = new World();
		world3.init("3");
		world3.simulate();


		System.out.println("Run 3 finished: " + new java.util.Date());
		/*  UNCOMMENT RUN SIMULATION WITH ALGORITHM 3s*/
		World world3s = new World();
		world3s.init("3s");
		world3s.simulate();


		/* UNCOMMENT TO RUN SIMULATION WITH ALGORITHM 4 */
		World world4 = new World();
		world4.init("3");
		world4.simulate();


		/* UNCOMMENT RUN SIMULATION WITH ALGORITHM 4s*/
		World world4s = new World();
		world4s.init("4s");
		world4s.simulate();
	

		System.out.println("All runs finished: " + new java.util.Date());

	}

}
