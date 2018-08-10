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
    String i; 
   final double rate =0.1;
   final double discount = 0.99; //test 
	double exploreate = 0.01 ; 
	double reward; 
	boolean alive; 
	Random rand = new Random(); 
	HashMap<String, Double> qTable = new HashMap<String, Double>(); 
	int times; 
    double q_i_new;
	Controller(Model m) throws Exception {
		this.model = m;
		
	}

	
	public boolean update(){

	 boolean do_flap = false;
     double qf, qnf;
     String qfs, qnfs;

	 if(explore())
	 {
		 if(rand.nextInt(100) < 5)
			 do_flap = true;
	 }
	 else
	 {
		 //exploit 
		
		 Double Qflap = qTable.get(qtablekey(true)); 
		 double qflap = (Qflap == null ? 0.0 : Qflap.doubleValue());
		 qfs = qtablekey(true); 

         Double DNoflap = qTable.get(qtablekey(false));
         qnfs = qtablekey(false); 
 		 double qNoflap = (DNoflap == null ? 0.0 : DNoflap.doubleValue());
        
		 if(qflap > qNoflap)
			 do_flap = true;
		 if(qNoflap > qflap)
			 do_flap = false; 
		 if(qNoflap == qflap)
		 {
			 //if qNoflap == qflap
			 if(rand.nextDouble() < 0.05)
				 do_flap = true;    
			 else
				 do_flap = false; 
		 }
		 
		
             qf = qflap;
             qnf = qNoflap;
	 }
	
	 
	
	//current state i, get one action; 
	i = qtablekey(do_flap);
	Double Q_i = qTable.get(i);
	double q_i = (Q_i == null ? 0.0 : Q_i.doubleValue());
	
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

			reward = -1000.00; 
		}
		else 
		{
			reward = 1.00; 
		}
		// learn(update the q table)
        String jtrue =  qtablekey(true);
		Double Q_j_flap = qTable.get(jtrue); 
		
		
		double q_j_Flap = ( Q_j_flap == null ? 0.0 :  Q_j_flap.doubleValue());
	
		 String jfalse = qtablekey(false);
		Double Q_j_dont = qTable.get(jfalse);
		double q_j_Dont = ( Q_j_dont == null ? 0.0 :  Q_j_dont.doubleValue());
		
		q_i_new = (1-rate) * q_i + rate * (reward + discount * (Math.max(q_j_Flap,q_j_Dont)));
		
		
		
		qTable.put(i, q_i_new);	
		

		return alive;
		
	}

	public boolean explore(){
    
			
		if(rand.nextDouble() < exploreate)
			return true;
		else
			return false; 
	}
	
	public void flap(boolean do_flap)
	{
		if(do_flap)
			model.flap();
	}
	
	public String qtablekey(boolean action)
	{
		  Tube tube = model.getTube();
		  if(tube == null)
		  {
			  tube.x = 0; 
			  tube.y = 0;
		  }
		  
	       double differ_x =  model.bird.x - tube.x;
	 	   double differ_y =  model.bird.y - tube.y;
		
	 		String key = ""; 
			key += Double.toString(differ_x)+", "; 
			
			key +=  Double.toString(differ_y)+", ";
			key +=  Double.toString(model.bird.vert_vel)+", ";
			key +=  (tube.up? "1":"0")+", ";
			key+= action;
			return key;
	}

}




