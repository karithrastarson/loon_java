package bo;

import structures.Pair;

public class Balloon {
	//Coordinates of the balloon
	int x;
	int y;
	int altitude;
	
	boolean isMovingUp = false;
	boolean isMovingDown = false;
	
	
	//the layer that the balloon is in
	WindLayer windLayer;

	
	public Balloon(){
		x = 0;
		y = 0;
	}
	public Balloon(int _x, int _y, WindLayer wl){
		x = _x;
		y = _y;
		windLayer = wl;
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

	public void setWindLayer(WindLayer newLayer) {
		windLayer = newLayer;
	}


	public int getAltitude() {
		return altitude;
	}
	public void setAltitude(int altitude) {
		this.altitude = altitude;
	}
	public boolean isMovingUp() {
		return isMovingUp;
	}
	public void goUp() {
		this.isMovingUp = true;
		this.isMovingDown = false;
	}
	public boolean isMovingDown() {
		return isMovingDown;
	}
	public void goDown() {
		this.isMovingDown = true;
		this.isMovingUp = false;
	}
	public void stopVertical(){
		this.isMovingDown = false;
		this.isMovingUp = false;
	}
	
	public void updateAltitude(int alt){
		if(this.isMovingDown){
			this.altitude-=alt;
		}
		else if(this.isMovingUp){
			this.altitude+=alt;
		}
	}
	@Override
	public String toString() {
		return "Balloon [x=" + x + ", y=" + y + ", altitude=" + altitude + ", isMovingUp=" + isMovingUp
				+ ", isMovingDown=" + isMovingDown + ", windLayer=" + windLayer + "]";
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


	
}
