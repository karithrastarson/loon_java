package bo;

import java.util.Arrays;
import java.util.Random;

import structures.Pair;

public class WindLayer {

	private static final int DEFAULT_SPEED = 45;
	private static final int NOISE = 5;
	

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
