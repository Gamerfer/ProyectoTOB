package entidad;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import main.GamePanel;
import main.ManejadorTeclas;

public class Jugador extends Entidad {
	
	

    private final GamePanel gP;
    private final ManejadorTeclas mT;

    private long tiempo = 0;
    private int rango;
    private int retDisparo;
    private int puntuacion;

    // Variables de Invencibilidad
    private boolean invencible = false;
    private int invencibleContador = 0;

    private Clip caminar;
    private Clip aud_disparo;

    private final int pantallaX;
    private final int pantallaY;

    public Jugador(GamePanel gP, ManejadorTeclas mT) throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        this.gP = gP;
        this.mT = mT;

        this.pantallaX = gP.getAnchoPantalla() / 2 - (gP.getTamanioTile() / 2);
        this.pantallaY = gP.getAltoPantalla() / 2 - (gP.getTamanioTile() / 2);
        
        this.configuracionInicial();
        this.getSpritesJugador();
        this.getAudioJugador();
    }

    public void configuracionInicial() {
        this.mundoX = gP.getTamanioTile() * 22;
        this.mundoY = gP.getTamanioTile() * 30;
        this.velocidad = 5;
        this.direccion = "abajo";
        this.rango = 75;
        this.retDisparo = 10;
        this.puntuacion = 0;
        
        // --- CAMBIO: SISTEMA DE 3 VIDAS ---
        this.maxVida = 3; // 3 vidas como pediste
        this.vidaActual = maxVida;
        
        hitbox = 32;
        this.offset = (gP.getTamanioTile() - hitbox) / 2;
        this.areaSolida = new Rectangle(offset, offset, hitbox, hitbox);
    }

    public void getSpritesJugador() {
        // ... (Tu código de carga de imágenes se mantiene igual) ...
        try {
            this.arriba1 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverArriba1.png"));
            this.arriba2 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverArriba2.png"));
            this.abajo1 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverAbajo1.png"));
            this.abajo2 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverAbajo2.png"));
            this.izquierda1 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverIzquierda1.png"));
            this.izquierda2 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverIzquierda2.png"));
            this.derecha1 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverDerecha1.png"));
            this.derecha2 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverDerecha2.png"));
            this.neutro = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/neutral.png"));
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void getAudioJugador() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        // ... (Tu código de audio se mantiene igual) ...
        caminar = AudioSystem.getClip();
        aud_disparo = AudioSystem.getClip();
        InputStream rawStream = getClass().getResourceAsStream("/sounds/walk.wav");
        BufferedInputStream bis = new BufferedInputStream(rawStream);
        AudioInputStream ais = AudioSystem.getAudioInputStream(bis);            
        caminar.open(ais);
        rawStream = getClass().getResourceAsStream("/sounds/disparo.wav");
        bis = new BufferedInputStream(rawStream);
        ais = AudioSystem.getAudioInputStream(bis);
        aud_disparo.open(ais);
    }

    public void update() {
        if (mT.getTeclaArriba() || mT.getTeclaAbajo() || mT.getTeclaIzquierda() || mT.getTeclaDerecha()) {
            if (this.mT.getTeclaArriba()) {this.direccion = "arriba";}
            else if (this.mT.getTeclaAbajo()) {this.direccion = "abajo";}
            else if (this.mT.getTeclaIzquierda()) {this.direccion = "izquierda";}
            else if (this.mT.getTeclaDerecha()) {this.direccion = "derecha";}
            
            this.colisionActivada = false;
            this.gP.getDetectorColisiones().revisaTile(this);

            if (this.colisionActivada == false) {
                switch (this.getDireccion()) {
                case "arriba": this.setMundoY(this.getMundoY() - this.getVelocidad()); break;
                case "abajo": this.setMundoY(this.getMundoY() + this.getVelocidad()); break;
                case "izquierda": this.setMundoX(this.getMundoX() - this.getVelocidad()); break;
                case "derecha": this.setMundoX(this.getMundoX() + this.getVelocidad()); break;
                default: break;
                }
            }

            this.contadorSprites++;
            if (this.contadorSprites > this.cambiaSprite) {
                if (this.numeroSprites == 1) {
                    this.numeroSprites = 2;
                } else {
                    this.numeroSprites = 1;
                    caminar.setFramePosition(0);
                    caminar.start();
                }
                this.contadorSprites = 0;
            }
        }

        if(tiempo < retDisparo+1) tiempo++;
        
        if (mT.getDisparo() && tiempo > retDisparo){                                                     
            gP.getListaProjectil().add(new Proyectil(this.gP, this.mT));                                        
            tiempo = 0;                                                                                         
            aud_disparo.setFramePosition(0);
            aud_disparo.start();
        }
        
        if(!gP.getListaProjectil().isEmpty())
            for(int i=0; i<gP.getListaProjectil().size(); i++)
                if(gP.getListaProjectil().get(i).getTimer() > rango)
                    gP.getListaProjectil().remove(i);

        // --- LÓGICA DE INVENCIBILIDAD ---
        // Esto evita que pierdas las 3 vidas en un solo segundo al tocar un zombie
        if(invencible) {
            invencibleContador++;
            if(invencibleContador > 60) { // 60 frames = 1 segundo de invencibilidad
                invencible = false;
                invencibleContador = 0;
            }
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage sprite = null;

        switch (this.direccion) {
        case "arriba": sprite = (this.numeroSprites == 1) ? this.arriba1 : this.arriba2; break;
        case "abajo": sprite = (this.numeroSprites == 1) ? this.abajo1 : this.abajo2; break;
        case "izquierda": sprite = (this.numeroSprites == 1) ? this.izquierda1 : this.izquierda2; break;
        case "derecha": sprite = (this.numeroSprites == 1) ? this.derecha1 : this.derecha2; break;
        }

        // Efecto visual de parpadeo cuando eres invencible (te han golpeado)
        if (invencible == true) {
            // Hace al personaje 50% transparente
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f));
        }
        
        g2.drawImage(sprite, this.pantallaX, this.pantallaY, this.gP.getTamanioTile(), this.gP.getTamanioTile(), null); 
        
        // Restaurar opacidad
        g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));

        // NOTA: He quitado el dibujado de texto y barras aquí. Ahora se encarga la clase UI.
    }

    // --- NUEVO MÉTODO PARA RECIBIR DAÑO ---
    @Override
    public void recibeDanio(int danio) {
        if(!invencible) {
            this.vidaActual -= danio;
            invencible = true; // Activa la invencibilidad temporal
            if (this.vidaActual < 0) {
                this.vidaActual = 0;
            System.out.println("Jugador golpeado. Vidas restantes: " + vidaActual);
        }
    }
    }
    
    // Getters y Setters...
    public int getMundoX() { return this.mundoX; }
    public void setMundoX(int valor) { this.mundoX = valor; }
    public int getMundoY() { return this.mundoY; }
    public void setMundoY(int valor) { this.mundoY = valor; }
    public int getVelocidad() { return this.velocidad; }
    public int getPantallaX() { return this.pantallaX; }
    public int getPantallaY() { return this.pantallaY; }
    public int getAreaSolidaX() { return this.areaSolida.x; }
    public int getAreaSolidaY() { return this.areaSolida.y; }
    public int getAreaSolidaAncho() { return this.areaSolida.width; }
    public int getAreaSolidaAlto() { return this.areaSolida.height; }
    public String getDireccion() { return this.direccion; }
    public int getPuntuacion() { return this.puntuacion; }
    public void setPuntuacion(int x) { this.puntuacion = x; }
}