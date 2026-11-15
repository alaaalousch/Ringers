
import codedraw.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Schritt05 {

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

        ArrayList<Kreis> auswahl = new ArrayList<>(sichtbareKreise);
        Collections.shuffle(auswahl, zufall);

        if (auswahl.size() >= 3) {

            int anzahl = 3 + zufall.nextInt(
                    Math.min(8, auswahl.size()) - 2
            );

            ArrayList<Kreis> verbundeneKreise = new ArrayList<>(
                    auswahl.subList(0, anzahl)
            );

            double summeX = 0;
            double summeY = 0;

            for (Kreis kreis : verbundeneKreise) {
                summeX += kreis.x;
                summeY += kreis.y;
            }

            double mittelX = summeX / verbundeneKreise.size();
            double mittelY = summeY / verbundeneKreise.size();

            verbundeneKreise.sort((a, b) ->
                    Double.compare(
                            Math.atan2(a.y - mittelY, a.x - mittelX),
                            Math.atan2(b.y - mittelY, b.x - mittelX)
                    )
            );

            cd.setColor(Color.GRAY);
            cd.setLineWidth(2);

            for (int i = 0; i < verbundeneKreise.size(); i++) {

                Kreis a = verbundeneKreise.get(i);
                Kreis b = verbundeneKreise.get(
                        (i + 1) % verbundeneKreise.size()
                );

                cd.drawLine(a.x, a.y, b.x, b.y);
            }

            cd.setColor(Color.BLUE);
            cd.fillCircle(mittelX, mittelY, 7);

            cd.setColor(Color.BLACK);
            cd.setLineWidth(2);

            for (Kreis kreis : sichtbareKreise) {
                cd.drawCircle(kreis.x, kreis.y, kreis.radius);
            }

            cd.setColor(Color.RED);
            cd.setLineWidth(4);

            for (Kreis kreis : verbundeneKreise) {
                cd.drawCircle(kreis.x, kreis.y, kreis.radius);
            }

        }

        cd.show();
    }
}

