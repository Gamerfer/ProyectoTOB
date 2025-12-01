package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class UI {

    GamePanel gP;
    Font fuenteArial_40, fuenteArial_80B;
    
    // Imágenes
    BufferedImage imagenGameOver;
    BufferedImage imagenVictoria;
    BufferedImage imagenInicio; // Nueva imagen para la portada
    BufferedImage corazonFull;

    // Estados booleanos (GamePanel maneja el estado principal, UI solo dibuja)
    public boolean juegoTerminado = false;
    public boolean victoria = false;
    
    public UI(GamePanel gP) {
        this.gP = gP;
        
        fuenteArial_40 = new Font("Arial", Font.PLAIN, 40);
        fuenteArial_80B = new Font("Arial", Font.BOLD, 80);

        try {
            imagenGameOver = ImageIO.read(getClass().getResourceAsStream("/spritesMensajes/gameover.png"));
            imagenVictoria = ImageIO.read(getClass().getResourceAsStream("/spritesMensajes/victoria.png"));
            corazonFull = ImageIO.read(getClass().getResourceAsStream("/spritesMensajes/corazon_full.png"));
            
            // --- CARGAR IMAGEN DE INICIO ---
            imagenInicio = ImageIO.read(getClass().getResourceAsStream("/spritesMensajes/inicio.png"));
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("¡ERROR! Revisa que inicio.png esté en la carpeta spritesMensajes");
        }
    }

    public void draw(Graphics2D g2) {
        
        // --- 1. PANTALLA DE INICIO ---
        // Si el GamePanel está en estado "titleState" (0), dibujamos la portada
        if (gP.gameState == gP.titleState) {
            
            // Fondo negro (opcional, por si la imagen no cubre todo)
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, gP.getAnchoPantalla(), gP.getAltoPantalla());

            // Dibujar imagen de inicio centrada
            if (imagenInicio != null) {
                int x = gP.getAnchoPantalla() / 2 - (imagenInicio.getWidth() / 2);
                int y = gP.getAltoPantalla() / 2 - (imagenInicio.getHeight() / 2);
                g2.drawImage(imagenInicio, x, y, null);
            }


        }
        
        // --- 2. PANTALLA DE VICTORIA ---
        else if (gP.gameState == gP.winState) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, gP.getAnchoPantalla(), gP.getAltoPantalla());

            if(imagenVictoria != null){
                int x = gP.getAnchoPantalla() / 2 - imagenVictoria.getWidth() / 2;
                int y = gP.getAltoPantalla() / 2 - imagenVictoria.getHeight() / 2;
                g2.drawImage(imagenVictoria, x, y, null);
            }
        }
        
        // --- 3. PANTALLA GAME OVER ---
        else if (gP.gameState == gP.gameOverState) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, gP.getAnchoPantalla(), gP.getAltoPantalla());

            if(imagenGameOver != null) {
                int x = gP.getAnchoPantalla()/2 - (imagenGameOver.getWidth()/2);
                int y = gP.getAltoPantalla()/2 - (imagenGameOver.getHeight()/2);
                g2.drawImage(imagenGameOver, x, y, null);
            } else {
                String texto = "PERDISTE";
                g2.setFont(fuenteArial_80B);
                g2.setColor(Color.RED);
                int longitud = (int)g2.getFontMetrics().getStringBounds(texto, g2).getWidth();
                int x = gP.getAnchoPantalla()/2 - longitud/2;
                int y = gP.getAltoPantalla()/2;
                g2.drawString(texto, x, y);
            }
            // Puntaje final
            g2.setFont(fuenteArial_40);
            g2.setColor(Color.WHITE);
            String textoPuntaje = "Puntaje Final: " + gP.getJugador().getPuntuacion();
            int longitud = (int)g2.getFontMetrics().getStringBounds(textoPuntaje, g2).getWidth();
            int x = gP.getAnchoPantalla()/2 - longitud/2;
            int y = gP.getAltoPantalla()/2 + 300;
            g2.drawString(textoPuntaje, x, y);
        } 
        
        // --- 4. PANTALLA DE JUEGO (HUD) ---
        else {
            g2.setFont(fuenteArial_40);
            g2.setColor(Color.WHITE);
            g2.drawString("Puntos: " + gP.getJugador().getPuntuacion(), 50, 50);


            // Corazones
            int vidas = gP.getJugador().getVidaActual();
            int xStart = gP.getAnchoPantalla() - 150; 
            int yStart = 20;
            int size = 32; 
            int espacio = 36; 

            for(int i = 0; i < vidas; i++) {
                if(corazonFull != null) {
                    g2.drawImage(corazonFull, xStart + (i*espacio), yStart, size, size, null);
                }
            }
        }
    }
}