import java.awt.event.MouseListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

class Controller implements MouseListener, KeyListener
{
	Model model;
	Random rand;
    NeuralNet nn;
	NeuralNet nno;
	
	double[] inputBuf;
	double[] outputBuf;
	
	double[] inputBufO;
	double[] outputBufO;
	
	
	int frame;
	boolean flap;
	double p_i; 
	Matrix P_i = new Matrix();
	Matrix P_j = new Matrix(); 
	Matrix K_j =new Matrix(); 
	Matrix Q_i = new Matrix();
	Matrix R_j = new Matrix(); 
	Matrix I  = new Matrix(); 
	StateAction estimate;  
	
	
	
	
	Controller(Model m) throws Exception {
		this.model = m;
		rand = new Random(1234);
		
		//for transition model
		nn = new NeuralNet(); 
		nn.layers.add(new Layer(4, 32));
		nn.layers.add(new Layer(32, 3));
		nn.init(rand);
	    inputBuf = new double[4];
		outputBuf = new double[3];
		
		//for oberservation model
		nno = new NeuralNet(); 
		nno.layers.add(new Layer(3, 32));
		nno.layers.add(new Layer(32, 2));
		nno.init(rand);
	    inputBufO = new double[3];
		outputBufO = new double[2];
		Q_i = Noise(3,0.01); //set Q_i
		R_j = Noise(2,0.01); //set R_j
		//initial P_i
		StateAction initial = new StateAction(model);
		p_i = initial.init; 
		P_i.setSize(3, 3);
		   for (int i = 0; i < P_i.rows(); i++) {
	           for (int j = 0; j < P_i.cols(); j++) {
	        	   if(i == j )
	        	   {
	        		   P_i.row(i)[j] = p_i;
	        	   }
	        	   else
	        	   {
	        	   P_i.row(i)[j] = 0;
	        	   }
	           }
	       }
		
	
		frame = 0;
		flap = false; 
		
		estimate = new StateAction(); 
		
	}
	void reset() {
		model.reset();
		frame = 0;
	}
	
	
	public void mousePressed(MouseEvent e) {
		this.model.flap();
	}

	public void keyPressed(KeyEvent e) {
		flap = true; 
		this.model.flap();
	}

	public void mouseReleased(MouseEvent e) {    }
	public void mouseEntered(MouseEvent e) {    }
	public void mouseExited(MouseEvent e) {    }

	public void mouseClicked(MouseEvent e) {    }
	public void keyTyped(KeyEvent e) {    }
	public void keyReleased(KeyEvent e) {    }
	
	public boolean update(double mu, double sigma, int burnin)
	{
	
	    StateAction prevStateAction = new StateAction(model, mu, sigma);
		prevStateAction.action = flap;
        
		
     	boolean alive = model.update();
		frame++;
		if(frame < burnin)
		{
			prevStateAction.toVecA(inputBuf);
			prevStateAction.toVec(inputBufO);
		}
		else
		{
			estimate.setAction(prevStateAction.action); 
			estimate.toVecA(inputBuf);
			estimate.toVec(inputBufO);
			
		}
		
		
		
		//obeservatioin
		//step6
		StateAction curStateAction = new StateAction(model, mu, sigma);
		
        curStateAction.toVecO(outputBufO);
        for(int i = 0; i < 10; i++)
           nno.refine(inputBufO, outputBufO, 0.1);
        double[] Observation = nno.forwardProp(inputBufO); 
		
		//transimition model
        //step 5
        curStateAction.toVec(outputBuf);
        for(int i = 0; i < 10; i++)
           nn.refine(inputBuf, outputBuf, 0.1);
        double[] prediction = nn.forwardProp(inputBuf); //x^i 
	  
        
	   
	    Matrix A_j = new Matrix(); 
	    A_j = nn.jacobian(inputBuf);
	    
	    A_j.copyBlock(nn.jacobian(inputBuf), 0, 0 , 3, 3);
	  
	    Matrix A_j_T = new Matrix(); 
	    A_j_T = A_j.transpose(); 
        P_j = addition(Multiply(Multiply(A_j, P_i),A_j_T), Q_i);
        
        //Kalman gain
        Matrix Right_part  = new Matrix(); 
        Right_part.setSize(2, 2);
        
        double noise = sigma * sigma;
        R_j = Noise(2,noise);
        
        Right_part = addition(Multiply(Multiply(nno.jacobian(inputBufO),P_j), nno.jacobian(inputBufO).transpose()),R_j).pseudoInverse(); 
	
		K_j = Multiply(Multiply(P_j,nno.jacobian(inputBufO).transpose()),Right_part);
		
        //test the accuracy 
        double[] real = new double[2];
        curStateAction.toVecO(real);
        double[] input = prediction;
        double[] pred = nno.forwardProp(input);
        double error = Math.sqrt((real[0] - pred[0])*(real[0] - pred[0]) + (real[1] - pred[1])*(real[1] - pred[1]));
        System.out.println(error);

		
		
	 
	   
		//update estimate the mesasurement Z_k; 
	   prediction = VecAddition(prediction, MatMutVec(K_j, VecSubtraction(Observation,outputBufO))); 
	   estimate.setState((int)(prediction[0]*1000),(int)(prediction[1]*1000), prediction[2]*100);
	   
	   
	   
	    // Update the error covariance 
	    I.setSize(3, 3);;
	    I.setToIdentity();
 	    P_j = Multiply(subtraction(I, Multiply(K_j,nno.jacobian(inputBufO))),P_j);
 	    
 	    
 	  
	     for(int i= 0; i<outputBufO.length; i++)
	     {
	    	 System.out.println(Observation[i] );
	    	 System.out.println(outputBufO[i]);
	     }
	     
 	     print_transition_error(Observation, outputBufO);
	    
		return alive;
	}
	
	public void print_transition_error(double[] prediction, double[] curState)
	{
	
	    double sse = 0.0;
	   
	    for(int i = 0; i < curState.length; i++)
	    {
	        double diff = curState[i] - prediction[i];
	        sse += diff * diff;
	    }
	    System.out.println(sse); 
	}
	
	
	
	public double[] VecSubtraction(double[] a, double[] b)
	{
		double[] c = new double[2]; 
		  for(int i = 0; i < a.length; i++)
		    {
		        c[i] = a[i] - b[i];
		       
		    }
		 return c;
    }
	
	public double[] VecAddition(double[] a, double[] b)
	{
		double[] c = new double[3]; 
		  for(int i = 0; i < a.length; i++)
		    {
		        c[i] = a[i] + b[i];
		       
		    }
		 return c;
    }
	
	public double[] MatMutVec(Matrix a, double[] b)
	{
  		double[] c = new double[3];  
		 for (int i = 0; i < a.rows(); i++) {
	           for (int j = 0; j < b.length; j++) {
	        	   
	        	    c[i] = c[i] + a.row(i)[j] * b[j] ;
	           }
	       }
		
		return c;
	}
	
	
	public Matrix addition(Matrix a, Matrix b)
	{
		if(a.rows() != b.rows())
			throw new IllegalArgumentException("rows are different sizes");
		if(a.cols() != b.cols())
			throw new IllegalArgumentException("cols are different sizes");
	     Matrix c =new Matrix();
	     c.setSize(a.rows(), b.cols());
	       for (int i = 0; i < a.rows(); i++) {
	           for (int j = 0; j < b.cols(); j++) {
	        	    c.row(i)[j] = a.row(i)[j] + b.row(i)[j];
           }
	       }
	       return c;
	}
		
	public Matrix subtraction(Matrix a, Matrix b)
	{
	     Matrix c = new Matrix() ;
	     c.setSize(a.rows(), b.cols());
	       for (int i = 0; i < a.rows(); i++) {
	           for (int j = 0; j < a.cols(); j++) {
	        	    c.row(i)[j] = a.row(i)[j] - b.row(i)[j];    
	           }
	       }
	       return c;
	}
	public Matrix Multiply(Matrix a, Matrix b)
	{
		Matrix c =new Matrix(); 
		c.setSize(a.rows(), b.cols());
		int rowsIna = a.rows(); 
		int columnsIna = a.cols();
		int columsInb = b.cols(); 
		
		 for (int i = 0; i < rowsIna; i++) {
			 for (int j = 0; j < columsInb; j++) {
				 for (int k = 0; k < columnsIna; k++) {
					   c.row(i)[j] += a.row(i)[k]*b.row(k)[j];
				 }
			 }
			 
		 } 
		 	return c; 
	}
	public Matrix Noise(int size, double n){
		Matrix m = new Matrix(size,size);
		m.setAll(0.0);
		for(int i=0;i<size;i++)
			m.row(i)[i] = n;

		return m;
	}
	
}
