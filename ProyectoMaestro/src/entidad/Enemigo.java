package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;

import main.GamePanel;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * Representa a un enemigo básico que sigue al jugador. 
 * Hereda propiedades y métodos de la superclase Entidad.
 */
public class Enemigo extends Entidad {

    // Referencia al panel principal del juego.
    private final GamePanel gP;
    private final Random random = new Random();

    /**
     * Constructor del Enemigo.
     * @param gP Referencia al GamePanel principal.
     */
    public Enemigo(GamePanel gP) {
        // Asigna la referencia del GamePanel principal.
        this.gP = gP;
        this.configuracionInicial();
    }


    //Establece los valores iniciales del enemigo (posición y velocidad).
    public void configuracionInicial() {
        // Posición inicial del enemigo en el mapa del MUNDO (ajustar según tu mapa).
        this.setPosicionAleatoria();

        // Velocidad de movimiento.
        this.velocidad = 0;
        this.direccion = "abajo"; 
        
        //vida del enemigo
        this.maxVida = 1;
        this.vidaActual = this.maxVida;
        this.danio = 1;
        
        //colisiones
        this.hitbox = 48;
		this.offset = (gP.getTamanioTile() - hitbox) / 2;
		this.areaSolida = new Rectangle(offset, offset, hitbox, hitbox);
        
    }
    
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
    
    public void setPosicionAleatoria() {
        // 1. Obtiene las dimensiones máximas del mundo en número de tiles
        int colMax = gP.getMaxColMundo(); // Máximo de columnas en el mundo [1]
        int renMax = gP.getMaxRenMundo(); // Máximo de filas en el mundo [1]
        int tamanioTile = gP.getTamanioTile(); // Tamaño de cada tile en píxeles [2]
        int colAleatoria;
        int renAleatorio;
        boolean estaColision;

        do {
            // 2. Genera un índice de columna y fila aleatorio.
            colAleatoria = random.nextInt(colMax);
            renAleatorio = random.nextInt(renMax);

            // 3. Convierte los índices de tile a coordenadas de píxel en el mundo.
            this.mundoX = colAleatoria * tamanioTile; // Coordenada X del mundo
            this.mundoY = renAleatorio * tamanioTile; // Coordenada Y del mundo
            
            estaColision = gP.getManejadorTiles().getColisionDeTile( gP.getManejadorTiles().getCodigoMapaTiles(renAleatorio, colAleatoria));
            System.out.println(estaColision);
        } while(estaColision);
        System.out.println("Terminar de generar\t" + colAleatoria +", " + renAleatorio);
    }

    /**
     * Define la lógica del enemigo, incluyendo IA de seguimiento y manejo de colisiones.
     * Sobrescribe el método base en Entidad.
     */
    public void update() {
        // 1. Lógica de IA: Determinar la dirección de seguimiento
        this.establecerDireccionDeSeguimiento(); 

        // 2. Manejo de Colisiones
        this.colisionActivada = false; 
        // Llama al detector de colisiones.
        this.gP.getDetectorColisiones().revisaTile(this); 

        // 3. Aplicar movimiento si no hay colisión
        if (this.colisionActivada == false) {
            // Mueve el enemigo basado en la dirección calculada (usando atributos heredados [2]).
            switch (this.getDireccion()) {
                case "arriba":
                    this.mundoY -= this.velocidad; 
                    break;
                case "abajo":
                    this.mundoY += this.velocidad;
                    break;
                case "izquierda":
                    this.mundoX -= this.velocidad;
                    break;
                case "derecha":
                    this.mundoX += this.velocidad;
                    break;
            }
        }
    }

    /**
     * Implementa una IA simple para que el enemigo se mueva hacia el jugador.
     * Establece la direccion basado en la distancia de los ejes 'x' y 'y' con respecto al jugador
     */
    private void establecerDireccionDeSeguimiento() {
        // Obtener la posición del jugador (Jugador.getMundoX() y Jugador.getMundoY()) [6].
        int jugadorX = gP.getJugador().getMundoX(); 
        int jugadorY = gP.getJugador().getMundoY(); 

        int distanciaX = jugadorX - this.mundoX;
        int distanciaY = jugadorY - this.mundoY;

        // Determina la dirección priorizando el eje con mayor distancia.
        if (Math.abs(distanciaX) > Math.abs(distanciaY)) {
            if (distanciaX < 0) {
                this.direccion = "izquierda";
            } else {
                this.direccion = "derecha";
            }
        } 
        else if (Math.abs(distanciaY) > Math.abs(distanciaX)) {
            if (distanciaY < 0) {
                this.direccion = "arriba";
            } else {
                this.direccion = "abajo";
            }
        } 
        else { // Si las distancias son iguales
             if (distanciaX < 0) {
                this.direccion = "izquierda";
            } else if (distanciaX > 0) {
                this.direccion = "derecha";
            }
        }
    }
    
    
    //=====================================================================
    public void recibeDanio(int danio) {
        this.vidaActual -= danio;
    }
    //===============================================

    /**
     * Dibuja el enemigo en la pantalla como un rectángulo rojo.
     * Sobrescribe el método base en Entidad.
     */
    public void draw(Graphics2D g2) {
        // Cálculo de la posición en pantalla, relativo a la cámara/jugador [7].
        int pantallaX = this.mundoX - gP.getJugador().getMundoX() + gP.getJugador().getPantallaX();
        int pantallaY = this.mundoY - gP.getJugador().getMundoY() + gP.getJugador().getPantallaY();

     // --- DIBUJO DE LA BARRA DE VIDA ---

        int anchoBarra = gP.getTamanioTile(); 
        int altoBarra = 5; 
        int offsetY = 10; 
        
        int barraX = pantallaX; 
        int barraY = pantallaY - offsetY; 
        
        // 1. Fondo de la barra (Negro)
        g2.setColor(Color.BLACK);
        g2.fillRect(barraX, barraY, anchoBarra, altoBarra);
        
        // 2. Calcula el ancho de la vida actual.
        double proporcionVida = (double) this.vidaActual / this.maxVida;
        int vidaActualAncho = (int) (proporcionVida * anchoBarra);
        
        // 3. Dibuja la vida actual (Rojo).
        g2.setColor(Color.RED);
        g2.fillRect(barraX, barraY, vidaActualAncho, altoBarra);

        // 4. Dibuja el rectángulo del enemigo (tal como estaba antes).
        g2.setColor(Color.RED); 
        g2.fillRect(pantallaX, pantallaY, gP.getTamanioTile(), gP.getTamanioTile()); 
        
        //hitbox
        g2.setColor(Color.BLUE); 
        g2.fillRect(pantallaX, pantallaY, hitbox, hitbox); 
    }
}
