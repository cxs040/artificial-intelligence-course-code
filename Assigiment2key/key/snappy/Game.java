import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import java.io.IOException;
import java.awt.event.WindowEvent;
import java.awt.Robot;
import java.io.File;

public class Game extends JFrame implements ActionListener {
	Model model;
	Controller controller;
	View view;
	Timer timer;
	int ttl;
//	Robot robot;
	int frame;
	int gameNumber;

	public Game() throws IOException, Exception {
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
//		this.robot = new Robot();
		timer = new Timer(30, this);
		timer.start(); // Indirectly calls actionPerformed at regular intervals
	}

/*	void train() throws Exception {
		for(int i = 0; i < 100; i++) {
			System.out.println(Integer.toString(i) + "%");
			for(int j = 0; j < 100000; j++) {
				if(!this.controller.update())
					controller.reset();
			}
		}*/
	//}

	public void actionPerformed(ActionEvent evt) {
		if(!this.controller.update()) {
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
			controller.exploreRate = 0.0;
			//controller.learn = false;
			System.out.println("---------------------------");
			gameNumber++;
		}
		frame++;
//		robot.mouseMove(470 + (int)(20 * Math.cos(frame)), 70 + (int)(20 * Math.sin(frame++)));
		view.invalidate();
		repaint(); // Indirectly calls View.paintComponent
	}

	public static void main(String[] args) throws IOException, Exception {
		/*double noise = Double.fromSring(args[0]);
		System.out.println(Double.toString(noise));*/
		new Game();
	}
}
