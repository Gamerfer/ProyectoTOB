package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JPanel;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import entidad.Enemigo;
import entidad.Jugador;
import entidad.Proyectil;
import tile.ManejadorTiles;

public class GamePanel extends JPanel implements Runnable {

    private static final long serialVersionUID = 6872273934923133050L;
    
    // Configuración de pantalla
    private final int tamanioOriginalTile = 16;
    private final int escala = 3;
    private final int tamanioTile = this.tamanioOriginalTile * this.escala;
    private final int maxColPantalla = 26;
    private final int maxRenPantalla = 15;
    private final int anchoPantalla = this.tamanioTile * this.maxColPantalla;
    private final int altoPantalla = this.tamanioTile * this.maxRenPantalla;

    // Configuración mundo
    private final int maxRenMundo = 50; 
    private final int maxColMundo = 50; 
    private final int anchoMundo = this.tamanioTile * this.maxColMundo;
    private final int altoMundo = this.tamanioTile * this.maxRenMundo;
    private final int FPS = 60; 

    // Sistema
    private Thread hebraJuego;
    private final ManejadorTeclas mT = new ManejadorTeclas();
    private final DetectorColisiones dC = new DetectorColisiones(this);
    
    // Entidades
    private final Jugador jugador = new Jugador(this, this.mT);
    private final ArrayList<Enemigo> listaEnemigos = new ArrayList<Enemigo>();
    private final ArrayList<Proyectil> listaProjectil= new ArrayList<Proyectil>();
    private final ManejadorTiles mTi = new ManejadorTiles(this);
    private final Clip musica = AudioSystem.getClip();
    
    private Clip aud_gameover;
    private Clip aud_golpePersonaje;
    private Clip aud_golpeZombie;
    private Clip aud_muerteZombie;

    // --- NUEVO: UI y ESTADOS DEL JUEGO ---
    public UI ui = new UI(this); // Instanciamos la UI
    public int gameState;
    public final int playState = 1;
    public final int gameOverState = 2;

    public GamePanel() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        this.setPreferredSize(new Dimension(this.anchoPantalla, this.altoPantalla));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(mT);
        this.setFocusable(true);
        
        this.gameState = playState; // Iniciamos jugando
    }

    public void configuraEnemigos() {
        final int NUM_ENEMIGOS_INICIALES = 10; 
        for (int i = 0; i < NUM_ENEMIGOS_INICIALES; i++) {
            listaEnemigos.add(new Enemigo(this));
        }
    }
    
    public void cargaMusica() {
        InputStream rawStream = getClass().getResourceAsStream("/sounds/musica.wav");
        BufferedInputStream bis = new BufferedInputStream(rawStream);
        AudioInputStream ais = null;
        try {
            ais = AudioSystem.getAudioInputStream(bis);
        } catch (Exception e) { e.printStackTrace(); }          
        try {
            musica.open(ais);
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    public void cargaEfectos() {
        try {
            // 1. Game Over
            InputStream is = getClass().getResourceAsStream("/sounds/gameover.wav");
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            aud_gameover = AudioSystem.getClip();
            aud_gameover.open(ais);

            // 2. Golpe al Personaje (Perder vida)
            is = getClass().getResourceAsStream("/sounds/golpe_personaje.wav");
            ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            aud_golpePersonaje = AudioSystem.getClip();
            aud_golpePersonaje.open(ais);

            // 3. Golpe al Zombie
            is = getClass().getResourceAsStream("/sounds/golpe_zombie.wav");
            ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            aud_golpeZombie = AudioSystem.getClip();
            aud_golpeZombie.open(ais);

            // 4. Muerte del Zombie
            is = getClass().getResourceAsStream("/sounds/muerte_zombie.wav");
            ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            aud_muerteZombie = AudioSystem.getClip();
            aud_muerteZombie.open(ais);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error cargando sonidos. Revisa los nombres de los archivos en /res/sounds/");
        }
    }

    public void reproducir(Clip clip) {
        if (clip != null) {
            // 1. Si el sonido ya está sonando, lo cortamos
            if (clip.isRunning()) {
                clip.stop();
            }
            // 2. Lo regresamos al principio (rebobinar)
            clip.setFramePosition(0);
            
            // 3. Le damos play
            clip.start();
        } else {
            // Esto te ayudará a saber si el archivo no se cargó
            System.out.println("ERROR: Intentando reproducir un audio NULL (No cargó correctamente)");
        }
    }

    public void iniciaHebraJuego() {
        this.cargaMusica();
        this.cargaEfectos();
        this.configuraEnemigos();
        this.hebraJuego = new Thread(this);
        this.hebraJuego.start();
    }

    @Override
    public void run() {
        double intervaloDibujo = 1000000000.0 / this.FPS; 
        double delta = 0; 
        long ultimaVez = System.nanoTime();
        long tiempoActual;

        musica.loop(10);
        
        while (this.hebraJuego != null) {
            tiempoActual = System.nanoTime();
            delta += (tiempoActual - ultimaVez) / intervaloDibujo;
            ultimaVez = tiempoActual;

            if (delta >= 1) {
                this.update();
                this.repaint();
                delta--;
            }
        }
    }

    public void update() {
        // Solo actualizamos la lógica si estamos en modo JUEGO
        if (gameState == playState) {
            
            this.jugador.update();
            
            // Revisar si el jugador murió
            if(this.jugador.getVidaActual() <= 0) {
                this.gameState = gameOverState;
                this.ui.juegoTerminado = true;
                musica.stop(); // Opcional: detener música
                reproducir(aud_gameover);
            }

         
            for (Enemigo e : listaEnemigos) {
                e.update();
                if (dC.revisaJugador(e)) {
                    // Guardamos la vida antes del golpe para saber si bajó
                    int vidaAntes = this.jugador.getVidaActual();
                    
                    this.jugador.recibeDanio(e.getDanio());
                    
                    // Si la vida bajó (significa que no era invencible), suena el golpe
                    if(this.jugador.getVidaActual() < vidaAntes) {
                        reproducir(aud_golpePersonaje); // <--- SONIDO GOLPE PERSONAJE
                    }
                }
            }

         // 3. LOGICA DISPARO A ZOMBIE
            Iterator<Proyectil> iteradorProyectil = listaProjectil.iterator();
            while (iteradorProyectil.hasNext()) {
                Proyectil proyectilActual = iteradorProyectil.next();
                proyectilActual.update();
                
                Iterator<Enemigo> iteradorEnemigo = listaEnemigos.iterator();
                boolean proyectilGolpeo = false;

                while (iteradorEnemigo.hasNext()) {
                    Enemigo enemigoActual = iteradorEnemigo.next();

                    if (dC.revisaEntidad(proyectilActual, enemigoActual)) {
                        enemigoActual.recibeDanio(proyectilActual.getDanio());
                        proyectilGolpeo = true;
                        
                        // Si el zombie muere
                        if (enemigoActual.getVidaActual() <= 0) {
                            reproducir(aud_muerteZombie); // <--- SONIDO ZOMBIE MUERE
                            enemigoActual.setPosicionAleatoria(); 
                            enemigoActual.setVidaActual(enemigoActual.getMaxVida());
                            jugador.setPuntuacion(jugador.getPuntuacion() + 10);
                        } else {
                            // Si el zombie sigue vivo
                            reproducir(aud_golpeZombie); // <--- SONIDO ZOMBIE HERIDO
                        }
                        break;
                    }
                }
                if (proyectilGolpeo || proyectilActual.getTimer() > 75) {
                     iteradorProyectil.remove(); 
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // 1. Dibujar Mundo y Entidades
        this.mTi.draw(g2);
        
        // Dibujar proyectiles
        for(Proyectil i : listaProjectil) i.draw(g2);
        
        // Dibujar enemigos
        for (Enemigo e : listaEnemigos) e.draw(g2); 
        
        this.jugador.draw(g2);

        // 2. Dibujar UI (Puntaje, Vidas, Game Over) encima de todo
        ui.draw(g2);

        g2.dispose();
    }

    // --- Getters ---
    public int getTamanioTile() { return this.tamanioTile; }
    public int getMaxRenPantalla() { return this.maxRenPantalla; }
    public int getMaxColPantalla() { return this.maxColPantalla; }
    public int getAnchoPantalla() { return this.anchoPantalla; }
    public int getAltoPantalla() { return this.altoPantalla; }
    public int getMaxRenMundo() { return this.maxRenMundo; }
    public int getMaxColMundo() { return this.maxColMundo; }
    public Jugador getJugador() { return this.jugador; }
    public int getAnchoMundo() { return this.anchoMundo; }
    public int getAltoMundo() { return this.altoMundo; }
    public DetectorColisiones getDetectorColisiones() { return this.dC; }
    public ManejadorTiles getManejadorTiles() { return this.mTi; }
    public ArrayList<Proyectil> getListaProjectil() { return listaProjectil; }
}