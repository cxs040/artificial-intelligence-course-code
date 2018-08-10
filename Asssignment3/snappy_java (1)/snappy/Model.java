import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

class Bird
{
	int x;
	int y;
	double vert_vel;
	int time_since_flap;

	Bird() {
		x = 100;
		y = 100;
	}

	boolean update() {
		time_since_flap++;
		y += vert_vel;
		vert_vel += 1.5;
		if(y < -55)
			return false;
		else if(y > 500)
			return false;
		return true;
	}

	void flap() {
		vert_vel = -12.0;
		if(time_since_flap > 4)
			time_since_flap = 0;
		else
			time_since_flap = 5;
	}
}


class Tube
{
	int x;
	int y;
	boolean up;

	Tube(int _y, boolean _up) {
		y = _y;
		x = 500;
		up = _up;
	}

	boolean update(Bird bird) {
		x -= 5;
		if(x < bird.x + 60 && x + 45 > bird.x) {
			if(up) {
				if(bird.y + 50 > y)
					return false;
			} else {
				if(bird.y + 20 < y)
					return false;
			}
		}
		return true;
	}
}

class StateAction
{
	int xDelta;
	int yDelta;
	double yVel;
	boolean up;
	boolean action;
    double mTop; 
    double mBot;
    double init; 
    Random rand = new Random(1234);
    StateAction(Model m)
	{
		yVel = (m.bird.vert_vel);
		
		xDelta = 500 - m.bird.x; 
		yDelta = m.bird.y; 
	
		//yDelta = Math.max(-50, Math.min(50, yDelta));
	}
    
	StateAction(Model m, double sigma, double mu)
	{
		yVel = (m.bird.vert_vel);
		
		xDelta = 500 - m.bird.x; 
		yDelta = m.bird.y; 
		mTop =  Math.sqrt(Math.pow(500 - m.bird.x, 2)+Math.pow(m.bird.y, 2)) + rand.nextGaussian() * sigma + mu;
		mBot =  Math.sqrt(Math.pow(500 - m.bird.x, 2)+Math.pow(500 - m.bird.y, 2)) + rand.nextGaussian() * sigma + mu;
		
		//yDelta = Math.max(-50, Math.min(50, yDelta));
	}
	
    
	public StateAction() {
		  yVel = 0;
	      xDelta = 0;
	      yDelta = 0;
	}

	void print(PrintWriter pw) {
		pw.print(Integer.toString(xDelta) + ",");
		pw.print(Integer.toString(yDelta) + ",");
		pw.print(yVel + ",");
		//pw.print((up ? "t" : "f") + ",");
		pw.print((action ? "t" : "f"));
	}

	@Override
	public boolean equals(Object other) {
		StateAction o = (StateAction)other;
		if(xDelta != o.xDelta) return false;
		if(yDelta != o.yDelta) return false;
		if(yVel != o.yVel) return false;
		//if(up != o.up) return false;
		if(action != o.action) return false;
		return true;
	}

	@Override
	public int hashCode() {
		return (int) (1409 * xDelta + 1021 * yDelta + 1303 * yVel  + (action ? 1303 : 0));
	}

	void toVec(double[] v)
	{
		v[0] = ((double)xDelta) * 0.001;
		v[1] = ((double)yDelta) * 0.001;
		v[2] = ((double)yVel) * 0.01;
	}
	void toVecA(double[] v)
	{
		v[0] = ((double)xDelta) * 0.001;
		v[1] = ((double)yDelta) * 0.001;
		v[2] = ((double)yVel) * 0.01;
		v[3] = action ? 0.1: 0.0;
	}
	
	void toVecO(double[] v)
	{
		v[0] = ((double)mTop) * 0.001;
		v[1] = ((double)mBot) * 0.001;
	}
	
	void initial()
	{
		
		 init = (double) Math.sqrt(Math.pow(((double)xDelta) * 0.001, 2)+Math.pow(((double)yDelta) * 0.001, 2));
		
	}
	
	void setState(int x, int y, double vel){
		this.xDelta = x;
		this.yDelta = y;
		this.yVel = vel;

	}
	void setAction(boolean action){
		this.action = action;
	}
}




class Model
{
	Bird bird;
	ArrayList<Tube> tubes;
	Random rand;
	int frame;
	int score;

	Model() {
		rand = new Random(0);
		bird = new Bird();
		//tubes = new ArrayList<Tube>();
		frame = 38;
	}
	void reset() {
		rand = new Random(0);
		bird = new Bird();
		//tubes = new ArrayList<Tube>();
		frame = 38;
	}
	public boolean update() {
		if(!bird.update())
			return false;
		/*for(int i = 0; i < tubes.size(); i++) {
			Tube t = tubes.get(i);
			if(!t.update(bird))
				return false;
			if(t.x < -55) {
				tubes.set(i, tubes.get(tubes.size() - 1));
				tubes.remove(tubes.get(tubes.size() - 1));
				System.out.println(Integer.toString(++score));
			}
		}
		if(++frame % 35 == 0) {
			boolean up = rand.nextBoolean();
			Tube t = new Tube(rand.nextInt(350) + (up ? 150 : 0), up);
			tubes.add(t);
		}*/
		return true;
	}

	public void flap() {
		bird.flap();
	}
}








