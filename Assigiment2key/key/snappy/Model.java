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
	int xDelta;
	int yDelta;
	int yVel;
	boolean up;
	boolean action;
    int mTop; 
    int mBot;
	StateAction(Model m)
	{
		yVel = ((int)m.bird.vert_vel);
		Tube t = m.findNextTube(55); // get next tube with x position >= 55
	/*	if(t == null)
		{
			xDelta = 500;
			yDelta = 300 - m.bird.y;
			up = true;
		}
		else
		{
			xDelta = t.x;
			yDelta = t.y - m.bird.y;
			up = t.up;
		}*/
		
		xDelta = 500 - m.bird.x; 
		yDelta = m.bird.y; 
		mTop = (int) Math.sqrt(Math.pow(500 - m.bird.x, 2)+Math.pow(m.bird.y, 2));
		mBot = (int) Math.sqrt(Math.pow(500 - m.bird.x, 2)+Math.pow(500 - m.bird.y, 2));
		
		//yDelta = Math.max(-50, Math.min(50, yDelta));
	}

	void print(PrintWriter pw) {
		pw.print(Integer.toString(xDelta) + ",");
		pw.print(Integer.toString(yDelta) + ",");
		pw.print(Integer.toString(yVel) + ",");
		//pw.print((up ? "t" : "f") + ",");
		pw.print((action ? "t" : "f"));
	}

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
		tubes = new ArrayList<Tube>();
		frame = 38;
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
				//System.out.println(Integer.toString(++score));
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








