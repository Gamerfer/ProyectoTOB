package entidad;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.ManejadorTeclas;

public class Proyectil extends Entidad{
	private ManejadorTeclas mT;
	private GamePanel gP;


	public Proyectil(GamePanel gP, ManejadorTeclas mT) {
		this.gP = gP;
		this.mT = mT;
		
		configuracionInicial();

	}
	
	public void configuracionInicial() {

		this.mundoX = gP.getJugador().getMundoX();		// Posición inicial del jugador en el mapa del MUNDO (coordenadas X).
		this.mundoY = gP.getJugador().getMundoY();		// Posición inicial del jugador en el mapa del MUNDO (coordenadas Y).
		this.velocidad = 7;								// Velocidad de movimiento del jugador en píxeles por fotograma.
		this.direccion = gP.getJugador().getDireccion();					// Dirección inicial a la que mira el jugador.
		
		//inicializar vida
		this.maxVida = 10;
		this.vidaActual = 10;
	}
	
	
	public void getSpritesProyectil() {
		try {
			// getClass().getResourceAsStream() es la forma estándar de acceder a recursos dentro del proyecto.
			// Carga sprite para el movimiento
			this.arriba1 = ImageIO.read(getClass().getResourceAsStream("/spritesproyectil/proyectil.png"));
			this.arriba2 = ImageIO.read(getClass().getResourceAsStream("/spritesproyectil/proyectil.png"));
			this.abajo1 = ImageIO.read(getClass().getResourceAsStream("/spritesproyectil/proyectil.png"));
			this.abajo2 = ImageIO.read(getClass().getResourceAsStream("/spritesproyectil/proyectil.png"));
			this.izquierda1 = ImageIO.read(getClass().getResourceAsStream("/spritesproyectil/proyectil.png"));
			this.izquierda2 = ImageIO.read(getClass().getResourceAsStream("/spritesproyectil/proyectil.png"));
			this.derecha1 = ImageIO.read(getClass().getResourceAsStream("/spritesproyectil/proyectil.png"));
			this.derecha2 = ImageIO.read(getClass().getResourceAsStream("/spritesproyectil/proyectil.png"));
		} catch (IOException e) {
			// Si ocurre un error al cargar las imágenes (ej: archivo no encontrado), se imprime el error.
			e.printStackTrace();
		}
	}
	
	

	public void update() {
		switch (this.getDireccion()) {
			case "arriba":
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
				break; 
		}

		this.contadorSprites++;
		if (this.contadorSprites > this.cambiaSprite) {
			if (this.numeroSprites == 1) {this.numeroSprites = 2;} 
			
			else {this.numeroSprites = 1;}
			this.contadorSprites = 0;
		}
	}

	public void draw(Graphics2D g2) {
		BufferedImage sprite = null;	// Declara una variable para almacenar el sprite que se va a dibujar.

		int pantallaX = this.mundoX - gP.getJugador().getMundoX() + gP.getJugador().getPantallaX();
		int pantallaY = this.mundoY - gP.getJugador().getMundoY() + gP.getJugador().getPantallaY();

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
		
		sprite = this.arriba1;

		g2.setColor(Color.YELLOW);
		g2.fillRect(pantallaX, pantallaY, gP.getTamanioTile(), gP.getTamanioTile());
		//PROBLEMA: No se porque no quiere cargar el sprite.
		//g2.drawImage(sprite, 200, 100, this.gP.getTamanioTile(), this.gP.getTamanioTile(), null);
		g2.drawString(String.valueOf(gP.getListaProjectil().size()), 100, 100); 					//(Debug)Numero de proyectiles en pantalla
	}
	
	
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
}
