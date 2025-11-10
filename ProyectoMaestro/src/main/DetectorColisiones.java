package main;

import entidad.Entidad;
import entidad.Jugador; 

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
        
        // ¡SE ELIMINA LA VERIFICACIÓN 'if (entidad instanceof Jugador)'!
        // Y se usan SÓLO los getters de Entidad, eliminando los casts que causan el error.

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

        if (this.gP.getManejadorTiles().getColisionDeTile(numTile1)
                || this.gP.getManejadorTiles().getColisionDeTile(numTile2)) {
            // Usa el setter de la superclase Entidad.
            entidad.setColisionActivada(true); 
        }
    }
}