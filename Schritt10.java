import codedraw.CodeDraw;
import java.awt.Color;
import java.util.*;

public class Schritt10 {
    static final Color WEISS = new Color(250, 249, 246);
    static final Color SCHWARZ = new Color(38, 38, 38);
    static final Color GELB = new Color(246, 202, 66);
    static final Color ROT = new Color(215, 74, 69);
    static final Color BLAU = new Color(65, 116, 181);
    static final Color GRUEN = new Color(65, 151, 108);
    static final Color BEIGE = new Color(226, 216, 190);

    static final Color[] PALETTE = {WEISS, SCHWARZ, GELB, ROT, BLAU, GRUEN, BEIGE};
    static final String[] LAYOUTS = {
            "3x3 grid", "4x4 grid", "5x5 grid", "Recursive grid",
            "Tiled 2,3", "Tiled 3,2", "Tiled 3,4", "Tiled 4,3",
            "Tiled 4,5", "Tiled 5,4", "Tiled 5,6", "6x6 grid"
    };
    // Annaeherung an die auf Art Blocks beobachteten Haeufigkeiten.
    static final int[] LAYOUT_GEWICHTE = {245,217,236,135,30,25,28,25,17,30,6,6};

    static class Peg {
        double x, y, r;
        boolean verbunden;
        Peg(double x, double y, double r) { this.x=x; this.y=y; this.r=r; }
    }
    static class Punkt {
        double x, y;
        Punkt(double x, double y) { this.x=x; this.y=y; }
    }
    static class Merkmale {
        int layout;
        String scaling, style, wrap, orientation, size;
        Color body, background, extra;
    }
    static class Bild {
        Merkmale m;
        ArrayList<Peg> pegs, faden;
        double[] kontur;
        Bild(Merkmale m, ArrayList<Peg> pegs, ArrayList<Peg> faden, double[] kontur) {
            this.m=m; this.pegs=pegs; this.faden=faden; this.kontur=kontur;
        }
    }

    public static void main(String[] args) {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : System.nanoTime();
        Random zufall = new Random(seed);
        Merkmale merkmale = waehleMerkmale(zufall);
        Bild bild = null;
        for (int i=0; i<10000 && bild==null; i++) bild = erzeugeBild(zufall, merkmale);
        if (bild==null) throw new IllegalStateException("Kein passendes Bild. Layout="+LAYOUTS[merkmale.layout]+" Wrap="+merkmale.wrap+" Orientierung="+merkmale.orientation+" Size="+merkmale.size);

        CodeDraw cd = new CodeDraw(800, 800);
        Merkmale m = bild.m;
        cd.setColor(m.background);
        cd.fillRectangle(0, 0, 800, 800);
        cd.setColor(m.body);
        cd.fillPolygon(bild.kontur);
        cd.setColor(m.background.equals(SCHWARZ) ? WEISS : SCHWARZ);
        cd.setLineWidth(5);
        cd.drawPolygon(bild.kontur);

        // Weave und Loop unterscheiden sich bereits bei der berechneten Geometrie:
        // Loop verwendet eine aeussere Rundung, Weave laesst Einbuchtungen zu.
        ArrayList<Peg> bullseyes = new ArrayList<>();
        if (m.style.equals("Bullseye")) {
            bullseyes.addAll(bild.pegs);
            Collections.shuffle(bullseyes, zufall);
            int wieViele = Math.min(bullseyes.size(), 3 + zufall.nextInt(6));
            bullseyes = new ArrayList<>(bullseyes.subList(0, wieViele));
        }
        Color konturFarbe = m.background.equals(SCHWARZ) ? WEISS : SCHWARZ;
        int accentIndex = m.extra == null ? -1 : zufall.nextInt(bild.pegs.size());
        for (int i=0; i<bild.pegs.size(); i++) {
            Peg p = bild.pegs.get(i);
            Color farbe = p.verbunden && ((i%3)==0) ? SCHWARZ : WEISS;
            if (m.background.equals(SCHWARZ) && farbe.equals(SCHWARZ)) farbe = BEIGE;
            if (i==accentIndex) farbe = m.extra;
            cd.setColor(farbe);
            cd.fillCircle(p.x,p.y,p.r);
            cd.setColor(konturFarbe);
            cd.setLineWidth(3);
            cd.drawCircle(p.x,p.y,p.r);
            if (bullseyes.contains(p)) {
                for (int ring=1; ring<=3; ring++) {
                    cd.setLineWidth(2.2);
                    cd.drawCircle(p.x,p.y,p.r*(1.0-ring*0.23));
                }
            }
        }
        System.out.println("Seed: " + seed);
        System.out.println("Peg layout: " + LAYOUTS[m.layout]);
        System.out.println("Peg count: " + bild.pegs.size());
        System.out.println("Peg scaling: " + m.scaling);
        System.out.println("Peg style: " + m.style);
        System.out.println("Wrap style: " + m.wrap);
        System.out.println("Wrap orientation: " + m.orientation);
        System.out.println("Body: " + farbname(m.body));
        System.out.println("Background: " + farbname(m.background));
        System.out.println("Extra color: " + (m.extra==null ? "N/A" : farbname(m.extra)));
        System.out.println("Size: " + m.size);
        System.out.println("Vom Faden genutzte Kreise: " + bild.faden.size());
        cd.show();
    }

    static String farbname(Color c) {
        String[] namen = {"White","Black","Yellow","Red","Blue","Green","Beige"};
        for (int i=0; i<PALETTE.length; i++) if (PALETTE[i].equals(c)) return namen[i];
        return "Andere";
    }
    static int gewichtet(Random z, int... w) {
        int sum=0; for(int a:w) sum+=a;
        int n=z.nextInt(sum);
        for(int i=0;i<w.length;i++) {n-=w[i]; if(n<0) return i;}
        return w.length-1;
    }
    static Merkmale waehleMerkmale(Random z) {
        Merkmale m=new Merkmale();
        m.layout=gewichtet(z,LAYOUT_GEWICHTE);
        m.scaling=new String[]{"Uniform","Bigger near center","Smaller near center"}
                [gewichtet(z,772,124,104)];
        m.style=z.nextDouble()<0.075?"Bullseye":"Solid";
        m.wrap=z.nextDouble()<0.505?"Loop":"Weave";
        m.orientation=z.nextDouble()<0.788?"Balanced":"Off-center";
        m.size=z.nextDouble()<0.036?"smol boi":"Normal";
        // Diese Haeufigkeiten stammen aus den 1000 fertigen Werken.
        m.body=new Color[]{WEISS,SCHWARZ,GELB,ROT,BLAU}
                [gewichtet(z,520,402,54,18,6)];
        // Weiss 80,2 Prozent; Verteilung der anderen Farben angenaehert.
        m.background=new Color[]{WEISS,SCHWARZ,BEIGE,GELB,ROT,BLAU,GRUEN}
                [gewichtet(z,802,13,48,71,56,8,2)];
        int extra=gewichtet(z,845,123,28,4);
        m.extra=new Color[]{null,ROT,BLAU,GRUEN}[extra];
        return m;
    }

    static Bild erzeugeBild(Random z, Merkmale m) {
        ArrayList<Punkt> raster=rasterPunkte(z,m.layout,m.size);
        if(raster.size()<4) return null;
        Collections.shuffle(raster,z);
        // Die Zahl sichtbarer Kreise ist von der gewaehlten Rasteranordnung abhaengig.
        double fuellung=z.nextDouble()<0.35?1.0:(0.52+0.45*z.nextDouble());
        int gesamt=Math.min(raster.size(),Math.max(4,(int)Math.round(raster.size()*fuellung)));
        double maxDist=m.size.equals("smol boi")?250:410;
        double basis=m.size.equals("smol boi")?13:22;
        if(m.layout==3) basis*=0.72; // bei dicht verschachteltem Raster
        ArrayList<Peg> pegs=new ArrayList<>();
        for(int i=0;i<gesamt;i++) {
            Punkt p=raster.get(i);
            double t=Math.min(1,Math.hypot(p.x-400,p.y-400)/maxDist);
            double faktor=1;
            if(m.scaling.equals("Bigger near center")) faktor=1.55-0.85*t;
            if(m.scaling.equals("Smaller near center")) faktor=0.65+0.90*t;
            pegs.add(new Peg(p.x,p.y,basis*faktor));
        }
        int maximum=Math.min(pegs.size(),12);
        int anzahl=3+z.nextInt(maximum-2);
        ArrayList<Peg> kandidaten=new ArrayList<>(pegs);
        if(m.orientation.equals("Off-center")) {
            final double ax=400+(z.nextBoolean()?1:-1)*(m.size.equals("smol boi")?100:210);
            final double ay=400+(z.nextBoolean()?1:-1)*(m.size.equals("smol boi")?100:210);
            final IdentityHashMap<Peg,Double> sortierung=new IdentityHashMap<>();
            for(Peg p:kandidaten) sortierung.put(p,Math.hypot(p.x-ax,p.y-ay)+z.nextDouble()*210);
            kandidaten.sort(Comparator.comparingDouble(sortierung::get));
        } else Collections.shuffle(kandidaten,z);
        ArrayList<Peg> faden=new ArrayList<>(kandidaten.subList(0,anzahl));
        double mx=0,my=0;
        for(Peg p:faden){mx+=p.x;my+=p.y;}
        mx/=faden.size(); my/=faden.size();
        double versatz=Math.hypot(mx-400,my-400);
        if(m.orientation.equals("Balanced") && versatz>(m.size.equals("smol boi")?75:115))return null;
        if(m.orientation.equals("Off-center") && versatz<(m.size.equals("smol boi")?40:95))return null;
        final double xM=mx,yM=my;
        faden.sort(Comparator.comparingDouble(p->Math.atan2(p.y-yM,p.x-xM)));
        entferneKreuzungen(faden);
        if(flaeche(faden)<0)Collections.reverse(faden);
        int einbuchtungen=0;
        for(int i=0;i<faden.size();i++) {
            Peg a=faden.get((i-1+faden.size())%faden.size());
            Peg b=faden.get(i), c=faden.get((i+1)%faden.size());
            if(kreuz(a.x,a.y,b.x,b.y,c.x,c.y)<-0.0001)einbuchtungen++;
        }
        if(m.wrap.equals("Loop") && einbuchtungen>0)return null;
        if(m.wrap.equals("Weave") && einbuchtungen==0 && raster.size()>9)return null;
        double[] kontur=berechneKontur(faden);
        if(kontur==null || !istEinfach(kontur))return null;
        for(Peg p:faden)p.verbunden=true;
        return new Bild(m,pegs,faden,kontur);
    }

    static ArrayList<Punkt> rasterPunkte(Random z,int layout,String size) {
        ArrayList<Punkt> punkte=new ArrayList<>();
        int zeilen=4,spalten=4;
        switch(layout) {
            case 0: zeilen=spalten=3;break;
            case 1: zeilen=spalten=4;break;
            case 2: zeilen=spalten=5;break;
            case 4: zeilen=2;spalten=3;break;
            case 5: zeilen=3;spalten=2;break;
            case 6: zeilen=3;spalten=4;break;
            case 7: zeilen=4;spalten=3;break;
            case 8: zeilen=4;spalten=5;break;
            case 9: zeilen=5;spalten=4;break;
            case 10: zeilen=5;spalten=6;break;
            case 11: zeilen=spalten=6;break;
        }
        double breite=size.equals("smol boi")?300:540;
        if(layout==3) {
            double schritt=breite/2.0;
            for(int r=0;r<3;r++)for(int c=0;c<3;c++) {
                double x=400-breite/2+c*schritt,y=400-breite/2+r*schritt;
                if(z.nextDouble()<0.45) {
                    for(int dx=-1;dx<=1;dx+=2)for(int dy=-1;dy<=1;dy+=2)
                        punkte.add(new Punkt(x+dx*schritt*0.18,y+dy*schritt*0.18));
                } else punkte.add(new Punkt(x,y));
            }
        } else {
            // Rechteckraster mit proportionalem Abstand in beide Richtungen.
            double schritt=breite/Math.max(zeilen-1,spalten-1);
            for(int r=0;r<zeilen;r++)for(int c=0;c<spalten;c++)
                punkte.add(new Punkt(400+(c-(spalten-1)/2.0)*schritt,
                        400+(r-(zeilen-1)/2.0)*schritt));
        }
        return punkte;
    }

    static void entferneKreuzungen(ArrayList<Peg> p) {
        int n=p.size();
        for(int r=0;r<30;r++) {
            boolean geaendert=false;
            aussen:for(int i=0;i<n;i++)for(int j=i+2;j<n;j++) {
                int i2=(i+1)%n,j2=(j+1)%n;
                if(i==0&&j2==0)continue;
                if(schneiden(p.get(i).x,p.get(i).y,p.get(i2).x,p.get(i2).y,
                        p.get(j).x,p.get(j).y,p.get(j2).x,p.get(j2).y)) {
                    Collections.reverse(p.subList(i+1,j+1));geaendert=true;break aussen;
                }
            }
            if(!geaendert)return;
        }
    }
    static double flaeche(ArrayList<Peg> p) {
        double a=0;
        for(int i=0;i<p.size();i++) {
            Peg u=p.get(i),v=p.get((i+1)%p.size());a+=u.x*v.y-v.x*u.y;
        }
        return a;
    }
    static double[] berechneKontur(ArrayList<Peg> kreise) {
        int n=kreise.size();
        Punkt[] von=new Punkt[n],bis=new Punkt[n];
        double[] winkel=new double[n];
        ArrayList<Double> result=new ArrayList<>();
        for(int i=0;i<n;i++) {
            Peg a=kreise.get(i),b=kreise.get((i+1)%n);
            double dx=b.x-a.x,dy=b.y-a.y,d=Math.hypot(dx,dy);
            double k=(a.r-b.r)/d;
            if(Math.abs(k)>=1)return null;
            double h=Math.sqrt(1-k*k);
            double nx=k*dx/d+h*dy/d,ny=k*dy/d-h*dx/d;
            von[i]=new Punkt(a.x+a.r*nx,a.y+a.r*ny);
            bis[i]=new Punkt(b.x+b.r*nx,b.y+b.r*ny);
            winkel[i]=Math.atan2(ny,nx);
        }
        boolean[] konkav=new boolean[n];
        for(int j=0;j<n;j++) {
            int i=(j-1+n)%n;
            Peg a=kreise.get(i),b=kreise.get(j),c=kreise.get((j+1)%n);
            if(kreuz(a.x,a.y,b.x,b.y,c.x,c.y)<-0.0001) {
                konkav[j]=true;
                Punkt schnitt=geradenSchnitt(von[i],bis[i],von[j],bis[j]);
                if(schnitt==null||Math.hypot(schnitt.x-b.x,schnitt.y-b.y)>3*b.r)return null;
                bis[i]=schnitt;von[j]=schnitt;
            }
        }
        for(int i=0;i<n;i++) {
            int j=(i+1)%n;
            hinzufuegen(result,von[i]);hinzufuegen(result,bis[i]);
            if(!konkav[j]) {
                Peg p=kreise.get(j);
                double delta=Math.atan2(Math.sin(winkel[j]-winkel[i]),Math.cos(winkel[j]-winkel[i]));
                if(delta< -0.00001)return null;
                int seg=Math.max(2,(int)Math.ceil(delta/0.07));
                for(int s=1;s<=seg;s++) {
                    double t=winkel[i]+delta*s/seg;
                    hinzufuegen(result,new Punkt(p.x+p.r*Math.cos(t),p.y+p.r*Math.sin(t)));
                }
            }
        }
        double[] a=new double[result.size()];
        for(int i=0;i<a.length;i++)a[i]=result.get(i);
        return a;
    }
    static Punkt geradenSchnitt(Punkt a,Punkt b,Punkt c,Punkt d) {
        double ux=b.x-a.x,uy=b.y-a.y,vx=d.x-c.x,vy=d.y-c.y,det=ux*vy-uy*vx;
        if(Math.abs(det)<1e-6)return null;
        double t=((c.x-a.x)*vy-(c.y-a.y)*vx)/det;
        return new Punkt(a.x+t*ux,a.y+t*uy);
    }
    static boolean istEinfach(double[] p) {
        int n=p.length/2;
        if(n<3)return false;
        for(int i=0;i<n;i++)for(int j=i+2;j<n;j++) {
            int i2=(i+1)%n,j2=(j+1)%n;
            if(i==0&&j2==0)continue;
            if(schneiden(p[2*i],p[2*i+1],p[2*i2],p[2*i2+1],
                    p[2*j],p[2*j+1],p[2*j2],p[2*j2+1]))return false;
        }
        return true;
    }
    static boolean schneiden(double ax,double ay,double bx,double by,
                             double cx,double cy,double dx,double dy) {
        double a=kreuz(ax,ay,bx,by,cx,cy),b=kreuz(ax,ay,bx,by,dx,dy);
        double c=kreuz(cx,cy,dx,dy,ax,ay),d=kreuz(cx,cy,dx,dy,bx,by);
        return a*b< -0.000001&&c*d< -0.000001;
    }
    static double kreuz(double ax,double ay,double bx,double by,double px,double py) {
        return (bx-ax)*(py-ay)-(by-ay)*(px-ax);
    }
    static void hinzufuegen(ArrayList<Double> ziel,Punkt p) {ziel.add(p.x);ziel.add(p.y);}
}
