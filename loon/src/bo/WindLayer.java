package bo;

import java.util.Arrays;
import java.util.Random;

import structures.Pair;

public class WindLayer {


	private Pair<Integer, Integer>[][] grid;
	private int Id;
	
	public WindLayer(String filename, int worldSize, int id){
		Random rand = new Random();
		Id = id;
		grid = new Pair[worldSize][worldSize];
		//TMP DATA
		
		if(rand.nextBoolean()){
		
		for(int i = 0; i<worldSize; i++){
			for(int j = 0; j<worldSize; j++){
				grid[i][j] = new Pair(1,1);
			}
		}
		}
		else{
			for(int i = 0; i<worldSize; i++){
				for(int j = 0; j<worldSize; j++){
					grid[i][j] = new Pair(-1,-1);
				}
			}
		}

		//TODO read from file

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
