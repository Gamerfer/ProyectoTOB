package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.GamePanel;

public class Jefe extends Entidad {

    private GamePanel gP;

    public Jefe(GamePanel gP) {
        this.gP = gP;
        
        // --- TAMAÑO GIGANTE ---
        // Multiplicamos por 3 el tamaño del tile. 
        // Aunque la imagen original sea pequeña, aquí definimos que ocupa mucho espacio.
        int escalaJefe = 3; 
        this.hitbox = gP.getTamanioTile() * escalaJefe; 
        
        // Ajustamos la colisión (hitbox) para que sea un poco más pequeña que el dibujo
        // Esto evita que el jefe se quede atorado en pasillos estrechos visualmente.
        this.areaSolida = new Rectangle(0, 0, hitbox, hitbox);
        this.areaSolida.x = 20; // Margen interno
        this.areaSolida.y = 20;
        this.areaSolida.width = hitbox - 40;
        this.areaSolida.height = hitbox - 40;

        // --- ESTADÍSTICAS DE JEFE ---
        this.maxVida = 80;  // Mucha más vida que un zombie normal (que tiene 4)
        this.vidaActual = maxVida;
        this.velocidad = 4; // Puedes subirlo a 4 si quieres que sea muy difícil
        this.danio = 2;     // Quita más vida al golpear
        
        configuracionInicial();
        getSpritesJefe();
    }

    public void configuracionInicial() {
        // Aparece en el CENTRO EXACTO del mapa para evitar bugs de pared
        int centroCol = gP.getMaxColMundo() / 2;
        int centroRen = gP.getMaxRenMundo() / 2;
        
        this.mundoX = centroCol * gP.getTamanioTile();
        this.mundoY = centroRen * gP.getTamanioTile();
        
        this.direccion = "abajo";
    }

    public void getSpritesJefe() {
        try {
            String ruta = "/spritesEnemigo/"; 

            this.arriba1 = ImageIO.read(getClass().getResourceAsStream(ruta + "moverArriba1.png"));
            this.arriba2 = ImageIO.read(getClass().getResourceAsStream(ruta + "moverArriba2.png"));
            this.abajo1 = ImageIO.read(getClass().getResourceAsStream(ruta + "moverAbajo1.png"));
            this.abajo2 = ImageIO.read(getClass().getResourceAsStream(ruta + "moverAbajo2.png"));
            this.izquierda1 = ImageIO.read(getClass().getResourceAsStream(ruta + "moverIzquierda1.png"));
            this.izquierda2 = ImageIO.read(getClass().getResourceAsStream(ruta + "moverIzquierda2.png"));
            this.derecha1 = ImageIO.read(getClass().getResourceAsStream(ruta + "moverDerecha1.png"));
            this.derecha2 = ImageIO.read(getClass().getResourceAsStream(ruta + "moverDerecha2.png"));
            this.neutro = ImageIO.read(getClass().getResourceAsStream(ruta + "neutral.png"));
            
        } catch (IOException e) {
            System.out.println("ERROR: No encuentro las imágenes en /spritesEnemigo/");
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        // 1. IA: Perseguir al Jugador
        establecerDireccionDeSeguimiento();
        
        // 2. Colisiones
        this.colisionActivada = false;
        gP.getDetectorColisiones().revisaTile(this);

        // 3. Movimiento
        if (!this.colisionActivada) {
            switch (this.direccion) {
                case "arriba": this.mundoY -= this.velocidad; break;
                case "abajo": this.mundoY += this.velocidad; break;
                case "izquierda": this.mundoX -= this.velocidad; break;
                case "derecha": this.mundoX += this.velocidad; break;
            }
        }

        // 4. Animación de caminar 
        this.contadorSprites++;
        if (this.contadorSprites > 12) { 
            if (this.numeroSprites == 1) {
                this.numeroSprites = 2;
            } else {
                this.numeroSprites = 1;
            }
            this.contadorSprites = 0;
        }
    }

    private void establecerDireccionDeSeguimiento() {
        int jugadorX = gP.getJugador().getMundoX();
        int jugadorY = gP.getJugador().getMundoY();
        
        // IA simple: moverse en la dirección que reduzca más la distancia
        if (Math.abs(mundoX - jugadorX) > Math.abs(mundoY - jugadorY)) {
            if (mundoX < jugadorX) direccion = "derecha";
            else direccion = "izquierda";
        } else {
            if (mundoY < jugadorY) direccion = "abajo";
            else direccion = "arriba";
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Cálculo de posición relativa a la cámara
        int pantallaX = this.mundoX - gP.getJugador().getMundoX() + gP.getJugador().getPantallaX();
        int pantallaY = this.mundoY - gP.getJugador().getMundoY() + gP.getJugador().getPantallaY();

        // Selección del sprite animado
        BufferedImage sprite = null;
        switch (this.direccion) {
            case "arriba":    sprite = (this.numeroSprites == 1) ? this.arriba1 : this.arriba2; break;
            case "abajo":     sprite = (this.numeroSprites == 1) ? this.abajo1 : this.abajo2; break;
            case "izquierda": sprite = (this.numeroSprites == 1) ? this.izquierda1 : this.izquierda2; break;
            case "derecha":   sprite = (this.numeroSprites == 1) ? this.derecha1 : this.derecha2; break;
            default:          sprite = this.abajo1; break;
        }

        // --- BARRA DE VIDA DEL JEFE ---
        if(vidaActual < maxVida) {
            int anchoBarra = gP.getTamanioTile() * 3; // La barra es tan ancha como el jefe
            double escalaVida = (double)anchoBarra / maxVida;
            double largoVida = escalaVida * vidaActual;
            
            // Fondo oscuro
            g2.setColor(new Color(35, 35, 35));
            g2.fillRect(pantallaX, pantallaY - 20, anchoBarra, 10);
            
            // Vida roja
            g2.setColor(new Color(220, 0, 0));
            g2.fillRect(pantallaX, pantallaY - 20, (int)largoVida, 10);
            
            // Borde blanco
            g2.setColor(Color.WHITE);
            g2.drawRect(pantallaX, pantallaY - 20, anchoBarra, 10);
        }

        // --- DIBUJO GIGANTE ---
        // dibujamos multiplicada por 3
        if (sprite != null) {
            g2.drawImage(sprite, pantallaX, pantallaY, gP.getTamanioTile() * 3, gP.getTamanioTile() * 3, null);
        }
    }
}