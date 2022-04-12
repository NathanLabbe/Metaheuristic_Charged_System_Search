import java.util.Comparator;



public class ChargedParticule implements Comparable {
    private Vecteur position;
    private Vecteur oldPosition;
    private Vecteur Velocity;
    private double q;
    private double f;

    public ChargedParticule(Vecteur position, Vecteur oldPosition, Vecteur velocity, double q, double f) {
        this.position = position;
        this.oldPosition = oldPosition;
        this.Velocity = velocity;
        this.q = q;
        this.f = f;
    }

    public Vecteur getPosition() {
        return position;
    }

    public void setPosition(Vecteur position) {
        this.position = position;
    }

    public Vecteur getOldPosition() {
        return oldPosition;
    }

    public void setOldPosition(Vecteur oldPosition) {
        this.oldPosition = oldPosition;
    }

    public Vecteur getVelocity() {
        return Velocity;
    }

    public void setVelocity(Vecteur velocity) {
        Velocity = velocity;
    }

    public double getQ() {
        return q;
    }

    public void setQ(double q) {
        this.q = q;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    @Override
    public int compareTo(Object o) {
        ChargedParticule cp = (ChargedParticule)o;
        if(this.getQ() > cp.getQ()){
            return -1;
        }else{
            return 0;
        }

    }
}
