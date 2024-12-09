package Common;

import java.awt.*;
import java.io.Serializable;
import java.util.List;

public class CommandDRAW implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Point> points;
    private Color color;

    public CommandDRAW(List<Point> points, Color color) {
        this.points = points;
        this.color = color;
    }

    public List<Point> getPoints() {
        return points;
    }

    public Color getColor() {
        return color;
    }
}
