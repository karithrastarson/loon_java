package service;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;
import java.awt.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import bo.Balloon;
import bo.WindLayer;
import structures.ObjectGrid;
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
	 * TOTAL_CELLS is used for statistical calculations
	 * ALGORITHM chooses which algorithm to use
	 * FileOutputStream is the stream in which the data will be collected
	 * The two files are used for storing the data from the simualation
	 * 
	 */

	public final int WORLD_SIZE = 1000;
	private final int RANGE = 10;
	private final int LIFETIME = 2000;
	private final double EXTRA_BALLOONS = 1.25;
	private final int NUMBER_OF_BALLOONS = (int) ((int) WORLD_SIZE*WORLD_SIZE*EXTRA_BALLOONS);
	private final int VERTICAL_SPEED = 40 ;
	private final int NUMBER_OF_STEPS = 2000;
	private final int NUMBER_OF_CURRENTS = 4;
	private final int MAX_ALTITUDE = 400;
	private final int MIN_ALTITUDE = 0;
	private final int TOTAL_CELLS = WORLD_SIZE*WORLD_SIZE;
	private final char ALGORITHM = '3';
	private FileOutputStream fos;
	private final String SIMULATION_COVERAGE_FILE = "outputData/simulation_coverage_alg"+ALGORITHM+".txt";
	private final String SIMULATION_HEATMAP = "outputData/Simulation_Heatmap_alg"+ALGORITHM+".txt";

	/*
	 * CONTAINERS USED BY THE MODEL
	 */
	private ArrayList<WindLayer> stratosphere;
	private ArrayList<Balloon> balloons;		
	private ObjectGrid<Balloon> balloons_grid;
	private int[][] grid;

	//heatmap counts how many times each cell interacts with a balloon
	private int[][] heatmap;

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
		balloons_grid = new ObjectGrid<Balloon>(WORLD_SIZE);
		heatmap = new int[WORLD_SIZE][WORLD_SIZE];



	}

	public void init() throws FileNotFoundException, IOException{
		//initialize statistical variables
		accumulatedCoverage = 0;
		droppedConnections = 0;
		simulationCoverage = 0;
		notConnected = 0;
		currentStep = 0;

		//add wind layers
		//		WindLayer w1 = new WindLayer(WORLD_SIZE,"windlayer1_X.txt","windlayer1_Y.txt", 0);
		//		WindLayer w2 = new WindLayer(WORLD_SIZE,"windlayer1_X.txt","windlayer1_Y.txt",  1);
		//		WindLayer w3 = new WindLayer(WORLD_SIZE,"windlayer1_X.txt","windlayer1_Y.txt",  2);
		//		WindLayer w4 = new WindLayer(WORLD_SIZE,"windlayer1_X.txt","windlayer1_Y.txt",  3);
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
				heatmap[i][j] = 0;

				notConnected++;
			}
		}

		/*
		 * Initialize the balloon list and create all
		 * the balloons
		 * 
		 * */

//				for(int i = 0; i < NUMBER_OF_BALLOONS; i++){
//					createBalloon();
//				}

	}

	public void step() throws IOException{
		currentStep++;
		for(Balloon b: balloons){

			switch(ALGORITHM){
			case '1':applyDecision1(b);
			break;
			case '2': applyDecision2(b);
			break;
			case '3': applyDecision3(b);
			break;
			case '4': applyDecision4(b);
			break;
			}
			moveBalloon(b);
		}
		updateStatistics();

		System.out.println("Step "+currentStep + " is complete.");
	}

	private void moveBetweenLayers(Balloon balloon, WindLayer newLayer) {

		/*
		 * This function moves a balloon to a new layer and stops the vertical
		 * movement of the balloon if it has reached the wind layer it was headed for
		 * 
		 * 
		 * */
		balloon.setWindLayer(newLayer);

		if(newLayer.equals(balloon.getNextLayer())){
			//has the balloon reached the layer it is headed for?
			balloon.stopVertical();
		}

	}


	private void applyDecision1(Balloon b) {
		//Control Algorithm 1
		int x = b.getX();
		int y = b.getY();

		//if(grid[x][y]>1){
		if(balloons_grid.duplicates(b, x, y)){
			int currentID = b.getWindLayer().getId();
			//Start moving down if at top. Else randomly up or down
			if(currentID==NUMBER_OF_CURRENTS-1){
				b.goDown();
				b.setNextLayer(stratosphere.get(currentID - 1));
			}
			else if(currentID==0){
				b.goUp();
				b.setNextLayer(stratosphere.get(currentID + 1));
			}
			else{
				Random rand = new Random();
				boolean sign = rand.nextBoolean();
				if(sign){
					b.goUp();
					b.setNextLayer(stratosphere.get(currentID + 1));
				}
				else{
					b.goDown();
					b.setNextLayer(stratosphere.get(currentID - 1));
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

		//if(grid[x][y] > 1){
		if(balloons_grid.duplicates(b, x, y)){
			//If the cell is crowded, then staying is not an option. Must move
			int currentID = b.getWindLayer().getId();

			//find the neighbouring layers
			int layerBelow = currentID;
			int layerAbove = currentID;

			//Fix boundary cases. Top or bottom layer
			if(currentID == 0){
				layerAbove = currentID + 1;
			}
			if(currentID == NUMBER_OF_CURRENTS-1){
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

			if(optionGoUp < optionGoDown && optionGoUp < grid[x][y]){
				//GO UP
				b.goUp();
			}

			else if(optionGoDown < grid[x][y]){
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

		if(balloons_grid.duplicates(b, x, y)){
			//If the cell is crowded, then staying is not an option. Must move
			int currentID = b.getWindLayer().getId();

			//Find critical area in the grid
			Pair<Integer,Integer> criticalPoint = findCriticalPoint(x,y);


			int projectedX = 0;
			int projectedY = 0;
			double bestOption = 10000;
			WindLayer bestLayer = null;
			Pair<Integer,Integer> optionPair =  new Pair<Integer,Integer>(projectedX,projectedY);

			for(WindLayer wl : stratosphere){
				projectedX = x + wl.getWind(x, y).getFirst();
				projectedY = y + wl.getWind(x, y).getSecond();

				optionPair.setFirst(projectedX);
				optionPair.setSecond(projectedY);

				if(distance(optionPair, criticalPoint)<bestOption){
					bestOption = distance(optionPair, criticalPoint);
					bestLayer = wl;
				}

			}


			//Determine where the new layer is in relation to the old
			if(bestLayer.getId()>currentID){
				b.goUp();
				b.setNextLayer(bestLayer);
			}
			else if(bestLayer.getId()<currentID){
				b.goDown();
				b.setNextLayer(bestLayer);
			}
			else{
				b.stopVertical();
			}
		}
	}

	private void applyDecision4(Balloon b){
		/*This algorithm only lets the balloons move when it is not already
		 * moving to a new layer. By restricting the time that the balloon has to move we avoid 
		 * constantly changing directions aimlessly. Every balloon is given
		 * time to reach its desired layer, and then and only then it is allowed
		 * to change direction
		 */

		//The first if loop is to check whether the balloon is moving
		if(!(b.isMovingDown() || b.isMovingUp())){

			applyDecision3(b);

		}
	}
	private Pair<Integer,Integer> findCriticalPoint(int xB, int yB ){


		/*
		 * This method browses through the whole world grid and measures
		 * the "criticality" of each cell. The size of the range that is
		 * taken into consideration is the global variable RANGE. When 
		 * all cells have been examined and rated, the one with the lowest
		 * score is returned as the most critical cell. 
		 * 
		 * The currentLow is initialized as some random high number, to make sure that 
		 * the loops will find some point with a lower score.
		 * 
		 * The runningSum variable is used as a temporary variable when calculating
		 * the crtiticality for a given cell
		 * */

		int criticalX = 0;
		int criticalY = 0;
		int runningSum = 0;
		int currentLow = 10;
		for(int x=(xB-RANGE); x<(xB+RANGE); x++){
			for(int y=(yB-RANGE); y<(yB+RANGE); y++){
				//Now find the sum of all the cells in the RANGE
				runningSum = 0;
				for(int xRange = (x-RANGE); xRange<(x+RANGE); xRange++){
					for(int yRange = (y-RANGE); yRange<(y+RANGE); yRange++){
						//runningSum += grid[getAdjustedX(xRange)][getAdjustedY(yRange)];
						runningSum += balloons_grid.getObjects(getAdjustedX(xRange), getAdjustedY(yRange)).size();
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
	private void createBalloon(){
		Balloon b = new Balloon(0,0,stratosphere.get(0));
		balloons.add(b);
		balloons_grid.addObject(b, 0, 0);
		grid[0][0]++;
		heatmap[0][0]++;
	}
	private void removeBalloon(Balloon b){
		int x = b.getX();
		int y = b.getY();
		
		grid[x][y]--;
		if(grid[x][y]==0){droppedConnections++;notConnected++;}
		balloons_grid.removeObject(b, x, y);
		balloons.remove(b);
		
	}

	private void createBalloon(int x, int y){
		Balloon b = new Balloon(x,y,stratosphere.get(0));
		balloons.add(b);
		balloons_grid.addObject(b, x, y);
		grid[x][y]++;
		heatmap[x][y]++;
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

		//Update the grid. The balloon is leaving these coordinates.
		grid[x][y]--;

		//if the old slot has 0, then number of notConnected increases by one
		//then it also means that the connection is dropped at that location
		if(grid[x][y]==0){notConnected++;droppedConnections++;}

		Balloon tmp = balloon;
		//Move the balloon
		balloon.moveWithWind();
		adjustPosition(balloon);

		//Get new coordinates
		int newX = balloon.getX();
		int newY = balloon.getY();

		balloons_grid.moveObject(tmp, x, y, newX, newY);
		//If the new slot has 0,then it will no longer be unconnected
		if(grid[newX][newY]==0){notConnected--;}

		//Update the grid
		grid[newX][newY]++;


		//Update heatmap
		heatmap[newX][newY]++;

	}

	private WindLayer getLayerFromAltitude(int altitude) {
		//THIS FUNCTION HAS TO BE IMPLEMENTED IN A NICER WAY

		if(altitude>=MIN_ALTITUDE && altitude < (MAX_ALTITUDE/4)){return stratosphere.get(0);}
		if(altitude>=(MAX_ALTITUDE/NUMBER_OF_CURRENTS) && altitude < (MAX_ALTITUDE/2)){return stratosphere.get(1);}
		if(altitude>=(MAX_ALTITUDE/2) && altitude < (3*MAX_ALTITUDE/2)){return stratosphere.get(2);}
		if(altitude>=(3*MAX_ALTITUDE/2)){return stratosphere.get(3);}

		System.out.println("No wind layer found for altitude: " + altitude);
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


	private void adjustPosition(Balloon balloon) {
		/*
		 *Method used to adjust the position of the balloon when it travels out of bounds
		 * 
		 */

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
		/*
		 * Similar function as adjustPosition but just returns
		 * the coordinate in stead of moving an actual balloon object
		 * */
		if(oldX < 0){
			return oldX + WORLD_SIZE;
		}
		if(oldX >= WORLD_SIZE){
			return oldX - WORLD_SIZE;
		}

		return oldX;
	}
	private int getAdjustedY(int oldY){
		/*
		 * Similar function as adjustPosition but just returns
		 * the coordinate in stead of moving an actual balloon object
		 * */
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
		printHeatMap();

		runtime = System.nanoTime() - start;
		simulationCoverage = accumulatedCoverage/NUMBER_OF_STEPS;

	}


	private void printHeatMap() {
		/*
		 * This function prints the heatmap array to file.
		 * 
		 * This is used to vizualize the  most frequently visited cells
		 * */

		File fileHeat = new File(SIMULATION_HEATMAP);
		try {
			BufferedWriter buffHeat = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileHeat)));
			for(int x = 0; x < WORLD_SIZE; x++){
				for(int y = 0; y < WORLD_SIZE; y++){

					buffHeat.write(Integer.toString(heatmap[x][y]));
					buffHeat.write("\t");

				}
				buffHeat.newLine();
			}
			buffHeat.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public ArrayList<Balloon> getBalloons() {
		return balloons;
	}

	private void writeWindLayersToFile() {
		//Write the layers to a file for vizualization
		int count = 1;
		for(WindLayer windlayer : stratosphere){
			File fileX = new File("outputData/windlayer"+count+"_X.txt");
			File fileY = new File("outputData/windlayer"+count+"_Y.txt");

			try{

				BufferedWriter outx = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileX)));
				BufferedWriter outy = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileY)));
				for(int x = 0; x < WORLD_SIZE; x++){
					for(int y = 0; y < WORLD_SIZE; y++){

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

	public ObjectGrid<Balloon> getBalloons_grid() {
		return balloons_grid;
	}

	public void simulate_AL() throws IOException{
		int runner = 0;
		long start = System.nanoTime();
		while(runner < NUMBER_OF_STEPS){
			step2();
			runner++;
		}
		printHeatMap();

		runtime = System.nanoTime() - start;
		simulationCoverage = accumulatedCoverage/NUMBER_OF_STEPS;
	}
	public void step2() throws IOException{
		currentStep++;
		
		if(balloons.size() < NUMBER_OF_BALLOONS){
			createBalloon();
		}
		try{
		for(Balloon b: balloons){
			b.age();
				switch(ALGORITHM){
				case '1':applyDecision1(b);
				break;
				case '2': applyDecision2(b);
				break;
				case '3': applyDecision3(b);
				break;
				case '4': applyDecision4(b);
				break;
				}
				moveBalloon(b);
			}}catch(Exception e){
				System.out.println("Error with balloons. Size: " + balloons.size());
			}
		
		//Now we remove all balloons that are too old
//		for(Balloon b : balloons){
//			if(b.getAge() > LIFETIME){
//				removeBalloon(b);
//				createBalloon();
//			}
//				}
		updateStatistics();

		System.out.println("Step "+currentStep + " is complete.");
	}
}

