package bo;

import structures.Pair;

public class Balloon {
	//Coordinates of the balloon
	int x;
	int y;
	//the layer that the balloon is in
	WindLayer windLayer;
	//the time it takes to travel between layers
	int delay;
	//when delay is not 0, then the balloon is on its way to the nextLayer
	WindLayer nextLayer;
	
	public Balloon(){
		x = 0;
		y = 0;
	}
	public Balloon(int _x, int _y, WindLayer wl){
		x = _x;
		y = _y;
		windLayer = wl;
		delay = 0;
		nextLayer = null;
	}
	
	public void moveWithWind(){
   		Pair<Integer, Integer> windVector = windLayer.getWind(x, y);
		int xChange = windVector.getFirst();
		int yChange = windVector.getSecond();
		
		setX(x+xChange);
		setY(y+yChange);
	}
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public WindLayer getWindLayer() {
		return windLayer;
	}
	public WindLayer getNextWindLayer(){
		return nextLayer;
	}
	public void setWindLayer(WindLayer newLayer) {
		windLayer = newLayer;
	}
	public int getDelay(){
		return delay;
	}
	public void setDelay(int d){
		delay = d;
	}

	@Override
	public String toString() {
		return "Balloon [x=" + x + ", y=" + y + ", windLayer=" + windLayer + ", delay=" + delay + ", nextLayer="
				+ nextLayer + "]";
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Balloon other = (Balloon) obj;
		if (windLayer == null) {
			if (other.windLayer != null)
				return false;
		} else if (!windLayer.equals(other.windLayer))
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	public void setNextLayer(WindLayer windLayer2) {
		nextLayer = windLayer2;
	}
	public void decreaseDelay() {
		delay--;
		
	}
	
}
