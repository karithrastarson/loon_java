package gui;

import java.io.FileNotFoundException;
import java.io.IOException;

import service.World;

public class TextGUI {

	public static void main(String[] args) throws FileNotFoundException, IOException {

//		RUN SIMULATION WITH ALGORITHM 1
//		World world = new World();
//		world.init("1");
//		world.simulate();
//		System.out.println(world.toString());
//		
//		System.out.println("Run 1 finished: " + new java.util.Date());
//		
//		//RUN SIMULATION WITH ALGORITHM 2
//		World world2 = new World();
//		world2.init("2");
//		world2.simulate();
//		System.out.println(world2.toString());
		
		System.out.println("Run s finished: " + new java.util.Date());
////		
////		
		//RUN SIMULATION WITH ALGORITHM 3
//		World world3 = new World();
//		world3.init("3");
//		world3.simulate();
//		System.out.println(world3.toString());
//		
//		System.out.println("Run 3 finished: " + new java.util.Date());
//		
////		
//		//RUN SIMULATION WITH ALGORITHM 3s
//		World world3s = new World();
//		world3s.init("3s");
//		world3s.simulate();
//		System.out.println(world3s.toString());
//		
		//RUN SIMULATION WITH ALGORITHM 4
		World world4 = new World();
		world4.init("4");
		world4.simulate();
		System.out.println(world4.toString());
		
		//RUN SIMULATION WITH ALGORITHM 4s
		World world4s = new World();
		world4s.init("4s");
		world4s.simulate();
		System.out.println(world4s.toString());
//		
		System.out.println("All runs finished: " + new java.util.Date());

	}

}
