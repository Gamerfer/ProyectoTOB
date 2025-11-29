package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;

import entidad.Enemigo;
import entidad.Jugador;
import entidad.Proyectil;
import tile.ManejadorTiles;

/**
 * @author Los Ratones
 * @version 0.5
 * @since 11 de julio de 2025, 09:00 horas (horario de la Ciudad de México) * El
 *        corazón del juego. Este panel es donde se dibuja todo y donde se
 *        ejecuta el bucle principal del juego (game loop). Hereda de JPanel
 *        para ser un componente de Swing y implementa Runnable para poder
 *        ejecutarse en un hilo separado.
 */
public class GamePanel extends JPanel implements Runnable {

	private static final long serialVersionUID = 6872273934923133050L;				// Identificador único para la serialización de la clase, requerido por JPanel.
	
	//--------------LO QUE SE MUESTRA EN PANTALLA-----------------------
	private final int tamanioOriginalTile = 16;										// Tamaño original de los mosaicos (tiles) en píxeles (16x16).
	private final int escala = 3;													// Escala a la que se dibujarán los elementos para hacerlos más grandes.
	private final int tamanioTile = this.tamanioOriginalTile * this.escala;			// Tamaño final del mosaico (16 * 3 = 48 píxeles).

	// Dimensiones de la pantalla en términos de mosaicos.
	private final int maxColPantalla = 26;
	private final int maxRenPantalla = 15;
	
	// Dimensiones de la pantalla en píxeles, calculadas a partir de los mosaicos.
	private final int anchoPantalla = this.tamanioTile * this.maxColPantalla;		// 48 * 26 = 1248 píxeles
	private final int altoPantalla = this.tamanioTile * this.maxRenPantalla;		// 48 * 15 = 720 píxeles

	//-------------DIMENSIONES DEL MAPA EN TERMINOS DE MOSAICOS--------------------------
	private final int maxRenMundo = 50; 											// constante de configuración
	private final int maxColMundo = 50; 											// constante de configuración

	// Dimensiones del mundo en píxeles
	private final int anchoMundo = this.tamanioTile * this.maxColMundo;
	private final int altoMundo = this.tamanioTile * this.maxRenMundo;

																					// Fotogramas por segundo (Frames Per Second) a los que se ejecutará el juego.
	private final int FPS = 60; 													// constante de configuración

	// --- COMPONENTES DEL JUEGO ---
	private Thread hebraJuego; 														// El hilo principal
	private final ManejadorTeclas mT = new ManejadorTeclas();
	private final DetectorColisiones dC = new DetectorColisiones(this);
	private final Jugador jugador = new Jugador(this, this.mT);
	private final ArrayList<Enemigo> listaEnemigos = new ArrayList<Enemigo>();	private final ArrayList<Proyectil> listaProjectil= new ArrayList<Proyectil>();		// Lista de proyectiles creados por el jugador
	private final ManejadorTiles mTi = new ManejadorTiles(this); 				//gestiona el mapa.

	/**
	 * Constructor de GamePanel. Configura las propiedades iniciales del panel.
	 */
	public GamePanel() {
		this.setPreferredSize(new Dimension(this.anchoPantalla, this.altoPantalla));
		this.setBackground(Color.BLACK);											// Establece un color de fondo
		this.setDoubleBuffered(true);												// (flickering).
		this.addKeyListener(mT);													// Añade el manejador de teclas como "oyente" de eventos
		this.setFocusable(true);													// Permite que el panel reciba el "foco" del sistema para poder capturar teclas.
	}
	public void configuraEnemigos() {
	    final int NUM_ENEMIGOS_INICIALES = 10; // Puedes cambiar este número para ajustar la cantidad de enemigos

	    for (int i = 0; i < NUM_ENEMIGOS_INICIALES; i++) {
	        listaEnemigos.add(new Enemigo(this));
	    }
	}


	public void iniciaHebraJuego() {
		this.configuraEnemigos();
		this.hebraJuego = new Thread(this);
		this.hebraJuego.start();
	}

	/**
	 * El bucle principal del juego (Game Loop). Se ejecuta en un hilo separado para
	 * controlar la actualización y el redibujado a una velocidad constante (FPS).
	 */
	@Override
	public void run() {
																					// Calcula cada cuánto tiempo debe ocurrir un fotograma en nanosegundos.
		double intervaloDibujo = 1000000000.0 / this.FPS; 							// 1 segundo = 1,000,000,000 nanosegundos.
		double delta = 0; 															// Delta time: acumulador para controlar cuándo actualizar.
		long ultimaVez = System.nanoTime();
		long tiempoActual;

																					// Bucle principal que se ejecuta mientras el hilo del juego exista (no sea null).
		while (this.hebraJuego != null) {
			tiempoActual = System.nanoTime();
			delta += (tiempoActual - ultimaVez) / intervaloDibujo;					// Acumula la proporción de tiempo transcurrido respecto al intervalo de dibujo.
			ultimaVez = tiempoActual;												// Actualiza 'ultimaVez' para el próximo ciclo.

																					// Si ha pasado suficiente tiempo para al menos un fotograma...
			if (delta >= 1) {
				this.update();														// 1. Actualiza la lógica del juego (movimiento, colisiones, IA, etc.).
				this.repaint();														// 2. Vuelve a dibujar todos los elementos en pantalla (llama a paintComponent).
				delta--;															// Reduce el delta en 1, manteniendo cualquier fracción para el siguiente ciclo.
			}
		}
	}

	/**
	 * Actualiza el estado de todos los elementos del juego. Se llama una vez por
	 * cada fotograma desde el bucle del juego.
	 */
	public void update() {
	    this.jugador.update();
	    
	    //ACTUALIZAR ENEMIGOS
	    for (Enemigo e : listaEnemigos) {										// Usa un iterador si planeas eliminar enemigos más adelante por otras razones
	        	e.update(); 														// Llama a la lógica de movimiento/IA para CADA enemigo
	        	if (dC.revisaJugador(e))	        							// Revisar colisión de CADA enemigo con el jugador
	        		this.jugador.recibeDanio(e.getDanio());	            // Si hay colisión, el jugador recibe daño de ESTE enemigo.
	    }

	    // 2. LÓGICA DE PROYECTIL (Colisión proyectil-enemigos)
	    Iterator<Proyectil> iteradorProyectil = listaProjectil.iterator();
	    
	    while (iteradorProyectil.hasNext()) {
	        Proyectil proyectilActual = iteradorProyectil.next();
	        proyectilActual.update();
	        
	        // Bucle ANIDADO para revisar el proyectil contra CADA enemigo
	        Iterator<Enemigo> iteradorEnemigo = listaEnemigos.iterator();
	        boolean proyectilGolpeo = false;

	        while (iteradorEnemigo.hasNext()) {
	            Enemigo enemigoActual = iteradorEnemigo.next();

	            if (dC.revisaEntidad(proyectilActual, enemigoActual)) {
	                // Colisión detectada: el proyectil golpeó a ESTE enemigo.
	                enemigoActual.recibeDanio(proyectilActual.getDanio());
	                proyectilGolpeo = true; // Marca que el proyectil golpeó algo
	                
	                // Revisar si el enemigo muere después del ataque
	                if (enemigoActual.getVidaActual() <= 0) {
	                    // Si el enemigo muere, reiniciamos sus valores para simular una reaparición (re-spawn).
	                    enemigoActual.setPosicionAleatoria(); 
	                    enemigoActual.setVidaActual(enemigoActual.getMaxVida()); 
	                    jugador.setPuntuacion(jugador.getPuntuacion() + 10);
	                }
	                // Si el proyectil solo puede golpear un objetivo, puedes hacer 'break;' aquí
	            }
	        }
	        
	        // Si el proyectil golpeó a algún enemigo, lo removemos de la lista de proyectiles
	        if (proyectilGolpeo || proyectilActual.getVidaActual() <= 0) { // Asumiendo que el proyectil también tiene un tiempo de vida (vida <= 0)
	             iteradorProyectil.remove(); 
	        }
	    }
	}

	/**
	 * Dibuja todos los componentes del juego en el panel. Este método es llamado
	 * automáticamente por Swing cada vez que se invoca repaint(). * @param g El
	 * contexto gráfico proporcionado por Swing para dibujar.
	 */
	@Override
	public void paintComponent(Graphics g) {

	    super.paintComponent(g);
	    Graphics2D g2 = (Graphics2D) g;
	    
	    this.mTi.draw(g2);
	    this.jugador.draw(g2);

	    //DIBUJAR CADA ENEMIGO DE LA LISTA
	    for (Enemigo e : listaEnemigos) 
	        e.draw(g2); // Llama al método de dibujo para CADA enemigo
	    
	    //DIBUJAR CADA PROYECTIL DE LA LISTA
	    for(Proyectil i : listaProjectil)
	        i.draw(g2);

	    g2.dispose();
	}

	// --- GETTERS ---
	// Métodos públicos para que otras clases puedan acceder a las configuraciones
	// del panel.

	/** @return El tamaño final de un mosaico en píxeles. */
	public int getTamanioTile() {
		return this.tamanioTile;
	}

	/** @return El número máximo de filas de mosaicos visibles en pantalla. */
	public int getMaxRenPantalla() {
		return this.maxRenPantalla;
	}

	/** @return El número máximo de columnas de mosaicos visibles en pantalla. */
	public int getMaxColPantalla() {
		return this.maxColPantalla;
	}

	/** @return El ancho total de la pantalla en píxeles. */
	public int getAnchoPantalla() {
		return this.anchoPantalla;
	}

	/** @return El alto total de la pantalla en píxeles. */
	public int getAltoPantalla() {
		return this.altoPantalla;
	}

	/** @return El número máximo de filas de mosaicos en el mundo. */
	public int getMaxRenMundo() {
		return this.maxRenMundo;
	}

	/** @return El número máximo de columnas de mosaicos en el mundo. */
	public int getMaxColMundo() {
		return this.maxColMundo;
	}

	/** @return La instancia del objeto Jugador. */
	public Jugador getJugador() {
		return this.jugador;
	}

	/** @return El ancho total del mundo en píxeles. */
	public int getAnchoMundo() {
		return this.anchoMundo;
	}

	/** @return El alto total del mundo en píxeles. */
	public int getAltoMundo() {
		return this.altoMundo;
	}

	/** @return La instancia del DetectorColisiones. */
	public DetectorColisiones getDetectorColisiones() {
		return this.dC;
	}

	/** @return La instancia del ManejadorTiles. */
	public ManejadorTiles getManejadorTiles() {
		return this.mTi;
	}

	/** @return La lista de proyectiles. */
    public ArrayList<Proyectil> getListaProjectil() {
        return listaProjectil;
    }

}