
import codedraw.*;
import java.util.Random;

public class Schritt02 {
    public static void main(String[] args) {

        CodeDraw cd = new CodeDraw(800, 800);
        Random zufall = new Random();

        int raster = 3 + zufall.nextInt(4);
        double abstand = 520.0 / (raster - 1);

        for (int zeile = 0; zeile < raster; zeile++) {
            for (int spalte = 0; spalte < raster; spalte++) {

                double x = 140 + spalte * abstand;
                double y = 140 + zeile * abstand;

                int radius = 13 + zufall.nextInt(18);

                cd.drawCircle(x, y, radius);
            }
        }

        cd.show();
    }
}
