import java.awt.event.MouseListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;

class Controller //implements MouseListener, KeyListener

{
	Model model;
	int frame ;
    double[] i; 
   final double rate =0.1;
   final double discount = 0.99; //test 
	double exploreate = 0.01 ; 
	double reward; 
	boolean alive; 
	Random rand = new Random(); 
    NeuralNet nn = new NeuralNet();
    
	int times; 
    double q_i_new;
	Controller(Model m) throws Exception {
		this.model = m;
		
	}

	
	public boolean update(){

	  double do_flap = 0;
     double qf, qnf;

	 if(explore())
	 {
		 if(rand.nextInt(100) < 5)
			 do_flap = 1;
	 }
	 else
	 {
		 //exploit 
		// flap = 1, no flap = 0;
	     Double Qflap =measureStateValue1( nn,  qtablekey(1));
		 double qflap = (Qflap == null? 0.0 : Qflap.doubleValue());
         
         Double QNoflap = measureStateValue1( nn,  qtablekey(0));
 		 double qNoflap = (QNoflap == null? 0.0 : Qflap.doubleValue());
        
		 if(qflap > qNoflap)
			 do_flap = 1;
		 if(qNoflap > qflap)
			 do_flap = 0; 
		 if(qNoflap == qflap)
		 {
			 //if qNoflap == qflap
			 if(rand.nextDouble() < 0.05)
				 do_flap = 1;    
			 else
				 do_flap = 0; 
		 }
		 
		
	 }
	
	 
	
	//current state i, get one action; 
	i = qtablekey(do_flap);
	Double Q_i =  measureStateValue1( nn,  i);
	double q_i = (Q_i == null? 0.0 : Q_i.doubleValue());
	
	//choose flap or not
	flap(do_flap); 
	alive = model.update();
	frame++;

	
	if(alive == false)
	{
		  frame = 0;
		
	}
	
	
	
	
	//future state j, two actions 

	if(alive == false)
		{

			reward = -0.1; 
		}
		else 
		{
			reward = 0.001; 
		}
		// learn(update the q table)

		
		updateStateValue(nn,i,qtablekey(1),qtablekey(0));
		
		return alive;
		
	}

	public boolean explore(){
    
			
		if(rand.nextDouble() < exploreate)
			return true;
		else
			return false; 
	}
	
	public void flap(double do_flap)
	{
		if(do_flap == 1)
			model.flap();
	}
	

	public double[] qtablekey(double action)
	{
		  Tube tube = model.getTube();
		  if(tube == null)
		  {
			  tube.x = 0; 
			  tube.y = 0;
		  }
		  
	       double differ_x =  model.bird.x - tube.x;
	 	   double differ_y =  model.bird.y - tube.y;
		
	 		double[] key = {differ_x/1000,differ_y/1000,model.bird.vert_vel/1000, (tube.up? 1:0),action };
			return key;
	}
	
	
	//action as output
	double measureStateValue1(NeuralNet nn, double[] i1)
	{
	   double[] in = i1;
	   double[] out = nn.forwardProp(in);
	   double m = Double.NEGATIVE_INFINITY;
	   for(int i = 0; i < out.length; i++)
	      m = Math.max(m, out[i]);
	   return m;
	}
	
	
	
	//action as input
	public double measureStateValue2(NeuralNet nn, double[] j1, double[] j2)
	{
	   double m = Double.NEGATIVE_INFINITY;
	   	  
	      double[] out = nn.forwardProp(j1);
	      double[] out2 = nn.forwardProp(j2);
	      m = Math.max(out[0], out2[0]);
	   
	   return m;
	}
	
	
	public void updateStateValue(NeuralNet nn, double[] i, double[] j1, double[] j2)
	{  
		
		
	   double[] in = i;
	   double[] target = new double[1];
	   Double Max = measureStateValue2(nn, j1, j2);
	   double max = (Max == null? 0.0 : Max.doubleValue());
	   target[0] = reward + discount *max ;
	   nn.refine(i, target, 0.02);//do i need learning rate? 
	}

}




