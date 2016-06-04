package structures;


import java.awt.List;
import java.util.ArrayList;


public class ObjectGrid<F> {

	private ArrayList<ArrayList<ArrayList<F>>> grid;
	private int size;

	public ObjectGrid(int SIZE) {
		size = SIZE;
		grid = new ArrayList<>();
		for(int i = 0; i < SIZE; i++){
			ArrayList<ArrayList<F>> al = new ArrayList<ArrayList<F>>();
			for(int j = 0; j < SIZE; j++){
				al.add(new ArrayList<F>());
			}
			grid.add(al);
		}

	}

	public void addObject(F newbie, int x, int y){
		grid.get(x).get(y).add(newbie);

	}

	public boolean removeObject(F rem, int x, int y){
		if(grid.get(x).get(y).contains(rem)){
			int r = grid.get(x).get(y).indexOf(rem);
			grid.get(x).get(y).remove(r);
			return true;
		}
		return false;
	}

	public F getObject(F comp, int x, int y){
		for(F item: grid.get(x).get(y)){
			if(item.equals(comp)){
				return item;}
		}
		return null;
	}
	public ArrayList<F> getObjects(int x, int y){
		return grid.get(x).get(y);
	}

	public boolean moveObject(F item, int oldX, int oldY, int newX, int newY){
		//Check if item exits
		if(grid.get(oldX).get(oldY).contains(item)){
			addObject(item, newX, newY);
			removeObject(item, oldX,oldY);
			return true;

		}
		return false;
	}
	public boolean duplicates(F item, int x, int y){
		int count = 0;
		for(F c : grid.get(x).get(y)){
			if(c.equals(item)){
				count++;
			}
		}
		if(count>1){
			return true;
		}
		return true;
	}


}