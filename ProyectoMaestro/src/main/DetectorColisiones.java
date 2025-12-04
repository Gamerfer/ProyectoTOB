package main;

import java.awt.Rectangle;

import entidad.Entidad;
import entidad.Jugador; 
import entidad.Jefe;

/**
 * Clase encargada de gestionar y verificar las colisiones entre las
 * entidades y los mosaicos del mapa.
 */
public class DetectorColisiones {

    private GamePanel gP; 

    public DetectorColisiones(GamePanel gP) {
        this.gP = gP;
    }

    /**
     * Revisa si la entidad colisionará con un mosaico sólido en su próxima
     * posición. Refactorizado para funcionar con cualquier Entidad (Jugador, Enemigo, etc.).
     * @param entidad La Entidad que se va a verificar.
     */
    public void revisaTile(Entidad entidad) {
        

        // Calcula las coordenadas del área de colisión de la entidad en el mundo, 
        // usando los getters de la superclase Entidad.
        int izquierdaEntidadMundoX = entidad.getMundoX() + entidad.getAreaSolidaX();
        int derechaEntidadMundoX = izquierdaEntidadMundoX + entidad.getAreaSolidaAncho();
        int arribaEntidadMundoY = entidad.getMundoY() + entidad.getAreaSolidaY();
        int abajoEntidadMundoY = arribaEntidadMundoY + entidad.getAreaSolidaAlto();

        // Conversión de coordenadas del mundo a columnas y filas del mapa:
        int colIzquierdaEntidad = izquierdaEntidadMundoX / this.gP.getTamanioTile();
        int colDerechaEntidad = derechaEntidadMundoX / this.gP.getTamanioTile();
        int renArribaEntidad = arribaEntidadMundoY / this.gP.getTamanioTile();
        int renAbajoEntidad = abajoEntidadMundoY / this.gP.getTamanioTile();

        int numTile1 = 0, numTile2 = 0;

        // Utiliza el getter de Entidad para la dirección, velocidad, etc.
        switch (entidad.getDireccion()) {
            case "arriba":
                renArribaEntidad = (arribaEntidadMundoY - entidad.getVelocidad()) / this.gP.getTamanioTile();
                numTile1 = this.gP.getManejadorTiles().getCodigoMapaTiles(renArribaEntidad, colIzquierdaEntidad);
                numTile2 = this.gP.getManejadorTiles().getCodigoMapaTiles(renArribaEntidad, colDerechaEntidad);
                break;
            case "abajo":
                renAbajoEntidad = (abajoEntidadMundoY + entidad.getVelocidad()) / this.gP.getTamanioTile();
                numTile1 = this.gP.getManejadorTiles().getCodigoMapaTiles(renAbajoEntidad, colIzquierdaEntidad);
                numTile2 = this.gP.getManejadorTiles().getCodigoMapaTiles(renAbajoEntidad, colDerechaEntidad);
                break;
            case "izquierda":
                colIzquierdaEntidad = (izquierdaEntidadMundoX - entidad.getVelocidad()) / this.gP.getTamanioTile();
                numTile1 = this.gP.getManejadorTiles().getCodigoMapaTiles(renArribaEntidad, colIzquierdaEntidad);
                numTile2 = this.gP.getManejadorTiles().getCodigoMapaTiles(renAbajoEntidad, colIzquierdaEntidad);
                break;
            case "derecha":
                colDerechaEntidad = (derechaEntidadMundoX + entidad.getVelocidad()) / this.gP.getTamanioTile();
                numTile1 = this.gP.getManejadorTiles().getCodigoMapaTiles(renArribaEntidad, colDerechaEntidad);
                numTile2 = this.gP.getManejadorTiles().getCodigoMapaTiles(renAbajoEntidad, colDerechaEntidad);
                break;
            default:
                break;
        }
        if (entidad instanceof Jefe) {
            if (numTile1 == 0) return;  
            if (numTile2 == 0) return;
        }

        if (this.gP.getManejadorTiles().getColisionDeTile(numTile1)
                || this.gP.getManejadorTiles().getColisionDeTile(numTile2)) {
            // Usa el setter de la superclase Entidad.
            entidad.setColisionActivada(true); 
        }
    }
    
    
    //===============================================================================================
    /**
    * Revisa si una entidad colisiona con otra entidad (usando las coordenadas del mundo).
    * @param entidad1 La primera entidad (ej: proyectil).
    * @param entidad2 La segunda entidad (ej: enemigo).
    * @return true si hay colisión, false en caso contrario.
    */
    public boolean revisaEntidad(Entidad entidad1, Entidad entidad2) {
        // Crear Rectangles de colisión en coordenadas del mundo
        java.awt.Rectangle area1 = new java.awt.Rectangle(
            entidad1.getMundoX(),
            entidad1.getMundoY(),
            entidad1.getAreaSolidaAncho(),
            entidad1.getAreaSolidaAlto()
        );
        

        java.awt.Rectangle area2 = new java.awt.Rectangle(
            entidad2.getMundoX(),
            entidad2.getMundoY(),
            entidad2.getAreaSolidaAncho(),
            entidad2.getAreaSolidaAlto()
        );

        // Verificar si intersectan
        return area1.intersects(area2);
    }
    /**
    * Revisa si una entidad (ej: Enemigo) colisiona con el Jugador.
    * @param entidad La entidad que se va a verificar (generalmente el enemigo).
    * @return true si hay colisión, false en caso contrario.
    */
    public boolean revisaJugador(Entidad entidad) {
        // 1. Obtener el área de colisión del Enemigo en coordenadas del mundo.
        Rectangle areaEntidad = new Rectangle(
            entidad.getMundoX() + entidad.getAreaSolidaX(),
            entidad.getMundoY() + entidad.getAreaSolidaY(),
            entidad.getAreaSolidaAncho(),
            entidad.getAreaSolidaAlto()
        );

        // 2. Obtener el área de colisión del Jugador en coordenadas del mundo.
        Jugador jugador = gP.getJugador();
        Rectangle areaJugador = new Rectangle(
            jugador.getMundoX() + jugador.getAreaSolidaX(),
            jugador.getMundoY() + jugador.getAreaSolidaY(),
            jugador.getAreaSolidaAncho(),
            jugador.getAreaSolidaAlto()
        );

        // 3. Verificar si intersectan.
        return areaEntidad.intersects(areaJugador);
    }
}