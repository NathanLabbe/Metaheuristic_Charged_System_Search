import java.awt.Canvas;
import java.awt.Graphics;
import java.lang.management.ManagementFactory;
import javax.swing.*;

public class Main {

    public enum objective_function{
        MCCORMICK,
        BUKIN,
        BOOTH,
        SQUARE;
    }

    public static void main(String[] args) throws InterruptedException {

        //Number of Particles and Iterations
        int nbCP = 1000;
        int nbIt = 700;
        int xSize = 600;
        int ySize = 600;
        objective_function fun = objective_function.BUKIN;
        boolean draw = true;

        //Study boundaries
        Vecteur v1 = new Vecteur(0,0);
        Vecteur v2 = new Vecteur(xSize,ySize);

        /**Parallel CSS*/
        ParallelCSS pCSS = new ParallelCSS(nbCP,v1,v2,fun);
        long start = System.currentTimeMillis();
        pCSS.run(nbIt);
        long stop = System.currentTimeMillis();
        System.out.println("Parallel Execution in " + ((stop-start)/1000) + " sec ");

        /**Sequential CSS*/
        SequentialCSS sCSS = new SequentialCSS(nbCP,v1,v2,fun);
        double oldtime = (stop-start);
        start = System.currentTimeMillis();
        sCSS.run(nbIt);
        stop = System.currentTimeMillis();
        System.out.println("Sequential Execution in " + ((stop-start)/1000) + " sec " );
        System.out.println("Diff " + (((stop-start)/oldtime)*100) +" % of parallel time ");

        if(draw) {
            /**Display Parallel CSS Result*/
            JFrame frame = new JFrame("Charged System Search algorithm in Parallel Mode");
            pCSS.setSize(xSize, ySize);
            frame.add(pCSS);
            frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


            /**Display Sequential CSS Result*/
            JFrame frame2 = new JFrame("Charged System Search algorithm Sequential Mode");
            sCSS.setSize(xSize, ySize);
            frame2.add(sCSS);
            frame2.pack();
            frame2.setVisible(true);
            frame2.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame2.setLocation(xSize, 0);
        }
    }
}

