
import codedraw.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Schritt04 {

    static class Kreis {
        double x, y, radius;

        Kreis(double x, double y, double radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }

    public static void main(String[] args) {

        CodeDraw cd = new CodeDraw(800, 800);
        Random zufall = new Random();

        ArrayList<Kreis> sichtbareKreise = new ArrayList<>();

        int raster = 3 + zufall.nextInt(4);
        double abstand = 520.0 / (raster - 1);

        for (int zeile = 0; zeile < raster; zeile++) {
            for (int spalte = 0; spalte < raster; spalte++) {

                double x = 140 + spalte * abstand;
                double y = 140 + zeile * abstand;
                int radius = 13 + zufall.nextInt(18);

                if (zufall.nextDouble() < 0.8) {
                    sichtbareKreise.add(new Kreis(x, y, radius));
                }
            }
        }

        cd.setColor(Color.BLACK);

        for (Kreis kreis : sichtbareKreise) {
            cd.drawCircle(kreis.x, kreis.y, kreis.radius);
        }

        ArrayList<Kreis> auswahl = new ArrayList<>(sichtbareKreise);
        Collections.shuffle(auswahl, zufall);

        if (auswahl.size() >= 3) {

            int anzahl = 3 + zufall.nextInt(
                    Math.min(8, auswahl.size()) - 2
            );

            ArrayList<Kreis> verbundeneKreise = new ArrayList<>(
                    auswahl.subList(0, anzahl)
            );

            cd.setColor(Color.RED);
            cd.setLineWidth(4);

            for (Kreis kreis : verbundeneKreise) {
                cd.drawCircle(kreis.x, kreis.y, kreis.radius);
            }
        }

        cd.show();
    }
}
