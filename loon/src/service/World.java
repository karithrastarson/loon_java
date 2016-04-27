package service;

import java.util.ArrayList;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import bo.Balloon;
import bo.WindLayer;
import structures.Pair;

public class World {

	/*
	 * MODEL PARAMETERS
	 * 
	 * World size
	 * Start number of balloons
	 * Vertical speed is the speed of the balloon up or down between layers
	 * Number of steps in the simulation
	 * Number of currents in the system
	 * Max altitude of a balloon
	 * Min altitude of a balloon
	 * 
	 */

	public final int WORLD_SIZE = 205;
	private final int NUMBER_OF_BALLOONS = WORLD_SIZE*WORLD_SIZE;
	private final int VERTICAL_SPEED = 3;
	private final int NUMBER_OF_STEPS = 1000;
	private final int NUMBER_OF_CURRENTS = 4;
	private final int MAX_ALTITUDE = 12;
	private final int MIN_ALTITUDE = 0;
	private final int TOTAL_CELLS = WORLD_SIZE*WORLD_SIZE;
	private FileOutputStream fos;

	/*
	 * CONTAINERS USED BY THE MODEL
	 */
	private ArrayList<WindLayer> stratosphere;
	private ArrayList<Balloon> balloons;
	private int[][] grid;



	//Various variables
	private int currentStep;
	private static BufferedWriter coverageWriter;
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

	public void init() throws FileNotFoundException, IOException{
		//initialize statistical variables
		accumulatedCoverage = 0;
		droppedConnections = 0;
		simulationCoverage = 0;
		notConnected = 0;
		currentStep = 0;

		//add wind layers
		WindLayer w1 = new WindLayer(WORLD_SIZE, 0);
		WindLayer w2 = new WindLayer(WORLD_SIZE, 1);
		WindLayer w3 = new WindLayer(WORLD_SIZE, 2);
		WindLayer w4 = new WindLayer(WORLD_SIZE, 3);

		stratosphere.add(w1);
		stratosphere.add(w2);
		stratosphere.add(w3);
		stratosphere.add(w4);

		writeWindLayersToFile();


		//initialize output stream
		fos = new FileOutputStream("simulation_coverage.txt");


		/*
		 * Initialize the earth grid:
		 * consists of integers that indicate
		 * the number of balloons on each spot
		 * */

		for(int i = 0; i < WORLD_SIZE; i++){
			for(int j = 0; j < WORLD_SIZE; j++){
				grid[i][j] = 0;
				notConnected++;
			}
		}

		/*
		 * Initialize the balloon list and create all
		 * the balloons
		 * 
		 * */

		for(int i = 0; i < NUMBER_OF_BALLOONS; i++){
			createBalloon();
		}
	}


	public String step() throws IOException{
		currentStep++;
		applyDecision();		
		applyCurrents();
		updateStatistics();



		return toString();

	}
	private void moveBetweenLayers(Balloon balloon, WindLayer newLayer) {


		balloon.setWindLayer(newLayer);
		balloon.stopVertical();

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
			applyDecision2(b);
		}

	}

	private void applyDecision1(Balloon b) {
		//Control Algorithm 1
		int x = b.getX();
		int y = b.getY();

		//If more than one balloons occupy this space, and balloon not moving,then start moving up or down
		boolean isMoving = b.isMovingDown()||b.isMovingUp();

		if(grid[x][y]>1 && !isMoving){

			int currentID = b.getWindLayer().getId();
			//Start moving down if at top. Else randomly up or down
			if(currentID==NUMBER_OF_CURRENTS-1){
				b.goDown();
			}
			else if(currentID==0){
				b.goUp();
			}
			else{
				Random rand = new Random();
				boolean sign = rand.nextBoolean();
				if(sign){
					b.goUp();
				}
				else{
					b.goDown();
				}
			}
		}
	}

	private void applyDecision2(Balloon b){
		/*
		 * Control algorithm 2
		 * 
		 * Slight improvement over algorithm 1 since this one does not
		 * decide randomly whether a balloon should move up or down when at
		 * a crowded space. It looks at the neighbouring wind layers and determines 
		 * which direction will blow him to a less occupied spaces
		 * 
		 * Also it does not matter if the balloon is already moving, the vertical 
		 * direction can still be changed
		 * 
		 * PROBLEM V.1.0:
		 * The algorithm doesn't anticipate all the balloons on its way to the same place
		 * That's why nothing erupts the status quo
		 * 
		 * */
		int x = b.getX();
		int y = b.getY();
		boolean isMoving = b.isMovingDown()||b.isMovingUp();
		if(grid[x][y] > 1 && !isMoving){
			int currentID = b.getWindLayer().getId();
			
			//find the neighbouring layers
			int layerBelow = currentID;
			int layerAbove = currentID;
			
			//Fix boundary cases. Top or bottom layer
			if(currentID == 0){
				layerBelow = currentID; //unchanged
				layerAbove = currentID + 1;
		
			}
			if(currentID == NUMBER_OF_CURRENTS-1){
				layerAbove = currentID; //unchanged
				layerBelow = currentID - 1;

			}
			
			
			//The cell this balloon is headed for
			int projectedX = x+b.getWindLayer().getWind(x, y).getFirst();
			int projectedY = y+b.getWindLayer().getWind(x, y).getSecond();
			
			//Correction
//			int optionStay = grid[getAdjustedX(projectedX)][getAdjustedY(projectedY)];
			int optionStay = grid[x][y];
			//Lets see what neighbouring currents would do
			//Option move down
			int projectedXBelow = x + stratosphere.get(layerBelow).getWind(x, y).getFirst();
			int projectedYBelow = y + stratosphere.get(layerBelow).getWind(x, y).getSecond();
			
			int optionGoDown = grid[getAdjustedX(projectedXBelow)][getAdjustedY(projectedYBelow)];
			
			//Option move up
			int projectedXAbove = x + stratosphere.get(layerAbove).getWind(x, y).getFirst();
			int projectedYAbove = y + stratosphere.get(layerAbove).getWind(x, y).getSecond();
			
			int optionGoUp = grid[getAdjustedX(projectedXAbove)][getAdjustedY(projectedYAbove)];
			
			//Now we find the lowest number of the three options and select that option
			 if(optionStay <= optionGoUp && optionStay <= optionGoDown){
				//STAY
				b.stopVertical();
			
			}
			 else if(optionGoUp <= optionStay && optionGoUp <= optionGoDown){
				//GO UP
				
				b.goUp();
			}

		
			else{
				//GO DOWN
			
				b.goDown();
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

		//update altitude
		balloon.updateAltitude(VERTICAL_SPEED);
		adjustAltitude(balloon);

		//Check if it is time to apply new wind layer
		//We find the wind layer that should apply to a balloon in that altitude
		WindLayer correctLayer = getLayerFromAltitude(balloon.getAltitude());

		if(!balloon.getWindLayer().equals(correctLayer)){
			//the wind layers don't match so we update
			moveBetweenLayers(balloon,correctLayer);
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

	private WindLayer getLayerFromAltitude(int altitude) {
		//THIS FUNCTION HAS TO BE IMPLEMENTED IN A NICER WAY

		if(altitude>=0 && altitude <3){return stratosphere.get(0);}
		if(altitude>=3 && altitude <6){return stratosphere.get(1);}
		if(altitude>=6 && altitude <9){return stratosphere.get(2);}
		if(altitude>=9){return stratosphere.get(3);}

		return null;
	}

	private void adjustAltitude(Balloon balloon) {
		/*If a balloon moves below the MIN ALTITUDE
		 * or above the MAX ALTITUDE then it is moved back to the 
		 * boundaries and the vertical movement is stopped
		 * 
		 */ 

		if(balloon.getAltitude()<MIN_ALTITUDE){
			balloon.setAltitude(MIN_ALTITUDE);
			balloon.stopVertical();
		}
		if(balloon.getAltitude()>MAX_ALTITUDE){
			balloon.setAltitude(MAX_ALTITUDE);
			balloon.stopVertical();
		}

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
	
	private int getAdjustedX(int oldX){
		if(oldX < 0){
			return oldX + WORLD_SIZE;
		}
		if(oldX >= WORLD_SIZE){
			return oldX - WORLD_SIZE;
		}
		
		return oldX;
	}
	private int getAdjustedY(int oldY){
		if(oldY < 0){
			return oldY + WORLD_SIZE;
		}
		if(oldY >= WORLD_SIZE){
			return oldY - WORLD_SIZE;
		}
		return oldY;
	}

	private void updateStatistics() throws IOException {
		accumulatedCoverage += (notConnected/(WORLD_SIZE*WORLD_SIZE));


		float currentCoverage = (float)(TOTAL_CELLS-notConnected)/(TOTAL_CELLS);
		String n = Integer.toString(currentStep) +"\t" + Float.toString(currentCoverage) +"\n";
		fos.write(n.getBytes());
		//Initialize the file to write coverage data
	}

	@Override
	public String toString() {


		return printStats();
	}

	public String printStats() {

		StringBuilder stats = new StringBuilder("Statistics for run.\n\n");
		stats.append("Number of steps:" + NUMBER_OF_STEPS+"\n");
		stats.append("World size: " + WORLD_SIZE + "\n");
		stats.append("Number of balloons: " + NUMBER_OF_BALLOONS + "\n");
		stats.append("Number of wind layers " + stratosphere.size() + "\n");

		stats.append("Coverage over simulation: "+(accumulatedCoverage/NUMBER_OF_STEPS)+"\n");
		stats.append("Dropped conncetions: "+droppedConnections+"\n");
		return stats.toString();
	}


	public void simulate() throws IOException{
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

	private void writeWindLayersToFile() {
		//Write the layers to a file for vizualization
		int count = 1;
		for(WindLayer windlayer : stratosphere){
			File fileX = new File("windlayer"+count+"_X.txt");
			File fileY = new File("windlayer"+count+"_Y.txt");

			try{

				//FileOutputStream outx = new FileOutputStream(fileX);
				//FileOutputStream outy = new FileOutputStream(fileY);

				BufferedWriter outx = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileX)));
				BufferedWriter outy = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileY)));
				for(int x = 0; x < WORLD_SIZE; x++){
					for(int y = 0; y < WORLD_SIZE; y++){



						//byte[] writeX = windlayer.getWind(x, y).getFirst().toString().getBytes();
						//	byte[] writeY = windlayer.getWind(x, y).getSecond().toString().getBytes();


						outx.write(windlayer.getWind(x, y).getFirst().toString());
						outy.write(windlayer.getWind(x, y).getSecond().toString());

						outx.write("\t");
						outy.write("\t");

					}

					outx.newLine();
					outy.newLine();

				}
				outx.close();
				outy.close();
				count++;	
			}catch(Exception e){}

		}
	}




}

