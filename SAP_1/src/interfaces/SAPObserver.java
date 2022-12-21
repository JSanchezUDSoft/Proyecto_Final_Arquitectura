package interfaces;

public interface SAPObserver {
	// Indica que el valor del Registro A ha cambiado con un nuevo valor v 
	public void cambioRegistroA(byte v);

	// Indica que el valor del Registro B ha cambiado con un nuevo valor v 
	public void cambioRegistroB(byte v);

	// Indica que el valor del Contador de Programa PC ha cambiado con un nuevo valor v 
	public void cambioPC(byte v);

	// Indica que el valor del Registro de direccionamiento de Memoria MAR ha cambiado con un nuevo valor v 
	public void cambioMAR(byte v);

	// Indica que el valor del Registro de Salida ha cambiado con un nuevo valor v 
	public void cambioOUT(byte v);

	// Indica que el valor del Registro de Instruccion IR ha cambiado con un nuevo valor v 
	public void cambioIR(byte v);

	// Indica que el valor del ciclo ha cambiado con un nuevo valor v 
	public void cambioConteoPaso(byte v);

	// Indica que el valor del Registro de Estados FLAGS ha cambiado con un nuevo valor v 
	public void cambioFLAGS();

	// Indica que el valor del BUS ha cambiado con un nuevo valor v 
	public void cambioBUS(byte v);

	// Indica que una o más líneas de control han cambiado
	public void cambioLineasControl();
}
