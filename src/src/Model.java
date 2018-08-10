import java.util.ArrayList;
import java.util.Random;
import java.io.PrintWriter;

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
	double yVel;
	boolean action;
	int x;
	int y;

	StateAction(Model m)
	{
		yVel = m.bird.vert_vel;
        x = m.bird.x;
        y = m.bird.y;
	}

    StateAction()
    {
        yVel = 0;
        x = 0;
        y = 0;
    }

/*	void print(PrintWriter pw) {
		pw.print(Integer.toString(xDelta) + ",");
		pw.print(Integer.toString(yDelta) + ",");
		pw.print(Integer.toString(yVel) + ",");
		pw.print((up ? "t" : "f") + ",");
		pw.print((action ? "t" : "f"));
	}*/

/*
	@Override
	public boolean equals(Object other) {
		StateAction o = (StateAction)other;
		if(xDelta != o.xDelta) return false;
		if(yDelta != o.yDelta) return false;
		if(yVel != o.yVel) return false;
		if(up != o.up) return false;
		if(action != o.action) return false;
		return true;
	}

	@Override
	public int hashCode() {
		return 1409 * xDelta + 1021 * yDelta + 1303 * yVel + (up ? 1907 : 0) + (action ? 1303 : 0);
	}

	void toVec(double[] v)
	{
		v[0] = ((double)xDelta) * 0.001;
		v[1] = ((double)yDelta) * 0.001;
		v[2] = ((double)yVel) * 0.01;
		v[3] = (up ? 0.0 : 1.0);
	}
*/

	void getStateAction(double[] v){
		v[0] = ((double) x) * 0.001;
		v[1] = ((double) y) * 0.001;
		v[2] = ((double) yVel) * 0.01;
		v[3] = (action ? 0.0 : 1.0);
	}
	void getState(double[] v){
		v[0] = ((double) x) * 0.001;
		v[1] = ((double) y) * 0.001;
		v[2] = ((double) yVel) * 0.01;
	}
	void Eucli(double[] distance) {
		distance[0] = Math.sqrt((x - 500)*(x - 500) + (y)*(y))/750.0;
		distance[1] = Math.sqrt((x - 500)*(x - 500) + (y - 500)*(y - 500))/750.0;
		if(false)
			System.out.format("the distance to two corner is (%f,%f)\n", distance[0], distance[1]);
	}

	void setState(int x, int y, double vel){
		this.x = x;
		this.y = y;
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
	Circle c;

	Model() {
		rand = new Random(0);
		bird = new Bird();
		tubes = new ArrayList<Tube>();
		frame = 38;
		c = new Circle();
	}

	void reset() {
		rand = new Random(0);
		bird = new Bird();
		tubes = new ArrayList<Tube>();
		frame = 38;
	}

	public boolean update() {
		if(!bird.update())
			return false;
		for(int i = 0; i < tubes.size(); i++) {
			Tube t = tubes.get(i);
			if(!t.update(bird))
				return false;
			if(t.x < -55) {
				tubes.set(i, tubes.get(tubes.size() - 1));
				tubes.remove(tubes.get(tubes.size() - 1));
				++score;
				//System.out.println(Integer.toString(score));
			}
		}
		if(++frame % 35 == 0) {
			boolean up = rand.nextBoolean();
			Tube t = new Tube(rand.nextInt(350) + (up ? 150 : 0), up);
			tubes.add(t);
		}
		return true;
	}

	Tube findNextTube(int xMin)
	{
		int x = 1000;
		Tube tu = null;
		for(int i = 0; i < tubes.size(); i++)
		{
			Tube t = tubes.get(i);
			if(t.x >= xMin && t.x < x)
			{
				x = t.x;
				tu = t;
			}
		}
		return tu;
	}

	public void flap() {
		bird.flap();
	}
}

class Circle{
	public int x;
	public int y;
	public int r;

	public void Circle(){
		x = 0;
		y = 0;
		r = 0;
	}
	public void Circle(int x, int y, double r) {
		this.x = x;
		this.y = y;
		this.r = (int)r;
	}
	public void setCircle(int x, int y, double r){
		this.x = x;
		this.y = y;
		this.r = (int)r;
	}


}







