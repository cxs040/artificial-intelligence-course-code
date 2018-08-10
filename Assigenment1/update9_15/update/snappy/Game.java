import javax.swing.JFrame;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

import javax.swing.Timer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.event.WindowEvent;
import java.awt.Robot;

public class Game extends JFrame implements ActionListener {
	Model model;
	View view;
	Timer timer;
	int ttl;
	
	int times1; 
	Robot robot;
	int frame;
    Controller controller;
    
	public Game() throws IOException, Exception {
		
		
		this.model = new Model();
		
		controller = new Controller(this.model);
		
		
		this.view = new View(this.model);
		this.setTitle("Snappy Bird");
		this.setSize(500, 500);
		this.getContentPane().add(view);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.robot = new Robot();
	    train();
		
		timer = new Timer(30, this);
		timer.start(); // Indirectly calls actionPerformed at regular intervals
	}

	
	
	public void train()
	{ 
		
		for(int i = 0; i < 5000000; i++)
		{
			controller.update(); 
	  	    
		
			if(!controller.update())
			{
				
				model.reset(); 
			
			}
	  		
		}
	   
	}
	
	public void actionPerformed(ActionEvent evt) {
		//System.out.println(ttl);
		
		if(ttl > 0)
		{
			if(--ttl == 0)
			{   
				timer.stop();
				//dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
				model.reset();
				
				timer.start();
				 
			}
			return;
		}
	
		
		if(!controller.update())
			ttl = 1;
		
		view.invalidate();
		repaint(); // Indirectly calls View.paintComponent
	}

	public static void main(String[] args) throws IOException, Exception {
		
		
		new Game();
	}
	
	
	
	
	
	
}
