import codedraw.CodeDraw;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class Schritt07 {

    static class Kreis {
        double x, y, radius;

        Kreis(double x, double y, double radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }

    static class Punkt {
        double x, y;

        Punkt(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) {
        CodeDraw cd = new CodeDraw(800, 800);
        Random zufall = new Random();
        ArrayList<Kreis> sichtbareKreise = new ArrayList<>();

        int raster = 3 + zufall.nextInt(4);
        double abstand = 520.0 / (raster - 1);

        do {
            sichtbareKreise.clear();

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
        } while (sichtbareKreise.size() < 5);

        ArrayList<Kreis> verbundeneKreise = null;
        double[] kontur = null;
        double mittelX = 0;
        double mittelY = 0;

        for (int versuch = 0; versuch < 3000; versuch++) {
            ArrayList<Kreis> auswahl = new ArrayList<>(sichtbareKreise);
            Collections.shuffle(auswahl, zufall);

            int maximum = Math.min(8, auswahl.size());
            int anzahl = 3 + zufall.nextInt(maximum - 2);
            ArrayList<Kreis> kandidat = new ArrayList<>(auswahl.subList(0, anzahl));

            double summeX = 0;
            double summeY = 0;

            for (Kreis kreis : kandidat) {
                summeX += kreis.x;
                summeY += kreis.y;
            }

            double mx = summeX / kandidat.size();
            double my = summeY / kandidat.size();

            kandidat.sort(Comparator.comparingDouble(kreis ->
                    Math.atan2(kreis.y - my, kreis.x - mx)
            ));

            entferneKreuzungen(kandidat);
            if (flaeche(kandidat) < 0) {
                Collections.reverse(kandidat);
            }

            double[] berechnet = berechneKontur(kandidat);

            if (berechnet != null && istEinfach(berechnet)) {
                verbundeneKreise = kandidat;
                kontur = berechnet;
                mittelX = mx;
                mittelY = my;
                break;
            }
        }

        if (kontur == null) {
            throw new IllegalStateException("Keine gueltige Kontur gefunden.");
        }

        cd.setColor(new Color(246, 202, 66));
        cd.fillPolygon(kontur);

        cd.setColor(Color.BLACK);
        cd.setLineWidth(4);
        cd.drawPolygon(kontur);

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

        cd.setColor(Color.BLUE);
        cd.fillCircle(mittelX, mittelY, 7);
        cd.show();
    }

    static double[] berechneKontur(ArrayList<Kreis> kreise) {
        int n = kreise.size();
        Punkt[] von = new Punkt[n];
        Punkt[] bis = new Punkt[n];
        double[] winkel = new double[n];
        ArrayList<Double> punkte = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Kreis a = kreise.get(i);
            Kreis b = kreise.get((i + 1) % n);
            double dx = b.x - a.x;
            double dy = b.y - a.y;
            double distanz = Math.hypot(dx, dy);
            double k = (a.radius - b.radius) / distanz;

            if (Math.abs(k) >= 1) {
                return null;
            }

            double h = Math.sqrt(1 - k * k);
            double nx = k * dx / distanz + h * dy / distanz;
            double ny = k * dy / distanz - h * dx / distanz;

            von[i] = new Punkt(a.x + a.radius * nx, a.y + a.radius * ny);
            bis[i] = new Punkt(b.x + b.radius * nx, b.y + b.radius * ny);
            winkel[i] = Math.atan2(ny, nx);
        }

        boolean[] einbuchtung = new boolean[n];

        for (int j = 0; j < n; j++) {
            int i = (j - 1 + n) % n;
            Kreis a = kreise.get(i);
            Kreis b = kreise.get(j);
            Kreis c = kreise.get((j + 1) % n);

            if (kreuz(a.x, a.y, b.x, b.y, c.x, c.y) < -0.0001) {
                einbuchtung[j] = true;
                Punkt schnitt = geradenSchnitt(von[i], bis[i], von[j], bis[j]);

                if (schnitt == null ||
                        Math.hypot(schnitt.x - b.x, schnitt.y - b.y) > 3 * b.radius) {
                    return null;
                }

                bis[i] = schnitt;
                von[j] = schnitt;
            }
        }

        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            hinzufuegen(punkte, von[i]);
            hinzufuegen(punkte, bis[i]);

            if (!einbuchtung[j]) {
                Kreis kreis = kreise.get(j);
                double delta = Math.atan2(
                        Math.sin(winkel[j] - winkel[i]),
                        Math.cos(winkel[j] - winkel[i])
                );

                if (delta < -0.00001) {
                    return null;
                }

                int segmente = Math.max(2, (int) Math.ceil(delta / 0.07));

                for (int s = 1; s <= segmente; s++) {
                    double t = winkel[i] + delta * s / segmente;
                    hinzufuegen(punkte, new Punkt(
                            kreis.x + kreis.radius * Math.cos(t),
                            kreis.y + kreis.radius * Math.sin(t)
                    ));
                }
            }
        }

        double[] ergebnis = new double[punkte.size()];
        for (int i = 0; i < ergebnis.length; i++) {
            ergebnis[i] = punkte.get(i);
        }
        return ergebnis;
    }

    static Punkt geradenSchnitt(Punkt a, Punkt b, Punkt c, Punkt d) {
        double ux = b.x - a.x;
        double uy = b.y - a.y;
        double vx = d.x - c.x;
        double vy = d.y - c.y;
        double det = ux * vy - uy * vx;

        if (Math.abs(det) < 0.000001) {
            return null;
        }

        double t = ((c.x - a.x) * vy - (c.y - a.y) * vx) / det;
        return new Punkt(a.x + t * ux, a.y + t * uy);
    }

    static void entferneKreuzungen(ArrayList<Kreis> kreise) {
        int n = kreise.size();

        for (int r = 0; r < 30; r++) {
            boolean geaendert = false;

            aussen:
            for (int i = 0; i < n; i++) {
                for (int j = i + 2; j < n; j++) {
                    int i2 = (i + 1) % n;
                    int j2 = (j + 1) % n;
                    if (i == 0 && j2 == 0) continue;

                    Kreis a = kreise.get(i);
                    Kreis b = kreise.get(i2);
                    Kreis c = kreise.get(j);
                    Kreis d = kreise.get(j2);

                    if (schneiden(a.x, a.y, b.x, b.y,
                            c.x, c.y, d.x, d.y)) {
                        Collections.reverse(kreise.subList(i + 1, j + 1));
                        geaendert = true;
                        break aussen;
                    }
                }
            }

            if (!geaendert) return;
        }
    }

    static double flaeche(ArrayList<Kreis> kreise) {
        double summe = 0;
        for (int i = 0; i < kreise.size(); i++) {
            Kreis a = kreise.get(i);
            Kreis b = kreise.get((i + 1) % kreise.size());
            summe += a.x * b.y - b.x * a.y;
        }
        return summe;
    }

    static boolean istEinfach(double[] p) {
        int n = p.length / 2;
        if (n < 3) return false;

        for (int i = 0; i < n; i++) {
            for (int j = i + 2; j < n; j++) {
                int i2 = (i + 1) % n;
                int j2 = (j + 1) % n;
                if (i == 0 && j2 == 0) continue;

                if (schneiden(p[2*i], p[2*i+1], p[2*i2], p[2*i2+1],
                        p[2*j], p[2*j+1], p[2*j2], p[2*j2+1])) {
                    return false;
                }
            }
        }
        return true;
    }

    static boolean schneiden(double ax, double ay, double bx, double by,
                             double cx, double cy, double dx, double dy) {
        double a = kreuz(ax, ay, bx, by, cx, cy);
        double b = kreuz(ax, ay, bx, by, dx, dy);
        double c = kreuz(cx, cy, dx, dy, ax, ay);
        double d = kreuz(cx, cy, dx, dy, bx, by);
        return a * b < -0.000001 && c * d < -0.000001;
    }

    static double kreuz(double ax, double ay, double bx, double by,
                        double px, double py) {
        return (bx - ax) * (py - ay) - (by - ay) * (px - ax);
    }

    static void hinzufuegen(ArrayList<Double> punkte, Punkt p) {
        punkte.add(p.x);
        punkte.add(p.y);
    }
}
