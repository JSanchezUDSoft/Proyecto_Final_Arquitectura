package interfaces;

public interface IClockObserver {

    /*
    Método llamado para indicar que el reloj del sistema ha cambiado de BAJO a 
    ALTO o de ALTO a BAJO. El reloj del sistema se utiliza para sincronizar 
    todos los componentes del SAP-1.
    */
    public void cambioReloj();

}
