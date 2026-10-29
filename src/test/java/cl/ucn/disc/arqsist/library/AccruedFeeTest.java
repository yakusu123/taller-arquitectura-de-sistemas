package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.service.BookService;
import cl.ucn.disc.arqsist.library.service.LoanPolicy;
import cl.ucn.disc.arqsist.library.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccruedFeeTest {

    private LoanDao loanDao;
    private LoanService loanService;
    private Member member;
    private Book book;
    private Clock fixedClock;

    @BeforeEach
    void setUp() throws Exception {
        Database db = new Database("jdbc:sqlite::memory:");
        BookDao bookDao = new BookDao(db.connectionSource());
        MemberDao memberDao = new MemberDao(db.connectionSource());
        loanDao = new LoanDao(db.connectionSource());
        fixedClock = Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZoneId.of("America/Santiago"));
        BookService bookService = new BookService(bookDao);
        loanService = new LoanService(loanDao, memberDao, bookService, fixedClock);
        book = new Book("Clean Code", "Robert C. Martin", "9780132350884", 5);
        bookDao.create(book);
        member = new Member("Ada Lovelace", "ada@example.com");
        memberDao.create(member);
    }

    @Test
    void accruedFeeCalculatesCorrectlyForOverdueAndActiveLoans() throws Exception {
        LocalDate today = LocalDate.now(fixedClock);
        LocalDate overdueLoanDate = today.minusDays(45);
        LocalDate overdueDueDate = LoanPolicy.dueDate(overdueLoanDate); // vence hace 24 dias
        Loan overdueLoan = new Loan(member, book, overdueLoanDate, overdueDueDate);
        loanDao.create(overdueLoan);
        LocalDate activeLoanDate = today.minusDays(5);
        LocalDate activeDueDate = LoanPolicy.dueDate(activeLoanDate);
        Loan activeLoan = new Loan(member, book, activeLoanDate, activeDueDate);
        loanDao.create(activeLoan);

        assertEquals(24.0, loanService.accruedFee(overdueLoan), "Overdue loan should accrue $24.0");
        assertEquals(0.0, loanService.accruedFee(activeLoan), "Active loan should accrue $0.0");
        List<Loan> loans = loanService.findAll();
        Loan foundOverdue = loans.stream().filter(l -> l.getId() == overdueLoan.getId()).findFirst().orElseThrow();
        Loan foundActive = loans.stream().filter(l -> l.getId() == activeLoan.getId()).findFirst().orElseThrow();
        assertEquals(24.0, foundOverdue.getOverdueFee(), "findAll should assign accrued fee of 24.0");
        assertEquals(0.0, foundActive.getOverdueFee(), "findAll should assign accrued fee of 0.0");
    }
}
