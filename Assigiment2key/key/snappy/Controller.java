import java.awt.event.MouseListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;
import java.util.Set;
import java.io.PrintWriter;
import java.util.Iterator;

class Controller implements MouseListener, KeyListener
{
	Model model;
	Random rand;
	NeuralNet nn;
	
	NeuralNet transitionModel; 
	double[] inputBuf;
	double[] outputBuf;
	ArrayList<StateAction> queue;
	double discount;
	double exploreRate;
	boolean please_choose_to_flap;
	int frame;
	int farthest_frame;

	Controller(Model m) throws Exception {
		this.model = m;
		rand = new Random(1234);
		discount = 0.99;
		exploreRate = 0.2;
		nn = new NeuralNet();
		nn.layers.add(new Layer(4, 32));
		nn.layers.add(new Layer(32, 2));
		nn.init(rand);
		inputBuf = new double[4];
		outputBuf = new double[2];
		queue = new ArrayList<StateAction>();
		frame = 0;
	}

	void reset() {
		please_choose_to_flap = false;
		model.reset();
		frame = 0;
		queue.clear();
	}

	public void mousePressed(MouseEvent e) {
		//this.model.flap();
		please_choose_to_flap = true;
	}

	public void keyPressed(KeyEvent e) {
		//this.model.flap();
		please_choose_to_flap = true;
	}

	public void mouseReleased(MouseEvent e) {    }
	public void mouseEntered(MouseEvent e) {    }
	public void mouseExited(MouseEvent e) {    }

	public void mouseClicked(MouseEvent e) {    }
	public void keyTyped(KeyEvent e) {    }
	public void keyReleased(KeyEvent e) {    }

	double[] getQvalues(StateAction j)
	{
		j.toVec(inputBuf);
		return nn.forwardProp(inputBuf);
	}

	boolean update()
	{
		// Decide what action to do
		StateAction prevStateAction = new StateAction(model);
		boolean flap = false;
		if(rand.nextDouble() < exploreRate) // explore or exploit?
		{
			// Explore
			if(rand.nextDouble() < 0.05)
			{
				flap = true;
			}
		}
		else
		{
			// Exploit
			double[] qVals = getQvalues(prevStateAction);
			double qFlap = qVals[1];
			double qNoflap = qVals[0];
			if(qFlap > qNoflap) // is flapping better?
				flap = true;
			else if(qNoflap > qFlap)
				flap = false;
			else if(rand.nextDouble() < 0.05)
				flap = true;
			else
				flap = false;
			if(exploreRate == 0.0)
				System.out.println(Double.toString(Math.max(qFlap, qNoflap)));
		}
		if(please_choose_to_flap)
		{
			please_choose_to_flap = false;
			flap = true;
		}

		// Do the action
		prevStateAction.action = flap;
		if(flap)
			model.flap();
		boolean alive = model.update();
		frame++;
		farthest_frame = Math.max(farthest_frame, frame);
		StateAction curStateAction = new StateAction(model);

		// Get a reward for the action we just performed
		double reward = 0.00001;
		if(!alive)
			reward = -0.01;

		// Learn
		double[] qVals = getQvalues(prevStateAction);
		double qOld = qVals[prevStateAction.action ? 1 : 0];
		outputBuf[0] = qVals[0];
		outputBuf[1] = qVals[1];
		qVals = getQvalues(curStateAction);
		double qBest = Math.max(qVals[1], qVals[0]);
		double qNew = reward + discount * qBest;
		outputBuf[prevStateAction.action ? 1 : 0] = qNew;
		prevStateAction.toVec(inputBuf);
		for(int i = 0; i < 10; i++)
			nn.refine(inputBuf, outputBuf, 0.1);
		
		
		print_transition_error(prevStateAction,prevStateAction.action ? 1 : 0, curStateAction );
		return alive;
	}
	
	public void print_transition_error(double[] prevState, double[] actions, double[] curState)
	{
	    double[] input = concatenate(prevState, actions);
	    double[] prediction = transitionModel.forwardProp(input);
	    double sse = 0.0;
	    for(int i = 0; i < curState.length; i++)
	    {
	        double diff = curState[i] - prediction[i];
	        sse += diff * diff;
	    }
	    System.out.print(sse);
	}
	
	public double[] concatenate(double[] a, double[] b)
	{
		 int aLen = a.length;
		   int bLen = b.length;
		   double[] c= new double[aLen+bLen];
		   System.arraycopy(a, 0, c, 0, aLen);
		   System.arraycopy(b, 0, c, aLen, bLen);
		return c;
	}
}
