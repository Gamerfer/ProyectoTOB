package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import main.GamePanel;
import main.ManejadorTeclas;

/**
 * @author Los Ratones
 * @version 0.5
 * @since 11 de julio de 2025, 09:00 horas (horario de la Ciudad de México) *
 *        Representa al personaje principal controlado por el usuario. Hereda
 *        las propiedades de la clase Entidad y añade la lógica para responder a
 *        las entradas del teclado y dibujarse en el centro de la pantalla.
 */
public class Jugador extends Entidad {

	private ArrayList<Proyectil> proyectil = new ArrayList<Proyectil>();
	// --- REFERENCIAS A COMPONENTES PRINCIPALES ---
	private final GamePanel gP;
	private final ManejadorTeclas mT;


	private long tInicio = 0;
	private long tiempo = 0;
	private int rango;
	private int retDisparo;

	/* --- COORDENADAS EN PANTALLA ---
	// Coordenadas FINALES y FIJAS del jugador en la PANTALLA.
	// El jugador siempre se dibuja en el centro; es el mapa el que se mueve.*/
	private final int pantallaX;
	private final int pantallaY;

	public Jugador(GamePanel gP, ManejadorTeclas mT) {
		this.gP = gP;
		this.mT = mT;

	
		// Se resta la mitad del tamaño del mosaico para que el CENTRO del jugador quede en el centro de la pantalla.
		this.pantallaX = gP.getAnchoPantalla() / 2 - (gP.getTamanioTile() / 2);		// Calcula la posición X central en la pantalla.
		this.pantallaY = gP.getAltoPantalla() / 2 - (gP.getTamanioTile() / 2);		// Calcula la posición Y central en la pantalla.
		this.areaSolida = new Rectangle(8, 16, 32, 32);								// Define el área sólida (hitbox) del jugador. new Rectangle(x, y, ancho, alto) relativo a la esquina superior izquierda del sprite.

		// Llama a los métodos para establecer los valores iniciales y cargar los gráficos.
		this.configuracionInicial(); 												// Establece la posición, velocidad, etc.
		this.getSpritesJugador(); 													// Carga las imágenes del personaje.
	}


	 //Establece los valores por defecto del jugador al iniciar el juego.
	public void configuracionInicial() {

		this.mundoX = gP.getTamanioTile() * 22;		// Posición inicial del jugador en el mapa del MUNDO (coordenadas X).
		this.mundoY = gP.getTamanioTile() * 30;		// Posición inicial del jugador en el mapa del MUNDO (coordenadas Y).
		this.velocidad = 4;							// Velocidad de movimiento del jugador en píxeles por fotograma.
		this.direccion = "abajo";					// Dirección inicial a la que mira el jugador.
		this.rango = 75;								// Que tan lejos va a llegar el proyectil (Todavia no implementado)
		this.retDisparo = 10;						// Es el retardo del disparo
		
		//inicializar vida
		this.maxVida = 10;
		this.vidaActual = 10;
	}


	//Carga las imágenes (sprites) del jugador desde la carpeta de recursos.
	public void getSpritesJugador() {
		try {
			// getClass().getResourceAsStream() es la forma estándar de acceder a recursos dentro del proyecto.
			// Carga sprite para el movimiento
			this.arriba1 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverArriba1.png"));
			this.arriba2 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverArriba2.png"));
			this.abajo1 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverAbajo1.png"));
			this.abajo2 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverAbajo2.png"));
			this.izquierda1 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverIzquierda1.png"));
			this.izquierda2 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverIzquierda2.png"));
			this.derecha1 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverDerecha1.png"));
			this.derecha2 = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/moverDerecha2.png"));
			this.neutro = ImageIO.read(getClass().getResourceAsStream("/spritesjugador/neutral.png"));
		} catch (IOException e) {
			// Si ocurre un error al cargar las imágenes (ej: archivo no encontrado), se imprime el error.
			e.printStackTrace();
		}
	}

	/**
	 * Actualiza el estado del jugador en cada fotograma. Gestiona el movimiento y
	 * la animación.
	 */
	public void update() {
		
		
		// Solo procesa el movimiento y la animación si alguna tecla de dirección está pulsada.
		if (mT.getTeclaArriba() || mT.getTeclaAbajo() || mT.getTeclaIzquierda() || mT.getTeclaDerecha()) {

			// Comprueba la tecla pulsada y establece la dirección correspondiente.
			if 		(this.mT.getTeclaArriba()) {this.direccion = "arriba";} 											// Establece la dirección a "arriba". El movimiento se gestiona más adelante.
			else if (this.mT.getTeclaAbajo()) {this.direccion = "abajo";}
			else if (this.mT.getTeclaIzquierda()) {this.direccion = "izquierda";}
			else if (this.mT.getTeclaDerecha()) {this.direccion = "derecha";}
			
			// Reinicia la bandera de colisión antes de cada comprobación.
			this.colisionActivada = false;
			// Llama al detector de colisiones para que revise si hay un obstáculo en la
			// dirección actual.
			this.gP.getDetectorColisiones().revisaTile(this);

			// Si la bandera de colisión NO fue activada por el detector...
			if (this.colisionActivada == false) {
				// ... permite que el jugador se mueva.
				switch (this.getDireccion()) {
				case "arriba":
					// Mueve al jugador hacia arriba en el mapa del mundo.
					this.setMundoY(this.getMundoY() - this.getVelocidad());
					break;
				case "abajo":
					this.setMundoY(this.getMundoY() + this.getVelocidad());
					break;
				case "izquierda":
					this.setMundoX(this.getMundoX() - this.getVelocidad());
					break;
				case "derecha":
					this.setMundoX(this.getMundoX() + this.getVelocidad());
					break;

				default:
					break; // Caso por defecto, no hace nada.
				}
			}


			// Incrementa el contador de sprites para la animación en cada fotograma de
			// movimiento.
			this.contadorSprites++;
			// Lógica para cambiar el fotograma de la animación cada cierto número de
			// updates.
			if (this.contadorSprites > this.cambiaSprite) {
				// Si el sprite actual es el 1, cambia al 2.
				if (this.numeroSprites == 1) {
					this.numeroSprites = 2;
				} else { // Si es el 2 (o cualquier otro valor), vuelve al 1.
					this.numeroSprites = 1;
				}
				// Reinicia el contador para el próximo cambio de sprite.
				this.contadorSprites = 0;
			}
		}

		
		if(tiempo < 200)																						//Cuenta el tiempo mientras no se pase de 200 (200 para que no sume para siempre)
			tiempo++;
		
		if (mT.getDisparo() && tiempo > retDisparo){		 													// si se presiona la tecla de disparo y cuando ya haya pasado el tiempo de recarga del disparo.
			tInicio = System.currentTimeMillis();																//Se marca el tiempo en el que se añadio el nuevo proyectil
			gP.getListaProjectil().add(new Proyectil(this.gP, this.mT));										//Se añade una nueva instancia de un proyectil a la lista de gamePanel
			tiempo = 0;																							//resetea el tiempo para disparar otra vez
		}
		
		//Si la lista no esta vacia, revisa si es que el timer no es mayor que el rango de tiempo para que el proyectil desaparezca
		if(!gP.getListaProjectil().isEmpty())
			for(int i=0; i<gP.getListaProjectil().size(); i++)
				if(gP.getListaProjectil().get(i).getTimer() > rango)
					gP.getListaProjectil().remove(i);
		
//		if(!gP.getListaProjectil().isEmpty() && System.currentTimeMillis() - tInicio > 500) {
//			gP.getListaProjectil().removeFirst();																//Remueve los proyectiles una vez pasen 500 milisegundos
//			tInicio = 0;																						//Se reinicia el tiempo para cuando desaparescan
//		}
		//+++++++PROBLEMA: se reinicia el tiempo para cada proyectil cada vez que se dispara!!!++++++++++
		
		

	}

	/**
	 * Dibuja al jugador en la pantalla. * @param g2 El contexto gráfico
	 * {@link Graphics2D} para dibujar.
	 */
	public void draw(Graphics2D g2) {
		BufferedImage sprite = null;	// Declara una variable para almacenar el sprite que se va a dibujar.

		// Selecciona el sprite correcto basado en la dirección y el número de sprite actual.
		switch (this.direccion) {
		case "arriba":
			// Usa el operador ternario para elegir entre el sprite 1 y 2 de "arriba".
			sprite = (this.numeroSprites == 1) ? this.arriba1 : this.arriba2;
			break;
		case "abajo":
			sprite = (this.numeroSprites == 1) ? this.abajo1 : this.abajo2;
			break;
		case "izquierda":
			sprite = (this.numeroSprites == 1) ? this.izquierda1 : this.izquierda2;
			break;
		case "derecha":
			sprite = (this.numeroSprites == 1) ? this.derecha1 : this.derecha2;
			break;
		}
		// --- LÓGICA DE DIBUJO DE LA BARRA DE VIDA (NUEVO) ---

	    // El jugador se dibuja en coordenadas de pantalla fijas (pantallaX, pantallaY) [12, 13].
	    int anchoBarra = gP.getTamanioTile();
	    int altoBarra = 5;
	    int offsetY = 10;

	    int barraX = this.getPantallaX(); // [6]
	    int barraY = this.getPantallaY() - offsetY; // [6]

	    // 1. Fondo de la barra (Negro)
	    g2.setColor(Color.BLACK);
	    g2.fillRect(barraX, barraY, anchoBarra, altoBarra);

	    // 2. Calcula el ancho de la vida actual.
	    double proporcionVida = (double) this.vidaActual / this.maxVida;
	    int vidaActualAncho = (int) (proporcionVida * anchoBarra);

	    // 3. Dibuja la vida actual (Verde).
	    g2.setColor(Color.GREEN);
	    g2.fillRect(barraX, barraY, vidaActualAncho, altoBarra);

		// Dibuja el sprite seleccionado en las coordenadas FIJAS de la pantalla.
		// El jugador siempre está en el centro; el mapa se mueve a su alrededor.
		g2.drawString(String.valueOf(System.currentTimeMillis()), 200, 100);						//(Debug) Tiempo de ejecucion
		g2.drawString(String.valueOf(System.currentTimeMillis() - tInicio), 200, 120);
		g2.drawString(String.valueOf(tiempo), 200, 140);
		g2.drawImage(sprite, this.pantallaX, this.pantallaY, this.gP.getTamanioTile(), this.gP.getTamanioTile(), null); //dibuja 'sprite' en pantalla(x,y) con un tamaño de 48x48
	}

	//================================================================
	public void recibeDanio(int danio) {
	    this.vidaActual -= danio;
	    if (this.vidaActual < 0) {
	        this.vidaActual = 0;
	    }
	}
	
	
	// --- GETTERS Y SETTERS ---
	// Proporcionan acceso controlado a las propiedades del jugador.

	/** @return La coordenada X del jugador en el mundo. */
	public int getMundoX() {
		return this.mundoX;
	}

	/** @param valor La nueva coordenada X del jugador en el mundo. */
	public void setMundoX(int valor) {
		this.mundoX = valor;
	}

	/** @return La coordenada Y del jugador en el mundo. */
	public int getMundoY() {
		return this.mundoY;
	}

	/** @param valor La nueva coordenada Y del jugador en el mundo. */
	public void setMundoY(int valor) {
		this.mundoY = valor;
	}

	/** @return La velocidad de movimiento del jugador. */
	public int getVelocidad() {
		return this.velocidad;
	}

	/** @return La coordenada X fija del jugador en la pantalla. */
	public int getPantallaX() {
		return this.pantallaX;
	}

	/** @return La coordenada Y fija del jugador en la pantalla. */
	public int getPantallaY() {
		return this.pantallaY;
	}

	/** @return La coordenada X del área sólida (hitbox) relativa al sprite. */
	public int getAreaSolidaX() {
		return this.areaSolida.x;
	}

	/** @return La coordenada Y del área sólida (hitbox) relativa al sprite. */
	public int getAreaSolidaY() {
		return this.areaSolida.y;
	}

	/** @return El ancho del área sólida (hitbox). */
	public int getAreaSolidaAncho() {
		return this.areaSolida.width;
	}

	/** @return El alto del área sólida (hitbox). */
	public int getAreaSolidaAlto() {
		return this.areaSolida.height;
	}

	/** @return La dirección actual del jugador como un String. */
	public String getDireccion() {
		return this.direccion;
	}
}