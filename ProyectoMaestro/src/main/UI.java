package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class UI {

    GamePanel gP;
    Font fuenteArial_40, fuenteArial_80B;
    
    BufferedImage imagenGameOver;
    BufferedImage imagenVictoria;
    BufferedImage corazonFull; // Variable para el corazón

    public boolean juegoTerminado = false;
    public boolean victoria = false;
    
    public UI(GamePanel gP) {
        this.gP = gP;
        
        fuenteArial_40 = new Font("Arial", Font.PLAIN, 40);
        fuenteArial_80B = new Font("Arial", Font.BOLD, 80);

        try {
            // Carga Game Over
            imagenGameOver = ImageIO.read(getClass().getResourceAsStream("/spritesMensajes/gameover.png"));
            imagenVictoria = ImageIO.read(getClass().getResourceAsStream("/spritesMensajes/victoria.png"));
            corazonFull = ImageIO.read(getClass().getResourceAsStream("/spritesMensajes/corazon_full.png"));
            
            System.out.println("¡Imagen del corazón cargada con éxito!"); // Mensaje de éxito en consola

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("¡ERROR! No encuentro la imagen. Revisa la carpeta /res/spritesMensajes/");
        }
    }

    public void draw(Graphics2D g2) {
    	if (victoria) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, gP.getAnchoPantalla(), gP.getAltoPantalla());

            int x = gP.getAnchoPantalla() / 2 - imagenVictoria.getWidth() / 2;
            int y = gP.getAltoPantalla() / 2 - imagenVictoria.getHeight() / 2;

            g2.drawImage(imagenVictoria, x, y, null);
            return;
        }
        if (juegoTerminado) {

            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, gP.getAnchoPantalla(), gP.getAltoPantalla());

            int x;
            int y;

            if(imagenGameOver != null) {
                x = gP.getAnchoPantalla()/2 - (imagenGameOver.getWidth()/2);
                y = gP.getAltoPantalla()/2 - (imagenGameOver.getHeight()/2);
                g2.drawImage(imagenGameOver, x, y, null);
            } else {
                String texto = "PERDISTE";
                g2.setFont(fuenteArial_80B);
                g2.setColor(Color.RED);
                int longitud = (int)g2.getFontMetrics().getStringBounds(texto, g2).getWidth();
                x = gP.getAnchoPantalla()/2 - longitud/2;
                y = gP.getAltoPantalla()/2;
                g2.drawString(texto, x, y);
            }
            
            g2.setFont(fuenteArial_40);
            g2.setColor(Color.WHITE);
            String textoPuntaje = "Puntaje Final: " + gP.getJugador().getPuntuacion();
            int longitud = (int)g2.getFontMetrics().getStringBounds(textoPuntaje, g2).getWidth();
            x = gP.getAnchoPantalla()/2 - longitud/2;
            y = gP.getAltoPantalla()/2 + 100;
            g2.drawString(textoPuntaje, x, y);

        } else {
            // PANTALLA DE JUEGO 
            
            g2.setFont(fuenteArial_40);
            g2.setColor(Color.WHITE);
            g2.drawString("Puntos: " + gP.getJugador().getPuntuacion(), 50, 50);

            int vidas = gP.getJugador().getVidaActual();
            
            int xStart = gP.getAnchoPantalla() - 150; 
            int yStart = 20;
            int size = 32; 
            int espacio = 36; 

            for(int i = 0; i < vidas; i++) {
                if(corazonFull != null) {
                    // Dibuja la imagen PNG
                    g2.drawImage(corazonFull, xStart + (i*espacio), yStart, size, size, null);
                } else {

                }
            }
        }
    }
}