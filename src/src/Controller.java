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


class Controller implements MouseListener, KeyListener {
    Model model;
    Random rand;
    NeuralNet observation;
    boolean please_choose_to_flap;
    NeuralNet transit;
    Matrix A, P, Q, K, H, R;
    StateAction prevStateAction, curStateAction;
    StateAction Ex;
    double r;


    Controller(Model m) throws Exception {
        this.model = m;
        //initiate transition
        transit = new NeuralNet();
        transit.layers.add(new Layer(4, 64));
        transit.layers.add(new Layer(64, 3));
        rand = new Random(1024);
        transit.init(rand);
        // initate observation
        rand = new Random(1234);
        observation = new NeuralNet();
        observation.layers.add(new Layer(3, 32));
        observation.layers.add(new Layer(32, 2));
        observation.init(rand);

        A = new Matrix(3,3);
        P = new Matrix(3,3);
        Q = Matrix.Noise(3, 0.01);
        K = new Matrix(3,2);
        H = new Matrix(2,2);
        R = Matrix.Noise(2, 0.01);
        prevStateAction = new StateAction();
        //P.setAll();

        Ex = new StateAction();
        P.setToIdentity(20000);


    }


    public void mousePressed(MouseEvent e) {
        //this.model.flap();
        please_choose_to_flap = true;
    }

    public void keyPressed(KeyEvent e) {
        //this.model.flap();
        please_choose_to_flap = true;
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }




    double[] Tran(double[] input1, double[] output1){
        // train transition model
        for (int i = 0; i < 10; i++)
            transit.refine(input1, output1, 0.1);
        //measure sum-squared prediction error
        double sse = 0.0;
        double[] predTran = transit.forwardProp(input1);

        for (int i = 0; i < predTran.length; i++) {
            sse += Math.pow((output1[i] - predTran[i]),2);

        }
        r = sse;

        return predTran;
    }

    double[] Obsv(double[] input2, double[] output2){
        /*   Observation transaction   */
          for(int i = 0; i < 10; i++)
            observation.refine(input2,output2,0.1);

        //measure sum-squared prediction error
        double sse = 0.0;
        double[] predObsv = observation.forwardProp(input2);
        for (int i = 0; i < predObsv.length; i++) {
            sse += Math.pow((output2[i] - predObsv[i]),2);
        }
        return predObsv;
    }

    boolean update(double mu, double sigma, int burnin) {
        boolean BurnIn = false;
        if(model.score>burnin) {
            BurnIn = true;
            //System.out.println("Burnin Start");
        }


        prevStateAction = new StateAction(model);
          //control by fingers
        prevStateAction.action = please_choose_to_flap;
        please_choose_to_flap = false;

        // Do the action
        if (prevStateAction.action)
           model.flap();
        boolean alive = model.update();
        curStateAction = new StateAction(model);


        double[] input1 = new double[4];
        double[] input2 = new double[3];
        if(!BurnIn) {

            prevStateAction.getStateAction(input1);

            prevStateAction.getState(input2);
        }
        else {
            Ex.setAction(prevStateAction.action);
            Ex.getStateAction(input1);
            Ex.getState(input2);
        }


        double[] output1 = new double[3];
        curStateAction.getState(output1);
        double[] predTran = Tran(input1, output1);


        double[] output2 = new double[2];
        curStateAction.Eucli(output2);
        double[] predObsv = Obsv(input2, output2);

        


        // preoject the error covariance ahead
        A = transit.jacobian(input1);
        A.copyBlock(transit.jacobian(input1),0,0,3,3);
        P = Matrix.addition(Matrix.multiply(
                Matrix.multiply(A,P,false,false)
                ,A,false,true
        ),Q);


        // compute the Kalman gain
        H = observation.jacobian(input2);
        Matrix K1 = Matrix.multiply(P,H,false,true);
        Matrix Km = Matrix.multiply(Matrix.multiply(H, P, false, false), H, false, true);
        double noise = mu + sigma*rand.nextGaussian();
        R = Matrix.Noise(2,noise );
        Matrix K2 = Matrix.addition(Km,R );
         Matrix K3 = K2.pseudoInverse();
        K3.copyBlock(K2.pseudoInverse(),0,0,2,2);
        K = Matrix.multiply(K1,K3,false,false);
        // update estimate with measrement
        double[] z = new double[2];
        curStateAction.Eucli(z);

        double[] truez = new double[2];
        curStateAction.Eucli(truez);
        double[] input3 = predTran;
        double[] predz = observation.forwardProp(input3);
        Matrix Z = new Matrix(2,1);
        Z.row(0)[0] =  truez[0] - predz[0];
        Z.row(1)[0] =  truez[1] - predz[1];

        double osse = Math.sqrt((truez[0] - predz[0])*(truez[0] - predz[0]) + (truez[1] - predz[1])*(truez[1] - predz[1]));
        System.out.println(osse);


        Matrix KZ = Matrix.multiply(K,Z,false,false);
        double x = predTran[0] + KZ.row(0)[0];
        double y = predTran[1] + KZ.row(1)[0];
        double vel = predTran[2]+KZ.row(2)[0];
        Ex.setState((int)(x*1000),(int)(y*1000),vel*100);
        // draw a circle

        model.c.setCircle((int)(x*1000),(int)(y*1000),noise);
        // update the error covariance
        Matrix I = new Matrix(3,3);
        I.setToIdentity(1.0);
        P = Matrix.multiply(
                Matrix.subtraction(I,Matrix.multiply(K,H,false,false))
                ,P,false,false);

        return alive;
    }

    Matrix getPart(Matrix mx,int m, int n){
        Matrix newm = new Matrix(m,n);
        for (int i = 0; i < m; i++)
        {
            for(int j = 0; j < n; j++)
                newm.row(i)[j] = mx.row(i)[j];
        }
        return mx;
    }
}
