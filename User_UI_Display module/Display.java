

public class Display {

	public int index;
	public int dir;
	public Display() {}
	public Display(int i,int j) {
		this.index = i;
		dir = j;
		
	}
	String dirToString() {
		if(dir==0) return "up";
		else if(dir==1) return "left";
		else if(dir==2) return "down";
		else return "right";
	}
	
	void printRight() {
		System.out.print(this.index+1 + this.dirToString()+" ¡æ");
	}
	void printLeft() {
		System.out.print(this.index+1+ this.dirToString() + " ¡ç");
	}
	void printStraight() {
		System.out.print(this.index+1+ this.dirToString() + " ¡è");
	}
	void printDown() {
		System.out.print(this.index+1 + this.dirToString()+ " ¡é");
	}
	

}
