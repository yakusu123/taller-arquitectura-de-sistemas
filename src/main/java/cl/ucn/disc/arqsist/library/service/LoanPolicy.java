package cl.ucn.disc.arqsist.library.service;

import java.time.LocalDate;

/**
 * politica de prestamo
 */
public final class LoanPolicy {
    /**
     * constructor
     */
    private LoanPolicy() {
    }

    /**
     * caluculo de multa por dias de retraso
     * @param loanDate dia inicial del prestamo
     * @return la fecha de entrega despues del maximo de prestamo
     */
    public static LocalDate fine(LocalDate loanDate) {
        return loanDate.plusDays(dueDate);
    }

    /**
     * tiempo de prestamo
     */
    public static final int dueDate = 21;
    /**
     * multiplicador de multa de prestamo por dia,
     */
    public static final double dailyFine = 1.0;
}
