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
    double sigma;
    double mu;
    int burnin;

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
//		this.robot = new Robot();
        timer = new Timer(30, this);
        timer.start(); // Indirectly calls actionPerformed at regular intervals
    }


    public void actionPerformed(ActionEvent evt) {
        if (!this.controller.update(this.mu, this.sigma, this.burnin)) {
            timer.stop();
         }
        view.invalidate();
        repaint(); // Indirectly calls View.paintComponent
    }

    public static void main(String[] args) throws IOException, Exception {
        System.out.println(args);
        if (args.length >= 3){
            double sigma,mu;
            int burnin;
            sigma = Double.parseDouble(args[1]);
            mu = Double.parseDouble(args[0]);
            burnin = Integer.parseInt(args[2]);
            new Game(mu, sigma,burnin);
        } else
            System.out.print("args error. Please add mu sigma and burnin time ");
    }
}
