package service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import bo.Balloon;
import bo.WindLayer;

public class World {

	/*
	 * MODEL PARAMETERS
	 * 
	 */
	
	public final int WORLD_SIZE = 250;
	private final int START_NUMBER_OF_BALLOONS = WORLD_SIZE*WORLD_SIZE;
	private final int DELAY_TIME = 2;
	private final int NUMBER_OF_STEPS = 1000;
	private final int NUMBER_OF_CURRENTS = 3;

	/*
	 * CONTAINERS USED BY THE MODEL
	 */
	private ArrayList<WindLayer> stratosphere;
	private ArrayList<Balloon> balloons;
	private int[][] grid;

	/*
	 * STATISTICAL VARIABLES USED FOR MEASUREMENT
	 */

	/*	Coverage is the ratio between connected and unconnected points in the grid at each step.
		This variable collects accumulated coverage over the simulation, to be used to represent
		simulationCoverage
	 */

	private double accumulatedCoverage;
	//droppedConnection is the accumulated number of 0s in the grid
	private int droppedConnections;

	//simulationCoverage is the accumulated coverage for each step divided by the number of steps
	private double simulationCoverage;

	//The number of entries in grid that carry 0. This variable is updated on the fly, when balloon is moved
	private int notConnected;

	public World(){
		//Initialize the lists. Balloons and wind layers
		stratosphere = new ArrayList<>();
		balloons = new ArrayList<>();

		//Initialize the grid that represents the surface
		grid = new int[WORLD_SIZE][WORLD_SIZE];
	}

	public void init(){
		//initialize statistical variables
		accumulatedCoverage = 0;
		droppedConnections = 0;
		simulationCoverage = 0;
		notConnected = 0;

		//add wind layers
		WindLayer w1 = new WindLayer("",WORLD_SIZE, 0);
		WindLayer w2 = new WindLayer("",WORLD_SIZE, 1);
		WindLayer w3 = new WindLayer("",WORLD_SIZE, 2);

		stratosphere.add(w1);
		stratosphere.add(w2);
		stratosphere.add(w3);

		/*
		 * Initialize the earth grid:
		 * consists of integers that indicate
		 * the number of balloons on each spot
		 * */

		for(int i = 0; i < WORLD_SIZE; i++){
			for(int j = 0; j < WORLD_SIZE; j++){
				grid[i][j] = 0;
			}
		}

		/*
		 * Initialize the balloon list and create all
		 * the balloons
		 * 
		 * */

		for(int i = 0; i < START_NUMBER_OF_BALLOONS; i++){
			createBalloon();
		}
	}
	public String step(){
		applyDecision();		
		applyCurrents();
		updateStatistics();
		return toString();

	}
	private void moveBetweenLayers(Balloon balloon) {

		//NextLayer becomes the current layer
		//and delay is reset to 0
		//and nextLayer is reset to null
		
		balloon.setWindLayer(balloon.getNextWindLayer());
		balloon.setDelay(0);
		balloon.setNextLayer(null);
	}

	public void applyCurrents(){
		for(Balloon b : balloons){
			moveBalloon(b);
		}

	}
	public void applyDecision(){

		/*	Apply decisions to all balloons that are not currently moving 
		 *	to another layer
		 */
		for(Balloon b : balloons){
			if(b.getDelay()==0){
				applyDecision1(b);
			}
		}

	}

	private void applyDecision1(Balloon b) {
		int x = b.getX();
		int y = b.getY();
		if(grid[x][y]>1){
			b.setDelay(DELAY_TIME);
			int currentID = b.getWindLayer().getId();
			//move to the next below if at the top. Else random
			if(currentID==NUMBER_OF_CURRENTS-1){
				b.setNextLayer(stratosphere.get(currentID-1));
			}
			else if(currentID==0){
				b.setNextLayer(stratosphere.get(currentID+1));
			}
			else{
				Random rand = new Random();
				boolean sign = rand.nextBoolean();
				if(sign){
					b.setNextLayer(stratosphere.get(currentID+1));
				}
				else{
					b.setNextLayer(stratosphere.get(currentID-1));
				}
			}
		}
	}

	public void createBalloon(){
		Balloon b = new Balloon(0,0,stratosphere.get(0));
		balloons.add(b);
		grid[0][0]++;
	}

	public void moveBalloon(Balloon balloon){
		/*
		 * This function is responsible for moving balloons. It first has to check
		 * whether the balloons are moving between wind layers, and make the
		 * appropriate updates
		 * */
		
		//Check if it is time to apply new wind layer
		if(balloon.getDelay()==1){
			moveBetweenLayers(balloon);
		}
		else{
			balloon.decreaseDelay();
		}
		
		//Get current coordinates
		int x = balloon.getX();
		int y = balloon.getY();

		//Update the grid
		grid[x][y]--;

		//if the old slot has 0, then number of notConnected increases by one
		if(grid[x][y]==0){notConnected++;droppedConnections++;}
		//Move the balloon
		balloon.moveWithWind();
		adjustPosition(balloon);

		//Get new coordinates
		int newX = balloon.getX();
		int newY = balloon.getY();

		//If the new slot has 0,then it will no longer be unconnected
		if(grid[newX][newY]==0){notConnected--;}

		//Update the grid
		grid[newX][newY]++;

	}

	//Method used to adjust the position of the balloon when it travels out of bounds
	private void adjustPosition(Balloon balloon) {
		int x = balloon.getX();
		int y = balloon.getY();

		if(x>=WORLD_SIZE){
			balloon.setX(x-WORLD_SIZE);
		}
		if(y>=WORLD_SIZE){
			balloon.setY(y-WORLD_SIZE);
		}
		if(x<0){
			balloon.setX(x+WORLD_SIZE);
		}
		if(y<0){
			balloon.setY(y+WORLD_SIZE);
		}

	}

	private void updateStatistics() {
		accumulatedCoverage = accumulatedCoverage+(notConnected/WORLD_SIZE);

	}

	@Override
	public String toString() {

		StringBuilder ret = new StringBuilder("Status of the world:\n");
		ret.append("Number of balloons: "+balloons.size()+"\n");
		ret.append("Position of balloons:\n");

			for(int i = 0; i<WORLD_SIZE; i++){
				for(int j = 0; j<WORLD_SIZE;j++){

					ret.append(grid[i][j]);
					ret.append("*");
				}
				ret.append('\n');
			}
		return ret.toString();
	}

	public String printStats() {

		StringBuilder stats = new StringBuilder("Statistics for run.\n\n");
		stats.append("Coverage over simulation: "+(accumulatedCoverage/NUMBER_OF_STEPS)+"\n");
		stats.append("Dropped conncetions: "+droppedConnections+"\n");
		return stats.toString();
	}


	public void simulate(){
		int runner = 0;
		while(runner < NUMBER_OF_STEPS){
			step();
			runner++;
		}
		simulationCoverage = accumulatedCoverage/NUMBER_OF_STEPS;
	}

	public ArrayList<Balloon> getBalloons() {
		return balloons;
	}




}

