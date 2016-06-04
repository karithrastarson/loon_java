package service;

import java.util.ArrayList;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

	public final int WORLD_SIZE = 500;
	public final int RANGE = 20;//(int) ((int) (Math.sqrt(2.0)*WORLD_SIZE)/(Math.sqrt(NUMBER_OF_BALLOONS)));
	private final int COMMUNICATION_RADIUS = 20;
	private final int NUMBER_OF_BALLOONS = (int) Math.ceil(0.5*(Math.pow(WORLD_SIZE,2)/Math.pow(RANGE,2)));
	private final int LIFETIME = 200;
	private final int VERTICAL_SPEED = 10 ;
	private final int NUMBER_OF_STEPS = 2000;
	private final int NUMBER_OF_CURRENTS = 4;
	private final int MAX_ALTITUDE = 400;
	private final int MIN_ALTITUDE = 0;
	private final int TOTAL_CELLS = WORLD_SIZE*WORLD_SIZE;
	private static String ALGORITHM;
	private FileOutputStream fos;
	private static String SIMULATION_COVERAGE_FILE ;
	private static String SIMULATION_HEATMAP ;
	private static String SIMULATION_RESULTS;

	/*
	 * CONTAINERS USED BY THE MODEL
	 */
	private ArrayList<WindLayer> stratosphere;
	private ArrayList<Balloon> balloons;		
	private ObjectGrid<Balloon> balloons_grid;
	//private int[][] grid;
	private boolean[][] coverage;

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

	private float accumulatedCoverage;
	//droppedConnection is the accumulated number of 0s in the grid
	private int droppedConnections;

	//The number of entries in grid that carry 0. This variable is updated on the fly, when balloon is moved
	private int notConnected;

	private int currentlyConnected;

	private long runtime;

	public World(){
		//Initialize the lists. Balloons and wind layers
		stratosphere = new ArrayList<>();
		balloons = new ArrayList<>();

		//Initialize the grid that represents the surface
		//	grid = new int[WORLD_SIZE][WORLD_SIZE];
		coverage = new boolean[WORLD_SIZE][WORLD_SIZE];
		balloons_grid = new ObjectGrid<Balloon>(WORLD_SIZE);
		heatmap = new int[WORLD_SIZE][WORLD_SIZE];
	}

	public void init(String alg) throws FileNotFoundException, IOException{
		//initialize statistical variables
		accumulatedCoverage = 0;
		droppedConnections = 0;
		notConnected = 0;
		currentStep = 0;
		currentlyConnected = 0;

		ALGORITHM = alg;

		SIMULATION_COVERAGE_FILE = "outputData/simulation_coverage_alg"+ALGORITHM+".txt";
		SIMULATION_HEATMAP = "outputData/Simulation_Heatmap_alg"+ALGORITHM+".txt";
		SIMULATION_RESULTS = "outputData/simulation_results.txt";

		WindLayer w1 = new WindLayer(WORLD_SIZE, 0, RANGE);
		WindLayer w2 = new WindLayer(WORLD_SIZE, 1, RANGE);
		WindLayer w3 = new WindLayer(WORLD_SIZE, 2, RANGE);
		WindLayer w4 = new WindLayer(WORLD_SIZE, 3, RANGE);

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

				//	grid[i][j] = 0;
				coverage[i][j] = false;
				heatmap[i][j] = 0;
				notConnected++;
			}
		}
	}

	private void moveBetweenLayers(Balloon balloon, WindLayer newLayer) {

		/*
		 * This function moves a balloon to a new layer and stops the vertical
		 * movement of the balloon if it has reached the wind layer it was headed for
		 * 
		 * */
		balloon.setWindLayer(newLayer);

		if(newLayer.equals(balloon.getNextLayer())){
			//has the balloon reached the layer it is headed for?

			//Then stop moving vertically
			balloon.stopVertical();
		}
	}

	private void applyDecision1(Balloon b) {
		//Control Algorithm 1
		int x = b.getX();
		int y = b.getY();

		//If there is another balloon in the same spot, and same altitude
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


			//Lets see what neighbouring currents would do
			//Option move down
			int projectedXBelow = x + stratosphere.get(layerBelow).getWind(x, y).getFirst();
			int projectedYBelow = y + stratosphere.get(layerBelow).getWind(x, y).getSecond();


			int optionGoDown = balloons_grid.getObjects(getAdjustedX(projectedXBelow), getAdjustedY(projectedYBelow)).size();

			//Option move up
			int projectedXAbove = x + stratosphere.get(layerAbove).getWind(x, y).getFirst();
			int projectedYAbove = y + stratosphere.get(layerAbove).getWind(x, y).getSecond();

			int optionGoUp = balloons_grid.getObjects(getAdjustedX(projectedXAbove), getAdjustedY(projectedYAbove)).size();
			if(optionGoUp < optionGoDown && optionGoUp < balloons_grid.getObjects(x, y).size()){
				//GO UP
				b.goUp();
			}

			else if(optionGoDown < balloons_grid.getObjects(x, y).size()){
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

	private void applyDecision3s(Balloon b){
		if(b.isMovingDown()==false && b.isMovingUp()==false){
			applyDecision3(b);
		}
	}

	private void applyDecision4(Balloon b){
		/*This algorithm implements a more strict balloon to balloon communication.
		 * 
		 * This method requires a balloon to only depend upon data from neighbouring balloons.
		 * This means that a balloon only has access to weather data from neighbouring layers
		 * 
		 */

		int x = b.getX();
		int y = b.getY();

		//First we have to randomly get the balloons accross several random layers
		if(b.getAge()<50){

			applyDecision1(b);
		}
		else{
			Pair<Integer,Integer> center = new Pair<>(x,y);
			Pair<Integer,Integer> checkpoint = new Pair<>(x,y);
			//Find all balloons in communication radius
			ArrayList<Balloon> neighbours = findAllNeighbours(x,y);
			//Now neighbours have been populated

			//Find wind layers available
			ArrayList<WindLayer> knownLayers = new ArrayList<>();
			for(Balloon tmpB : neighbours){
				WindLayer tmpL = tmpB.getWindLayer();

				if(!knownLayers.contains(tmpL)){
					knownLayers.add(tmpL);
				}
			}
			//Now we have all neighbouring balloons and windlayers

			//Now we explore projection points for all windlayers

			int fewest = 100;
			WindLayer bestLayer = b.getWindLayer();
			for(WindLayer wl : knownLayers){
				Pair<Integer,Integer> pp = new Pair<>(x+wl.getWind(x, y).getFirst(), y+wl.getWind(x, y).getSecond());
				int numberOfNeighbours = findAllNeighbours(pp.getFirst(), pp.getSecond()).size();
				if(numberOfNeighbours < fewest){
					fewest = numberOfNeighbours;
					bestLayer = wl;
				}
			}

			if(bestLayer.getId()>b.getWindLayer().getId()){
				b.goUp();
				b.setNextLayer(bestLayer);
			}
			else if(bestLayer.getId()<b.getWindLayer().getId()){
				b.goDown();
				b.setNextLayer(bestLayer);
			}
			else{
				b.stopVertical();
			}
		}

	}

	private void applyDecision4s(Balloon b){
		/*
		 * This algorithm computes the sum of all the vectors
		 * between the balloon and its neighbours, and then selects
		 * the known layer that will take it closes to that direction
		 */
		int x = b.getX();
		int y = b.getY();

		//First we have to randomly get the balloons accross several random layers
		if(b.getAge()<50){

			applyDecision1(b);
		}
		//Find all balloons in communication radius
		ArrayList<Balloon> neighbours = findAllNeighbours(x,y);
		//Now neighbours have been populated

		//Find wind layers available
		ArrayList<WindLayer> knownLayers = new ArrayList<>();
		knownLayers.add(b.getWindLayer());
		for(Balloon tmpB : neighbours){
			WindLayer tmpL = tmpB.getWindLayer();

			if(!knownLayers.contains(tmpL)){
				knownLayers.add(tmpL);
			}
		}
		//Now we have all neighbouring balloons and windlayers

		//Now we compute the VECTOR that points from (x,y) to the middle of them all
		Pair<Integer,Integer> heatspot = new Pair<Integer,Integer>(0,0);
		for(Balloon tmp : neighbours){
			//compute the vector
			int x_v = tmp.getX()-x;
			int y_v = tmp.getY()-y;
			heatspot.setFirst(heatspot.getFirst()+x_v);
			heatspot.setSecond(heatspot.getSecond()+y_v);
		}

		//So the critical point is (x,y) plus the heatspot vector
		Pair<Integer,Integer> criticalPoint = new Pair<Integer,Integer>(x+heatspot.getFirst(),y+heatspot.getSecond());

		//And the opposite is the same vector multiplied with -1
		criticalPoint.setFirst(criticalPoint.getFirst()*(-1));
		criticalPoint.setSecond(criticalPoint.getSecond()*(-1));

		//Now same logic from algorithm 3 to find a layer
		//But only use known layers
		int projectedX = 0;
		int projectedY = 0;
		double bestOption = 10000;
		WindLayer bestLayer = null;
		Pair<Integer,Integer> optionPair =  new Pair<Integer,Integer>(projectedX,projectedY);

		for(WindLayer wl : knownLayers){
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
		if(bestLayer.getId()>b.getWindLayer().getId()){
			b.goUp();
			b.setNextLayer(bestLayer);
		}
		else if(bestLayer.getId()<b.getWindLayer().getId()){
			b.goDown();
			b.setNextLayer(bestLayer);
		}
	}

	private ArrayList<Balloon> findAllNeighbours(int x, int y){
		Pair<Integer,Integer> center = new Pair<>(x,y);
		Pair<Integer,Integer> checkpoint = new Pair<>(x,y);

		ArrayList<Balloon> neighbours = new ArrayList<Balloon>();
		for(int i = x-COMMUNICATION_RADIUS; i < x + COMMUNICATION_RADIUS; i++){
			for(int j = y-COMMUNICATION_RADIUS; j < y + COMMUNICATION_RADIUS; j++){
				checkpoint.setFirst(getAdjustedX(i));
				checkpoint.setSecond(getAdjustedY(j));
				if(inCircle(center, checkpoint,COMMUNICATION_RADIUS)){
					for(Balloon bo : balloons_grid.getObjects(getAdjustedX(i), getAdjustedY(j))){
						neighbours.add(bo);
					}
				}
			}
		}

		return neighbours;
	}
	
	private Pair<Integer,Integer> findCriticalPoint(int xB, int yB ){

		/*
		 * This method browses through the range of a balloon and requests a measure of criticality from each balloon.
		 * 
		 * The criticality number is the number of balloons within the range. 
		 * 
		 * This algorithm favours groups and will probably do a bad job of spreading the balloons properly
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
		int currentLow = 100;
		ArrayList<Balloon> neigh = findAllNeighbours(xB,yB);
		for(Balloon b : neigh){
			ArrayList<Balloon> n = findAllNeighbours(b.getX(), b.getY());
			if(n.size() < currentLow){
				currentLow = n.size();
				criticalX = b.getX();
				criticalY = b.getY();
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
		heatmap[0][0]++;
	}
	private void removeBalloon(Balloon b){
		int x = b.getX();
		int y = b.getY();

		balloons_grid.removeObject(b, x, y);
		balloons.remove(b);
	}

	private void createBalloon(int x, int y){
		Balloon b = new Balloon(x,y,stratosphere.get(0));
		balloons.add(b);
		balloons_grid.addObject(b, x, y);
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
		Balloon tmp = balloon;
		
		//Move the balloon
		balloon.moveWithWind();
		adjustPosition(balloon);

		//Get new coordinates
		int newX = balloon.getX();
		int newY = balloon.getY();

		balloons_grid.moveObject(tmp, x, y, newX, newY);

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

		StringBuilder str = new StringBuilder();

		float currentCoverage = (float)(TOTAL_CELLS-notConnected)/(TOTAL_CELLS);


		currentCoverage = getCurrentCoverage();
		accumulatedCoverage += currentCoverage;

		DecimalFormat twoPlaces = new DecimalFormat("0.000000");
		str.append(currentStep);
		str.append("\t");
		str.append(twoPlaces.format(currentCoverage).replaceAll(",","."));

		str.append(System.getProperty("line.separator"));
		//String n = Integer.toString(currentStep) +"\t" + Float.toString(currentCoverage) +"\n";
		fos.write(str.toString().getBytes());
		//Initialize the file to write coverage data
	}
	
	public void print() {
		StringBuilder stats = new StringBuilder("Statistics for run."+new java.util.Date()+"\n\n");

		try {
			BufferedWriter writRes = new BufferedWriter
					(new FileWriter(SIMULATION_RESULTS,true));

			writRes.append("Statistics for run: "+new java.util.Date());
			writRes.newLine();
			writRes.append("Algorithm used: " + ALGORITHM);writRes.newLine();
			writRes.append("Runtime: " + runtime/1000000+"ms");writRes.newLine();
			writRes.append("Number of steps: " + NUMBER_OF_STEPS);writRes.newLine();
			writRes.append("Lifetime: " + LIFETIME);writRes.newLine();
			writRes.append("World size: " + WORLD_SIZE);writRes.newLine();
			writRes.append("Number of balloons: " + NUMBER_OF_BALLOONS);writRes.newLine();
			writRes.append("Range: " + RANGE);writRes.newLine();
			writRes.append("Communication radius: " + COMMUNICATION_RADIUS);writRes.newLine();
			writRes.append("Number of wind layers: " + stratosphere.size());writRes.newLine();
			writRes.append("Coverage over simulation: "+(accumulatedCoverage/NUMBER_OF_STEPS));writRes.newLine();
			writRes.append("Dropped connections: "+droppedConnections);

			writRes.newLine();
			writRes.newLine();
			writRes.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		printHeatMap();
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
		print();
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


	private float getCurrentCoverage(){
		int count = 0;
		for(int i = 0; i<WORLD_SIZE; i++){
			for(int j = 0; j<WORLD_SIZE; j++){
				if(coverage[i][j]){count++;}
			}
		}

		//The difference in currently connected are dropped connections
		if(count<currentlyConnected){
			droppedConnections += currentlyConnected-count;
		}

		//update currentlyConnected
		currentlyConnected = count;
		return ((float)((float)count/(float)TOTAL_CELLS));
	}

	public void step() throws IOException{
		currentStep++;

		if(balloons.size() < NUMBER_OF_BALLOONS){
			createBalloon();
		}
		try{
			for(Balloon b: balloons){
				b.age();
				switch(ALGORITHM){
				case "1":applyDecision1(b);
				break;
				case "2": applyDecision2(b);
				break;
				case "3": applyDecision3(b);
				break;
				case "3s": applyDecision3s(b);
				break;
				case "4": applyDecision4(b);
				break;
				case "4s": applyDecision4s(b);
				break;
				}
				moveBalloon(b);
			}
		}catch(Exception e){
			System.out.println("Error with balloons. Size: " + balloons.size());
		}

		//Now we safely remove all balloons that are too old
		for(Balloon b : balloons){
			if(b.getAge() > LIFETIME){
				removeBalloon(b);
				createBalloon();
				break;
			}
		}
		updateCoverage();
		updateStatistics();

		System.out.println("Step "+currentStep + " is complete.");
	}

	private boolean inCircle(Pair<Integer,Integer> center, Pair<Integer,Integer> point, int radius){
		boolean inCircle;

		/*The formula for circle is (x-h)^2 + (y -k)^2 = r^2. Where:
				x = x-coordinate
				y = y-coordinate
				a = x-coordinate of the center point
				b = y-coordinate of the center point
				r = radius
		 */
		int a = center.getFirst();
		int b = center.getSecond();
		int x = point.getFirst();
		int y = point.getSecond();
		int r = radius;

		return (((x-a)*(x-a))+((y-b)*(y-b))<=(r*r));
	}
	private void updateCoverage(){
		Pair<Integer,Integer> center = new Pair<Integer,Integer>(0,0);
		Pair<Integer,Integer> point = new Pair<Integer,Integer>(0,0);

		for(int a = 0; a < WORLD_SIZE; a++){
			for(int b = 0; b < WORLD_SIZE; b++){
				coverage[a][b] = false;
			}
		}

		for(Balloon b : balloons){

			center.setFirst(b.getX());
			center.setSecond(b.getY());

			for(int i = b.getX() - RANGE; i < b.getX() + RANGE; i++){
				for(int j = b.getY() - RANGE; j < b.getY() + RANGE; j++){
					point.setFirst(getAdjustedX(i));
					point.setSecond(getAdjustedY(j));
					coverage[getAdjustedX(i)][getAdjustedY(j)] = inCircle(center,point,RANGE); 

				}
			}
		}
	}
	public boolean[][] getCoverage(){
		return coverage;
	}
}

