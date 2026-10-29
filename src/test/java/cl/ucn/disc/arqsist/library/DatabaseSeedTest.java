package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.model.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseSeedTest {
    private Database db;
    private BookDao bookDao;
    private MemberDao memberDao;
    private LoanDao loanDao;
    private ReservationDao reservationDao;
    @BeforeEach
    void setUp() throws Exception {
        db = new Database("jdbc:sqlite::memory:");
        bookDao = new BookDao(db.connectionSource());
        memberDao = new MemberDao(db.connectionSource());
        loanDao = new LoanDao(db.connectionSource());
        reservationDao = new ReservationDao(db.connectionSource());
    }
    @Test
    void seedIfEmptyPopulatesDataCorrectlyAndIsIdempotent() throws Exception {
        db.seedIfEmpty();
        assertEquals(3, bookDao.findAll().size(), "Should seed 3 books");
        assertEquals(3, memberDao.findAll().size(), "Should seed 3 members");
        assertEquals(1, reservationDao.findAll().size(), "Should seed 1 reservation");
        List<Loan> loans = loanDao.findAll();
        assertEquals(3, loans.size(), "Should seed 3 loans");
        long returnedCount = loans.stream().filter(Loan::isReturned).count();
        long activeCount = loans.stream().filter(l -> !l.isReturned()).count();
        assertEquals(1, returnedCount, "Should have 1 returned loan");
        assertEquals(2, activeCount, "Should have 2 open/active loans");

        db.seedIfEmpty();
        assertEquals(3, bookDao.findAll().size(), "Books count should not change");
        assertEquals(3, memberDao.findAll().size(), "Members count should not change");
        assertEquals(3, loanDao.findAll().size(), "Loans count should not change");
        assertEquals(1, reservationDao.findAll().size(), "Reservations count should not change");
    }
}
