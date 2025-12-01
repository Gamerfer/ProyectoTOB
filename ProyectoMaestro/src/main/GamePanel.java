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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JPanel;

import entidad.Enemigo;
import entidad.Jefe; 
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

    // Sonidos
    private Clip aud_gameover;
    private Clip aud_golpePersonaje;
    private Clip aud_golpeZombie;
    private Clip aud_muerteZombie;
    private Clip aud_victoria;
    private Clip aud_Scream;

    // --- VARIABLES DEL JEFE FINAL ---
    public Jefe jefeFinal;
    public boolean jefeActivo = false; // Indica si el jefe ya apareció
    public boolean jefeDerrotado = false; // Indica si ya lo mataste
    public final int PUNTUACION_PARA_JEFE = 100; //

    // UI y ESTADOS DEL JUEGO
    public UI ui = new UI(this); 
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int gameOverState = 2;
    public final int winState = 3;


    public GamePanel() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        this.setPreferredSize(new Dimension(this.anchoPantalla, this.altoPantalla));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(mT);
        this.setFocusable(true);

        this.gameState = titleState;
    }

    public void configuraEnemigos() {
        final int NUM_ENEMIGOS_INICIALES = 10;
        for (int i = 0; i < NUM_ENEMIGOS_INICIALES; i++) {
            listaEnemigos.add(new Enemigo(this));
        }
    }

    public void cargaMusica() {
        try {
            InputStream rawStream = getClass().getResourceAsStream("/sounds/musica.wav");
            BufferedInputStream bis = new BufferedInputStream(rawStream);
            AudioInputStream ais = AudioSystem.getAudioInputStream(bis);
            musica.open(ais);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void cargaEfectos() {
        try {
            aud_gameover = cargarSonido("/sounds/gameover.wav");
            aud_golpePersonaje = cargarSonido("/sounds/golpe_personaje.wav");
            aud_golpeZombie = cargarSonido("/sounds/golpe_zombie.wav");
            aud_muerteZombie = cargarSonido("/sounds/muerte_zombie.wav");
            aud_victoria = cargarSonido("/sounds/victoria.wav");
            aud_Scream = cargarSonido("/sounds/zombieScream.wav");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error cargando sonidos.");
        }
    }
    
    // Método auxiliar para cargar sonidos más limpio
    private Clip cargarSonido(String ruta) throws Exception {
        InputStream is = getClass().getResourceAsStream(ruta);
        AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
        Clip clip = AudioSystem.getClip();
        clip.open(ais);
        return clip;
    }

    public void reproducir(Clip clip) {
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            clip.start();
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
        
        // Reproducir música en bucle
        if(musica != null) musica.loop(Clip.LOOP_CONTINUOUSLY);

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
    	
    	if (gameState == titleState) {
            // Si estamos en la portada y presionan ESPACIO (que es el disparo en tu código)
            if (mT.getDisparo() == true) {
                gameState = playState; // ¡COMIENZA EL JUEGO!
                // Opcional: reproducir sonido de inicio
            }
            return; // Importante: Salimos del método para que NO se muevan enemigos ni jugador todavía
        }
    	
    	if (gameState == gameOverState) {
            // Si presionan Espacio (getDisparo) mientras perdieron
            if (mT.getDisparo() == true) {
                reiniciarJuego();      // Llamamos al método que creamos arriba
                gameState = playState; // Cambiamos el estado a JUGAR
            }
            return; // Salimos para no procesar nada más
        }
    	
        if (gameState == playState) {
            this.jugador.update();

            // REVISAR CONDICIÓN DE DERROTA
            if(this.jugador.getVidaActual() <= 0) {
                this.gameState = gameOverState;
                this.ui.juegoTerminado = true;
                if(musica != null) musica.stop();
                reproducir(aud_Scream);
                reproducir(aud_gameover);
            }

            // --- LÓGICA DEL JEFE FINAL ---
            // 1. Verificar si debe aparecer
            if (!jefeActivo && !jefeDerrotado && jugador.getPuntuacion() >= PUNTUACION_PARA_JEFE) {
                jefeFinal = new Jefe(this); // Instanciar al jefe
                jefeActivo = true;
                System.out.println("¡EL JEFE HA APARECIDO EN EL CENTRO!");
                // Opcional: Podrías cambiar la música aquí
            }

            // 2. Actualizar Jefe si está activo
            if (jefeActivo) {
                jefeFinal.update();
                
                // Colisión Jefe golpea a Jugador
                if (dC.revisaJugador(jefeFinal)) {
                    int vidaAntes = this.jugador.getVidaActual();
                    this.jugador.recibeDanio(jefeFinal.getDanio());
                    if(this.jugador.getVidaActual() < vidaAntes) {
                        reproducir(aud_golpePersonaje);
                    }
                }
            }
            // -----------------------------

            // Lógica enemigos normales
            for (Enemigo e : listaEnemigos) {
                e.update();
                if (dC.revisaJugador(e)) {
                    int vidaAntes = this.jugador.getVidaActual();
                    this.jugador.recibeDanio(e.getDanio());
                    if(this.jugador.getVidaActual() < vidaAntes) {
                        reproducir(aud_golpePersonaje);
                    }
                }
            }

            // Lógica Proyectiles (Vs Enemigos y Vs Jefe)
            Iterator<Proyectil> iteradorProyectil = listaProjectil.iterator();
            while (iteradorProyectil.hasNext()) {
                Proyectil proyectilActual = iteradorProyectil.next();
                proyectilActual.update();
                boolean proyectilImpacto = false;

                // A) Verificar impacto contra JEFE
                if (jefeActivo) {
                    if (dC.revisaEntidad(proyectilActual, jefeFinal)) {
                        jefeFinal.recibeDanio(proyectilActual.getDanio());
                        proyectilImpacto = true;
                        reproducir(aud_golpeZombie); // Sonido de golpe

                        if (jefeFinal.getVidaActual() <= 0) {
                            jefeActivo = false;
                            jefeDerrotado = true;
                            reproducir(aud_muerteZombie);
                            jugador.setPuntuacion(jugador.getPuntuacion() + 500); // Super Bonus
                            System.out.println("¡JEFE DERROTADO!");
                            if(musica!= null) musica.stop();
                            ui.victoria = true;
                            reproducir(aud_victoria);
                            this.gameState = gameOverState;
                        }
                    }
                }

                // B) Verificar impacto contra Enemigos normales (si no golpeó al jefe)
                if (!proyectilImpacto) {
                    Iterator<Enemigo> iteradorEnemigo = listaEnemigos.iterator();
                    while (iteradorEnemigo.hasNext()) {
                        Enemigo enemigoActual = iteradorEnemigo.next();
                        if (dC.revisaEntidad(proyectilActual, enemigoActual)) {
                            enemigoActual.recibeDanio(proyectilActual.getDanio());
                            proyectilImpacto = true;

                            if (enemigoActual.getVidaActual() <= 0) {
                                reproducir(aud_muerteZombie);
                                enemigoActual.setPosicionAleatoria();
                                enemigoActual.setVidaActual(enemigoActual.getMaxVida());
                                jugador.setPuntuacion(jugador.getPuntuacion() + 10);
                            } else {
                                reproducir(aud_golpeZombie);
                            }
                            break; 
                        }
                    }
                }

                // Eliminar proyectil si golpeó algo o si voló muy lejos
                if (proyectilImpacto || proyectilActual.getTimer() > 75) {
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

        // Dibujar enemigos normales
        for (Enemigo e : listaEnemigos) e.draw(g2);

        // --- DIBUJAR JEFE ---
        if (jefeActivo && jefeFinal != null) {
            jefeFinal.draw(g2);
        }
        // --------------------

        this.jugador.draw(g2);
        
        // 2. Dibujar UI
        ui.draw(g2);
        g2.dispose();
    }
    
    public void reiniciarJuego() {
        // Restaurar al Jugador
        jugador.configuracionInicial(); 

        jugador.setPuntuacion(0); 

        // Limpiar las listas viejas
        listaEnemigos.clear();
        listaProjectil.clear();
        
        //Volver a crear enemigos
        configuraEnemigos(); 
        
        //REINICIAR LÓGICA DEL JEFE
        jefeFinal = null;       // Borra al jefe viejo de la memoria
        jefeActivo = false;     // decimos al juego que el jefe NO está en pantalla
        jefeDerrotado = false;  // decimos que NO lo hemos derrotado aún
        
        // Reiniciar banderas de la UI
        ui.juegoTerminado = false; 
        ui.victoria = false;

        // Reiniciar música
        if (musica != null) {
            musica.setFramePosition(0);
            musica.start(); 
        }
    }
    
    // Getters
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