import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;
import java.awt.Canvas;
import java.awt.Graphics;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import javax.xml.transform.stream.StreamSource;

import static java.lang.Math.*;

public class ParallelCSS extends Canvas{
    private ArrayList<ChargedParticule> CPs;
    private ArrayList<ChargedParticule> cm;
    private Vecteur v1;
    private Vecteur v2;
    private double epsilon = 0.00001;
    private double a;
    private double ka;
    private double kv;
    private Main.objective_function fun;

    private int nbThreads;
    private int nLoc;
    private Semaphore[] sem;
    private Thread[] threads;



    public ParallelCSS(int nbCP, Vecteur v1, Vecteur v2, Main.objective_function f ) {
        fun = f;
        a=v1.minus(v2).norm()/10;
        nbThreads = ManagementFactory.getThreadMXBean().getThreadCount()-1;
        nLoc = nbCP/(nbThreads+1);
        sem = new Semaphore[nbCP];
        for(int i = 0; i < nbCP; i++){
            sem[i] = new Semaphore(1);
        }
        threads = new Thread[nbThreads];

        Vecteur position = new Vecteur(0,0);
        double tmp = 0;
        CPs = new ArrayList<>();
        for(int i =0; i<nbCP; i++){
            ChargedParticule c = new ChargedParticule(position,position,position,tmp,tmp);
            CPs.add(c);
        }
        this.v1=v1;
        this.v2=v2;
        cm=new ArrayList<>();

    }
    /**
     * Method to draw the finale result of Parallel CSS
     * @param g
     */
    public void paint(Graphics g) {
        g.setColor(Color.white);
        g.fillRect(0,0, this.getWidth(), this.getHeight());
        g.setColor(Color.black);
        for(int i =0; i<CPs.size();i++) {
            g.fillOval((int)CPs.get(i).getPosition().getX(), (int)CPs.get(i).getPosition().getY(), 10, 10);
        }
    }

    /**
     *  Run the Parallel version of CSS Algorithm
     * @param maxIterations
     */
    public void run(int maxIterations) throws InterruptedException {
        initialisation();
        ranking();
        createCm();
        int t=0;

        while(t<maxIterations){
            ka=0.5*(1 + t/maxIterations);
            kv=0.5*(1 - t/maxIterations);

            for(int th = 0; th < nbThreads; th++) {
                int finalT = th;
                threads[th] = new Thread(){
                    public void run(){
                        for(int i = finalT * nLoc; i < (finalT +1)*nLoc; i++){
                            CPs.get(i).setOldPosition(CPs.get(i).getPosition());
                        }
                    }
                };
            }

            for(int i = 0; i < nbThreads; i++){
                threads[i].start();
            }

            for(int i = nbThreads * nLoc; i < CPs.size(); i++){
                CPs.get(i).setOldPosition(CPs.get(i).getPosition());
            }

            for(int i = 0; i < nbThreads; i++){
                threads[i].join();
            }


            for(int th = 0; th < nbThreads; th++) {
                int finalT = th;
                threads[th] = new Thread(){
                    public void run(){
                        for(int i = finalT * nLoc; i < (finalT +1)*nLoc; i++){
                            try {
                                updatePositionAndVelocity(i);
                                correctPosition(i);

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
            }

            for(int i = 0; i < nbThreads; i++){
                threads[i].start();
            }

            for(int i = nbThreads * nLoc; i < CPs.size(); i++){
                updatePositionAndVelocity(i);
                correctPosition(i);
            }

            for(int i = 0; i < nbThreads; i++){
                threads[i].join();
            }

            ranking();
            actualizeCm();
            t++;
        }

    }

    /**
     * Initialisation of CSS algorithm. With random position for each particles
     */
    public void initialisation() throws InterruptedException {
        for(ChargedParticule c : CPs){
            Vecteur v = new Vecteur(Math.random() * ( v2.getX() - v1.getX() ),Math.random() * ( v2.getY() - v1.getY() ));
            c.setOldPosition(v);
            c.setPosition(v);
        }
        ranking();

    }

    /**
     * Actualize the force Q and the fitness F for each particles. Then Rank them by Q.
     */
    public void ranking() throws InterruptedException {
        for(int t = 0; t < nbThreads; t++) {
            int finalT = t;
            threads[t] = new Thread(){
                public void run(){
                    try {
                        sem[0].acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    double min = CPs.get(0).getF();
                    sem[0].release();

                    try {
                        sem[CPs.size()-1].acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    double max = CPs.get(CPs.size()-1).getF();
                    sem[CPs.size()-1].release();
                    for(int i = finalT * nLoc; i < (finalT == nbThreads-1 ? CPs.size() : (finalT +1)*nLoc); i++){
                        CPs.get(i).setF(fitness(CPs.get(i).getPosition().getX(), CPs.get(i).getPosition().getY()));


                        CPs.get(i).setQ(CPs.get(i).getF()- max / min -  max);
                    }
                }
            };
        }

        for(int i = 0; i < nbThreads; i++){
            threads[i].start();
        }
        for(int i = 0; i < nbThreads; i++){
            threads[i].join();
        }

        Collections.sort(CPs);

    }

    /**
     * Create Cm, to memorize the first 1/4 of particles with best Q.
     */
    public void createCm() {
        for (int i = 0; i < CPs.size()/4; ++i)
            cm.add(CPs.get(i));
    }

    /**
     * Determines the impact of the particles between them
     * @param indx
     * @return
     */
    public Vecteur determineTheImpact(int indx){

        Vecteur F = new Vecteur(0,0);
        for (int j = 0; j < CPs.size(); ++j)
        {
            if (j != indx)
            {
                ChargedParticule tmp = CPs.get(j);
                double p = (tmp.getF() < CPs.get(indx).getF()) ? 1 : 0;
                Vecteur v = tmp.getOldPosition().minus(CPs.get(indx).getOldPosition());
                double rij = v.norm();
                Vecteur divater = tmp.getOldPosition().add(CPs.get(indx).getOldPosition());
                divater.setX(divater.getX()/2);  divater.setY(divater.getY()/2);
                divater.minus(cm.get(0).getOldPosition());
                rij /= divater.norm() + epsilon;
                if (rij < a)
                    F=F.add(tmp.getOldPosition().minus(CPs.get(indx).getOldPosition()).multConstante( (tmp.getQ()*rij*p) / (a*a*a)));
                else
                    F=F.add(tmp.getOldPosition().minus(CPs.get(indx).getOldPosition()).multConstante((tmp.getQ()*p)/(rij*rij)));
            }
        }
        return F;
    }

    /**
     * Update the position and the velocity of each particles regarding the impact of each ones
     * @param indx
     */
    public void updatePositionAndVelocity(int indx)  throws InterruptedException{
        Vecteur F = determineTheImpact(indx);
        Vecteur oldPos = CPs.get(indx).getOldPosition();
        Vecteur newF = F.multConstante(ka*Math.random());
        Vecteur newVel = CPs.get(indx).getVelocity().multConstante(kv*Math.random());
        Vecteur result = oldPos.add(newF).add(newVel);
        sem[indx].acquire();
        CPs.get(indx).setPosition(result);
        CPs.get(indx).setVelocity(CPs.get(indx).getPosition().minus(CPs.get(indx).getOldPosition()));
        sem[indx].release();
    }

    /**
     * Manages the exit of particle from study boundaries
     * @param indx
     */
    public void correctPosition(int indx) throws InterruptedException {
        Vecteur v1rand = new Vecteur(Math.random()*v2.getY(), Math.random()*v2.getY());
        if(CPs.get(indx).getPosition().getX() < v1.getX() || CPs.get(indx).getPosition().getX() > v2.getX()) {
            Vecteur tmp = new Vecteur(v1rand.getX(),CPs.get(indx).getPosition().getY());
            sem[indx].acquire();
            CPs.get(indx).setPosition(tmp);
            sem[indx].release();
        }
        if(CPs.get(indx).getPosition().getY() < v1.getY() || CPs.get(indx).getPosition().getY() > v2.getY()) {
            Vecteur tmp = new Vecteur(CPs.get(indx).getPosition().getX(),v1rand.getY());
            sem[indx].acquire();
            CPs.get(indx).setPosition(tmp);
            sem[indx].release();
        }
    }

    /**
     * Actualize Cm, the first 1/4 of particles with best Q
     */
    public void actualizeCm(){
        int j = 0;
        for(int i =0 ; i<cm.size(); i++){
            for(;j<cm.size();j++){
                if(CPs.get(i).getF() < cm.get(j).getF()){
                    cm.add(j,CPs.get(i));
                    break;
                }
            }
        }
    }

    /**
     * Objective Function
     * @param x
     * @param y
     * @return
     */
    public double fitness (double x, double y){
        if(fun == Main.objective_function.MCCORMICK) {
            return sin(x+y)+pow(x-y,2)-1.0*x +2.5*y+1;
        }else if(fun == Main.objective_function.BUKIN) {
            return 100 * sqrt(abs(y - 0.01 * x * x)) + 0.01 * abs(x + 10);

        }else if(fun == Main.objective_function.BOOTH){
            return pow((x+2*y-7),2) + pow((2*x+y-5), 2);
        }
        else{
            return x*x + y*y;
        }
    }





}
