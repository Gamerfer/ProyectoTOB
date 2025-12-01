package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
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
        this.getSpritesEnemigo();
    }


    //Establece los valores iniciales del enemigo (posición y velocidad).
    public void configuracionInicial() {
        this.setPosicionAleatoria();
        this.velocidad = 2;
        this.direccion = "abajo"; 
        
        this.maxVida = 4; // Aumentado a 4. Como el proyectil hace 1 de daño, requiere 4 disparos.
        this.vidaActual = this.maxVida;
        this.danio = 1; // El daño que le hace al jugador (1 vida)
        
        this.hitbox = 48;
        this.offset = (gP.getTamanioTile() - hitbox) / 2;
        this.areaSolida = new Rectangle(offset, offset, hitbox, hitbox);
    }
    
    public void getSpritesEnemigo() {
		try {
			// getClass().getResourceAsStream() es la forma estándar de acceder a recursos dentro del proyecto.
			// Carga sprite para el movimiento
			this.arriba1 = ImageIO.read(getClass().getResourceAsStream("/spritesEnemigo/moverArriba1.png"));
			this.arriba2 = ImageIO.read(getClass().getResourceAsStream("/spritesEnemigo/moverArriba2.png"));
			this.abajo1 = ImageIO.read(getClass().getResourceAsStream("/spritesEnemigo/moverAbajo1.png"));
			this.abajo2 = ImageIO.read(getClass().getResourceAsStream("/spritesEnemigo/moverAbajo2.png"));
			this.izquierda1 = ImageIO.read(getClass().getResourceAsStream("/spritesEnemigo/moverIzquierda1.png"));
			this.izquierda2 = ImageIO.read(getClass().getResourceAsStream("/spritesEnemigo/moverIzquierda2.png"));
			this.derecha1 = ImageIO.read(getClass().getResourceAsStream("/spritesEnemigo/moverDerecha1.png"));
			this.derecha2 = ImageIO.read(getClass().getResourceAsStream("/spritesEnemigo/moverDerecha2.png"));
			this.neutro = ImageIO.read(getClass().getResourceAsStream("/spritesEnemigo/neutral.png"));
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
        
        // Incrementa el contador de sprites para la animación en cada fotograma de movimiento.
        this.contadorSprites++;
        // Lógica para cambiar el fotograma de la animación cada cierto número de updates.
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

//        // 4. Dibuja el rectángulo del enemigo (tal como estaba antes).
//        g2.setColor(Color.RED);
//        g2.fillRect(pantallaX, pantallaY, gP.getTamanioTile(), gP.getTamanioTile());
//
//        //hitbox
//        g2.setColor(Color.BLUE);
//        g2.fillRect(pantallaX, pantallaY, hitbox, hitbox);

        g2.drawImage(sprite, pantallaX, pantallaY, this.gP.getTamanioTile(), this.gP.getTamanioTile(), null); //dibuja 'sprite' en pantalla(x,y) con un tamaño de 48x48
    }
}
