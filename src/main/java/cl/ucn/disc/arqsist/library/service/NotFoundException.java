/**
 * packete base del proyecto
 */
package cl.ucn.disc.arqsist.library.service;

/**
 * clase extencion para enviarm mensajes de error
 */
public class NotFoundException extends RuntimeException{
    /**
     * Constructor básico sin mensaje
     */
    public NotFoundException() {
        super();
    }

    /**
     * Constructor que acepta un mensaje personalizado
     * @param message mensaje a desplegar
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y otra causa
     * @param message mensaje a desplegar
     * @param cause causa que provoco el error
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
