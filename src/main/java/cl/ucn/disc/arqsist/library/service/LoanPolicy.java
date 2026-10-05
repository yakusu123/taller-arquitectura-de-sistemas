package cl.ucn.disc.arqsist.library.service;

import java.time.LocalDate;

/**
 * Politica de prestamo y tarifas.
 */
public final class LoanPolicy {

    /**
     * Tiempo estandar de prestamo en dias.
     */
    public static final int DUE_DAYS = 21;

    /**
     * Tarifa de multa por dia de retraso.
     */
    public static final double FEE_PER_DAY = 1.0;

    /**
     * Constructor privado.
     */
    private LoanPolicy() {
    }

    /**
     * Calcula la fecha de vencimiento del prestamo.
     * @param loanDate dia inicial del prestamo
     * @return la fecha de entrega limite
     */
    public static LocalDate dueDate(LocalDate loanDate) {
        return loanDate.plusDays(DUE_DAYS);
    }
}
