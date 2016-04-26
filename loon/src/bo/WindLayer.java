package bo;

import java.util.Arrays;
import java.util.Random;

import structures.Pair;

public class WindLayer {

	private static final int DEFAULT_SPEED = 15;
	private static final int NOISE = 4;
	

	private Pair<Integer, Integer>[][] grid;
	private int Id;
	
	public WindLayer(int worldSize, int id){
		Random rand = new Random();
		Id = id;
		grid = new Pair[worldSize][worldSize];
		//TMP DATA
		
	
		for(int i = 0; i<worldSize; i++){
			for(int j = 0; j<worldSize; j++){
				
				int randomDeviation = 0 + (int)(Math.random() * NOISE);
				int x,y;
				
				if(rand.nextBoolean()){
					 x = DEFAULT_SPEED + randomDeviation;
				}
				else{
					 x = DEFAULT_SPEED -randomDeviation;                         
				}
	
					randomDeviation = 0 + (int)(Math.random() * NOISE);
				
				if(rand.nextBoolean()){
					 y = DEFAULT_SPEED + randomDeviation;
				}
				else{
					 y = DEFAULT_SPEED -randomDeviation;                         
				}
					
				grid[i][j] = new Pair<Integer, Integer>(x,y);
			}
		}

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
	   
	
}
