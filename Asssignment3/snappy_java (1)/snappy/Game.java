import javax.swing.JFrame;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.Timer;

import java.io.IOException;
import java.awt.event.WindowEvent;
import java.awt.Robot;

public class Game extends JFrame implements ActionListener {
	 double sigma;
	    double mu;
	    int burnin;
	Model model;
	View view;
	Timer timer;
	int ttl;
	int frame;
	int gameNumber;
	Controller controller;

	public Game(double sigma, double mu,int burnin) throws IOException, Exception {
		this.sigma = sigma;
	    this.mu = mu;
	    this.burnin = burnin;
		this.model = new Model();
		controller = new Controller(this.model);
		this.view = new View(this.model);
		addMouseListener(controller);
		addKeyListener(controller);
		this.setTitle("Snappy Bird");
		this.setSize(500, 500);
		this.getContentPane().add(view);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		timer = new Timer(30, this);
		timer.start(); // Indirectly calls actionPerformed at regular intervals
	}

	public void actionPerformed(ActionEvent evt) {
		if(controller == null)
			System.out.println("uh oh");
		
		if(!this.controller.update(this.mu, this.sigma, this.burnin)) {
			controller.reset();
			if(gameNumber == 0)
			{
				timer.stop();
				try {
					//train();
				} catch(Exception e) {
					e.printStackTrace();
				}
				timer.start();
			}
			//controller.learn = false;
			System.out.println("---------------------------");
			gameNumber++;
		}
		//robot.mouseMove(470 + (int)(20 * Math.cos(frame)), 70 + (int)(20 * Math.sin(frame++)));
		view.invalidate();
		repaint(); // Indirectly calls View.paintComponent
	}

	public static void main(String[] args) throws IOException, Exception {
	        if (args.length >= 3){
	            double sigma; 
	            double mu;
	            int burnin;
	            sigma = Double.parseDouble(args[1]);
	            mu = Double.parseDouble(args[0]);
	            burnin = Integer.parseInt(args[2]);
	            new Game(mu, sigma,burnin); 
	        } else
	            System.out.print("wrong added ");
	    }
		
}
