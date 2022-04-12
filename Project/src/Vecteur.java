import static java.lang.Math.sqrt;

class Vecteur{
    private double x;
    private double y;
    public Vecteur(double x, double y){
        this.x=x;
        this.y=y;
    }

    public Vecteur minus(Vecteur v2){
        Vecteur v = new Vecteur(this.getX()-v2.getX(),this.getY()-v2.getY());
        return v;
    }

    public Vecteur add(Vecteur v2){
        Vecteur v = new Vecteur(this.getX()+v2.getX(),this.getY()+v2.getY());
        return v;
    }

    public Vecteur mult(Vecteur v2){
        Vecteur v = new Vecteur(this.getX()*v2.getX()+this.getX()*v2.getY(),this.getY()*v2.getX()+this.getY()*v2.getY());
        return v;
    }

    public Vecteur multConstante(double x){
        Vecteur v = new Vecteur(this.getX()*x,this.getY()*x);
        return v;

    }

    public Vecteur divConstante(double x){
        Vecteur v = new Vecteur(this.getX()/x,this.getY()/x);
        return v;
    }

    public double norm(){
        return sqrt(this.x*this.x + this.y*this.y);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
