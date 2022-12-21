package interfaces;

public interface IRAMObserver {

    /*
    Método que le informa a todos los observadores que una dirección de memoria 
    en particular ha cambiado. La dirección se refiere a la posición de la 
    memoria [0, 15] que se ha cambiado. Depende del observador determinar cuál 
    es el nuevo valor.
    */
    public void cambiaValorRAM(int address);
}
