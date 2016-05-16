package bo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import structures.Pair;

public class WindLayer {

	private static final int DEFAULT_SPEED = 45;
	private static final int NOISE = 5;
	

	private Pair<Integer, Integer>[][] grid;
	private int Id;
	
	public WindLayer(int worldSize ,int id){
		Random rand = new Random();
		Id = id;
		grid = new Pair[worldSize][worldSize];
		//TMP DATA
		

		for(int i = 0; i<worldSize; i++){
			for(int j = 0; j<worldSize; j++){
				
				int randomDeviation = 0 + (int)(Math.random() * NOISE);
				int x=DEFAULT_SPEED,y=DEFAULT_SPEED;
				if(Id==0){x = DEFAULT_SPEED; y = DEFAULT_SPEED;}
				if(Id==1){x = DEFAULT_SPEED; y = -DEFAULT_SPEED;}
				if(Id==2){x = -DEFAULT_SPEED; y = DEFAULT_SPEED;}
				if(Id==3){x = -DEFAULT_SPEED; y = -DEFAULT_SPEED;}
				
				
				if(rand.nextBoolean()){
					 x += randomDeviation;
				}
				else{
					 x -= randomDeviation;                         
				}
	
					randomDeviation = 0 + (int)(Math.random() * NOISE);
				
				if(rand.nextBoolean()){
					 y += randomDeviation;
				}
				else{
					 y -= randomDeviation;                         
				}
					
				grid[i][j] = new Pair<Integer, Integer>(x,y);
			}
		
		

		}
		
		

	}
	
	public WindLayer(int worldSize, String textFileX, String textFileY, int id){
		Id = id;
		grid = new Pair[worldSize][worldSize];
		
		readFromFile(textFileX, textFileY, worldSize);
	}
	public Pair<Integer,Integer> getWind(int x, int y){

		return grid[x][y];
	}

	 
	public int getId() {
		return Id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WindLayer other = (WindLayer) obj;
		if (!Arrays.deepEquals(grid, other.grid))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "WindLayer [Id=" + Id + "]";
	}
	
	private void readFromFile(String textfileX, String textfileY ,int world_size){
		try {
		Scanner inputX = new Scanner (new File(textfileX));
		Scanner inputY = new Scanner (new File(textfileY));


			for(int i = 0; i<world_size; i++){  
				
			Scanner colReaderX = new Scanner(inputX.nextLine());
		    Scanner colReaderY = new Scanner(inputY.nextLine());
		    
				for(int j = 0; j<world_size; j++){
					
		        grid[i][j] = new Pair<Integer, Integer>(colReaderX.nextInt(),colReaderY.nextInt());
		       
		    }
		   
		    
		    colReaderX.close();
		    colReaderY.close();
		}
		
		inputX.close();
		inputY.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	   
	
}
