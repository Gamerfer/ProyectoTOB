package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import main.GamePanel;
import java.util.Random;

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
        
        // Define la hitbox. Se utilizan las mismas dimensiones que el Jugador [5].
        // x=8, y=16, ancho=32, alto=32, relativo al tile.
        this.areaSolida = new Rectangle(8, 16, 32, 32); 
        
        this.configuracionInicial();
    }

    /**
     * Establece los valores iniciales del enemigo (posición y velocidad).
     */
    public void configuracionInicial() {
        // Posición inicial del enemigo en el mapa del MUNDO (ajustar según tu mapa).
        this.setPosicionAleatoria();

        // Velocidad de movimiento.
        this.velocidad = 2; 

        this.direccion = "abajo"; 
        
        //vida del enemigo
        this.maxVida = 3;
        this.vidaActual = 3;
    }
    public void setPosicionAleatoria() {
        // 1. Obtiene las dimensiones máximas del mundo en número de tiles
        int colMax = gP.getMaxColMundo(); // Máximo de columnas en el mundo [1]
        int renMax = gP.getMaxRenMundo(); // Máximo de filas en el mundo [1]
        int tamanioTile = gP.getTamanioTile(); // Tamaño de cada tile en píxeles [2]

        // 2. Genera un índice de columna y fila aleatorio.
        int colAleatoria = random.nextInt(colMax);
        int renAleatorio = random.nextInt(renMax);

        // 3. Convierte los índices de tile a coordenadas de píxel en el mundo.
        this.mundoX = colAleatoria * tamanioTile; // Coordenada X del mundo
        this.mundoY = renAleatorio * tamanioTile; // Coordenada Y del mundo
        
        // NOTA: Para una implementación más robusta, aquí se debería verificar
        // que el tile (colAleatoria, renAleatorio) no tenga colisión.
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
    }
}
