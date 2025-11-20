package main;

import javax.swing.JFrame;

//Adios

/**
 * @author Los Ratones
 * @version 0.5
 * @since 11 de julio de 2025, 09:00 horas (horario de la Ciudad de México) *
 *        Clase principal que actúa como punto de entrada del juego. Su única
 *        responsabilidad es crear la ventana principal y añadir el panel del
 *        juego para que se ejecute.
 */
public class RRTheGame {
//hola
	/**
	 * Método principal que la Máquina Virtual de Java ejecuta al iniciar el
	 * programa. Configura y lanza la ventana del juego. * @param args Argumentos de
	 * la línea de comandos (no se utilizan).
	 */
	public static void main(String[] args) {

		// 1. Crear la ventana principal del juego.
		JFrame ventana = new JFrame();

		// 2. Configurar el comportamiento de la ventana.
		// Asegura que el programa termine cuando se cierre la ventana.
		ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Impide que el usuario cambie el tamaño de la ventana.
		ventana.setResizable(false);
		// Establece el título que aparecerá en la barra de la ventana.
		ventana.setTitle("Ratones Reloaded - The Game");

		// 3. Crear una instancia del panel principal del juego, donde ocurre toda la
		// acción.
		GamePanel panelJuego = new GamePanel();
		// Añadir el panel a la ventana para que sea visible.
		ventana.add(panelJuego);

		// 4. Ajustar el tamaño de la ventana al tamaño preferido del panel de juego.
		ventana.pack();

		// 5. Configurar la visualización de la ventana.
		// Centra la ventana en la pantalla (null como referencia significa el centro).
		ventana.setLocationRelativeTo(null);
		// Hace la ventana visible para el usuario.
		ventana.setVisible(true);

		// 6. Iniciar el hilo de ejecución del juego.
		panelJuego.iniciaHebraJuego();
	}
	//jair
}