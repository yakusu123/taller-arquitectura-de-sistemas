package cl.ucn.disc.arqsist.library.service;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.model.Reservation;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public final class ReservationService {
    private final ReservationDao reservationDao;
    private final MemberDao memberDao;
    private final LoanDao loanDao;
    private final BookService bookService;
    private final Clock clock;

    public ReservationService(ReservationDao reservationDao, MemberDao memberDao, LoanDao loanDao, BookService bookService, Clock clock) {
        this.reservationDao = reservationDao;
        this.memberDao = memberDao;
        this.loanDao = loanDao;
        this.bookService = bookService;
        this.clock = clock;
    }

    public Reservation reserve(int bookId, int memberId) {
        Book book = bookService.findById(bookId);
        if (book == null) {
            throw new NotFoundException("Book not found with id: " + bookId);
        }

        Member member = memberDao.findById(memberId);
        if (member == null) {
            throw new NotFoundException("Member not found with id: " + memberId);
        }

        LocalDate today = LocalDate.now(clock);
        Reservation reservation = new Reservation(member, book, today);
        reservationDao.create(reservation);
        return reservation;
    }

    public List<Reservation> findAll() {
        return reservationDao.findAll();
    }

    public Loan fulfill(int reservationId) {
        Reservation reservation = reservationDao.findById(reservationId);
        if (reservation == null) {
            throw new NotFoundException("Reservation not found with id: " + reservationId);
        }
        if (reservation.isFulfilled()) {
            throw new IllegalStateException("Reservation already fulfilled: " + reservationId);
        }

        try {
            return reservationDao.transaction(() -> {
                reservation.setFulfilled(true);
                reservationDao.update(reservation);

                bookService.borrow(reservation.getBook().getId());

                LocalDate today = LocalDate.now(clock);
                LocalDate dueDate = LoanPolicy.dueDate(today);

                Loan loan = new Loan(reservation.getMember(), reservation.getBook(), today, dueDate);
                loanDao.create(loan);
                return loan;
            });
        } catch (SQLException e) {
            throw new RuntimeException("Database error during reservation fulfillment", e);
        }
    }
}
