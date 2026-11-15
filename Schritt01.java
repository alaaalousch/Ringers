

import codedraw.*;

    public class Schritt01 {
        public static void main(String[] args) {

            CodeDraw cd = new CodeDraw(800, 800);

            for (int zeile = 0; zeile < 5; zeile++) {
                for (int spalte = 0; spalte < 5; spalte++) {

                    int x = 100 + spalte * 100;
                    int y = 100 + zeile * 100;

                    cd.drawCircle(x, y, 20);
                }
            }

            cd.show();
        }
    }


