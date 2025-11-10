package entidad;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
/**
 * Clase base (o superclase) para todos los objetos "vivos" del juego.
 * Contiene todos los atributos y métodos comunes a Jugador y Enemigo.
 */
public class Entidad {

    // Coordenadas y movimiento [4, 5]
    protected int mundoX, mundoY;
    protected int velocidad;

    // --- VARIABLES DE SALUD (NUEVAS) ---
    protected int maxVida;
    protected int vidaActual;
    
    // Sprites y animación [5, 6]
    protected BufferedImage arriba1, arriba2, abajo1, abajo2, izquierda1, izquierda2, derecha1, derecha2;
    protected String direccion;
    protected int contadorSprites = 0;
    protected int numeroSprites = 1;
    protected int cambiaSprite = 10;
    
    // Colisión [6, 7]
    protected Rectangle areaSolida;
    protected boolean colisionActivada = false;

    // --- MÉTODOS BASE (Placeholders necesarios para sobrescribir) ---
    
    public void update() {
        // Implementación base.
    }

    public void draw(Graphics2D g2) {
        // Implementación base.
    }
    
    // --- GETTERS NECESARIOS PARA COLISIÓN (Extraídos de Jugador [1-3]) ---

    public int getMundoX() {
        return this.mundoX;
    }

    public int getMundoY() {
        return this.mundoY;
    }

    public int getVelocidad() {
        return this.velocidad;
    }

    public String getDireccion() {
        return this.direccion;
    }
    
    // Getters para las propiedades de la hitbox [3]
    public int getAreaSolidaX() {
        return this.areaSolida.x;
    }

    public int getAreaSolidaY() {
        return this.areaSolida.y;
    }

    public int getAreaSolidaAncho() {
        return this.areaSolida.width;
    }

    public int getAreaSolidaAlto() {
        return this.areaSolida.height;
    }
    
    // Getters de Vida (Nuevos)
    public int getMaxVida() {
        return this.maxVida;
    }

    public int getVidaActual() {
        return this.vidaActual;
    }
    
    // Setter existente [7]
    public void setColisionActivada(boolean valor) {
        this.colisionActivada = valor;
    }
}
