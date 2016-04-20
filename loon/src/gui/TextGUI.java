package gui;

import java.io.FileNotFoundException;
import java.io.IOException;

import service.World;

public class TextGUI {

	public static void main(String[] args) throws FileNotFoundException, IOException {

		World world = new World();
		world.init();
		world.simulate();
		System.out.println(world.toString());

	}

}
