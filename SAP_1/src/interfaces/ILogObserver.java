package interfaces;

public interface ILogObserver {

    /*
    El observador de registro debe saber cuándo se ha agregado algo al registro. 
    En el sistema, la Vista sirve como LogObserver. La vista quiere saber qué 
    se está agregando al registro para poder actualizar su interfaz de usuario 
    de manera adecuada. El parámetro "enty" representa la nueva cadena que se ha
    añadido al Log.
    */
    public void agregaEntradaLog(String entry);
}
