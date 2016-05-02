package arconis.discovery;

/**
 * Created by aegis on 09/02/16.
 */
public class PositionData {

    // Private Fields

    double radius;
    double xPos;
    double yPos;

    // Getters & Setters

    public double getRadius(){
        return this.radius;
    }

    public double getXPos(){
        return this.xPos;
    }

    public double getYPos(){
        return this.yPos;
    }

    // Constructors

    public PositionData(double xPos, double yPos, double radius) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.radius = radius;
    }

}
