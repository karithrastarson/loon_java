package service;

import java.util.ArrayList;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;

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

	public final int WORLD_SIZE = 250;
	private final int RANGE = 5;
	private final double EXTRA_BALLOONS = 1.5;
	private final int NUMBER_OF_BALLOONS = (int) ((int) WORLD_SIZE*WORLD_SIZE*EXTRA_BALLOONS);
	private final int VERTICAL_SPEED = 3;
	private final int NUMBER_OF_STEPS = 1000;
	private final int NUMBER_OF_CURRENTS = 4;
	private final int MAX_ALTITUDE = 12;
	private final int MIN_ALTITUDE = 0;
	private final int TOTAL_CELLS = WORLD_SIZE*WORLD_SIZE;
	private FileOutputStream fos;
	private final String SIMULATION_COVERAGE_FILE = "simulation_coverage_alg1_noDelay.txt";
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
	
	private long runtime;

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
		fos = new FileOutputStream(SIMULATION_COVERAGE_FILE);


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


	
	private void moveBetweenLayers(Balloon balloon, WindLayer newLayer) {


		balloon.setWindLayer(newLayer);
		balloon.stopVertical();

	}

//	public void applyCurrents(){
//		for(Balloon b : balloons){
//			moveBalloon(b);
//		}
//
//	}
//	public void applyDecision(){
//
//		/*	Apply decisions to all balloons that are not currently moving 
//		 *	to another layer
//		 */
//		for(Balloon b : balloons){
//			applyDecision2(b);
//		}
//
//	}
	public String step() throws IOException{
		currentStep++;

		for(Balloon b: balloons){
			applyDecision1(b);
			moveBalloon(b);
		}
	
		updateStatistics();

		System.out.println("Step "+currentStep + " is complete.");

		return toString();

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
		 * */
		int x = b.getX();
		int y = b.getY();

		if(grid[x][y] > 1){
			//If the cell is crowded, then staying is not an option. Must move
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
			
			//Lets see what neighbouring currents would do
			//Option move down
			int projectedXBelow = x + stratosphere.get(layerBelow).getWind(x, y).getFirst();
			int projectedYBelow = y + stratosphere.get(layerBelow).getWind(x, y).getSecond();
			
			int optionGoDown = grid[getAdjustedX(projectedXBelow)][getAdjustedY(projectedYBelow)];
			
			//Option move up
			int projectedXAbove = x + stratosphere.get(layerAbove).getWind(x, y).getFirst();
			int projectedYAbove = y + stratosphere.get(layerAbove).getWind(x, y).getSecond();
			
			int optionGoUp = grid[getAdjustedX(projectedXAbove)][getAdjustedY(projectedYAbove)];
			
			  if(optionGoUp <= optionGoDown){
				//GO UP
				
				b.goUp();
			}

			else{
				//GO DOWN
			
				b.goDown();
			}
			
		}
	}
	
	private void applyDecision3(Balloon b){
		/*
		 * Control algorithm 3
		 * 
		 * Slight improvement over algorithm 2. Instead of locating 
		 * the least occupied area, it finds a critical area in the grid
		 * and navigates towards that space. This function relies on 
		 * findCritical and distance private functions.
		 * 
		 * */
		int x = b.getX();
		int y = b.getY();

		if(grid[x][y] > 1){
			//If the cell is crowded, then staying is not an option. Must move
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
			
			//Find critical area in the grid
			Pair<Integer,Integer> criticalPoint = findCriticalPoint(x,y);
			
			//Lets see what neighbouring currents would do
			//Option move down
			int projectedXBelow = x + stratosphere.get(layerBelow).getWind(x, y).getFirst();
			int projectedYBelow = y + stratosphere.get(layerBelow).getWind(x, y).getSecond();
			
			
			//Option move up
			int projectedXAbove = x + stratosphere.get(layerAbove).getWind(x, y).getFirst();
			int projectedYAbove = y + stratosphere.get(layerAbove).getWind(x, y).getSecond();
			
			
			//What option moves the balloon closest to the critical area
			Pair<Integer,Integer> optionDown = new Pair<Integer,Integer>(projectedXBelow,projectedYBelow);
			Pair<Integer,Integer> optionUp = new Pair<Integer,Integer>(projectedXAbove,projectedYAbove);
		
			
			double optionGoUp = distance(criticalPoint,optionUp);
			double optionGoDown = distance(criticalPoint,optionDown);
			
			  if(optionGoUp <= optionGoDown){
				//GO UP
				b.goUp();
			}

			else{
				//GO DOWN
				b.goDown();
			}
			
		}
	}

	private Pair<Integer,Integer> findCriticalPoint(int xB, int yB ){
		int criticalX = 0;
		int criticalY = 0;
		
		/*
		 * This method browses through the whole world grid and analyzes
		 * the criticality of each cell. The size of the range that is
		 * taken into consideration is the global variable RANGE. When 
		 * all cells have been examined and rated, the one with the lowest
		 * score is returned as the most critical cell
		 * */
		int runningSum = 0;
		int currentLow = 10;
		for(int x=(xB-RANGE); x<(xB+RANGE); x++){
			for(int y=(yB-RANGE); y<(yB+RANGE); y++){
				//Now find the sum of all the cells in the RANGE
				runningSum = 0;
				for(int xRange = (x-RANGE); xRange<(x+RANGE); xRange++){
					for(int yRange = (y-RANGE); yRange<(y+RANGE); yRange++){
						runningSum += grid[getAdjustedX(xRange)][getAdjustedY(yRange)];
					}
				}
				if(runningSum<currentLow){
					currentLow = runningSum;
					criticalX = x;
					criticalY = y;
					}
				
			}
		}
		
		Pair<Integer,Integer> critical = new Pair<>(criticalX,criticalY);
		return critical;
	}
	
	private static double distance(Pair<Integer,Integer> a, Pair<Integer,Integer> b){
		/*
		 * Helper function that returns the distance between two
		 * points in a plane according to the distance formula:
		 * 
		 * d = sqrt((x2-x1)^2+(y2-y1)^2)
		 * */
		int x1 = a.getFirst();
		int y1 = a.getSecond();
		int x2 = b.getFirst();
		int y2 = b.getSecond();
		
		
		return (Math.sqrt(Math.pow((x2-x1), 2)+Math.pow((y2-y1), 2)));
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
		
//		if(altitude>=0 && altitude <1){return stratosphere.get(0);}
//		if(altitude>=1 && altitude <2){return stratosphere.get(1);}
//		if(altitude>=2 && altitude <3){return stratosphere.get(2);}
//		if(altitude>=3){return stratosphere.get(3);}

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
		StringBuilder str = new StringBuilder();
	
		float currentCoverage = (float)(TOTAL_CELLS-notConnected)/(TOTAL_CELLS);
		
		DecimalFormat twoPlaces = new DecimalFormat("0.000000");
		str.append(currentStep);
		str.append("\t");
		str.append(twoPlaces.format(currentCoverage).replaceAll(",","."));
		
		str.append(System.getProperty("line.separator"));
		//String n = Integer.toString(currentStep) +"\t" + Float.toString(currentCoverage) +"\n";
		fos.write(str.toString().getBytes());
		//Initialize the file to write coverage data
	}

	@Override
	public String toString() {


		return printStats();
	}

	public String printStats() {

		StringBuilder stats = new StringBuilder("Statistics for run.\n\n");
		
		stats.append("Runtime:" + runtime/1000000+"ms\n");
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
		 long start = System.nanoTime();
		while(runner < NUMBER_OF_STEPS){
			step();
			runner++;
		}
		runtime = System.nanoTime() - start;
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
	public static void main(String args[]){
//		Pair<Integer,Integer> a = new Pair<Integer,Integer>(1,1);
//		Pair<Integer,Integer> b = new Pair<Integer,Integer>(1,4);
//		double d = distance(a,b);
//		System.out.println(d);
	}



}

