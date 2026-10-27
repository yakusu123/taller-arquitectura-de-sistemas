package cl.ucn.disc.arqsist.library.service;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public final class LoanService {
    private final LoanDao loanDao;
    private final MemberDao memberDao;
    private final BookService bookService;
    private final Clock clock;

    public LoanService(LoanDao loanDao, MemberDao memberDao, BookService bookService, Clock clock) {
        this.loanDao = loanDao;
        this.memberDao = memberDao;
        this.bookService = bookService;
        this.clock = clock;
    }

    public Loan checkout(int memberId, int bookId) {
        try {
            return loanDao.transaction(() -> {
                bookService.borrow(bookId);

                Member member = memberDao.findById(memberId);
                if (member == null) {
                    throw new NotFoundException("Member not found with id: " + memberId);
                }

                Book book = bookService.findById(bookId);
                LocalDate today = LocalDate.now(clock);
                LocalDate dueDate = LoanPolicy.dueDate(today);

                Loan loan = new Loan(member, book, today, dueDate);
                loanDao.create(loan);
                return loan;
            });
        } catch (SQLException e) {
            throw new RuntimeException("Database error during checkout", e);
        }
    }

    public Loan returnLoan(int loanId) {
        Loan loan = loanDao.findById(loanId);
        if (loan == null) {
            throw new NotFoundException("Loan not found with id: " + loanId);
        }
        if (loan.isReturned()) {
            throw new IllegalStateException("Loan is already returned: " + loanId);
        }

        try {
            return loanDao.transaction(() -> {
                loan.setReturned(true);
                LocalDate today = LocalDate.now(clock);
                loan.setReturnDate(today);

                LocalDate due = loan.getDueDate();
                if (today.isAfter(due)) {
                    long daysOverdue = ChronoUnit.DAYS.between(due, today);
                    loan.setOverdueFee(daysOverdue * LoanPolicy.FEE_PER_DAY);
                }

                loanDao.update(loan);
                bookService.returnCopy(loan.getBook().getId());
                return loan;
            });
        } catch (SQLException e) {
            throw new RuntimeException("Database error during return", e);
        }
    }

    public double accruedFee(Loan loan) {
        if (loan == null) {
            return 0.0;
        }
        if (loan.isReturned()) {
            return loan.getOverdueFee();
        }
        LocalDate today = LocalDate.now(clock);
        if (today.isAfter(loan.getDueDate())) {
            long days = ChronoUnit.DAYS.between(loan.getDueDate(), today);
            return days * LoanPolicy.FEE_PER_DAY;
        }
        return 0.0;
    }

    public Loan withAccruedFees(Loan loan) {
        if (loan != null && !loan.isReturned()) {
            loan.setOverdueFee(accruedFee(loan));
        }
        return loan;
    }

    public List<Loan> withAccruedFees(List<Loan> loans) {
        if (loans != null) {
            loans.forEach(this::withAccruedFees);
        }
        return loans;
    }

    public List<Loan> findAll() {
        return withAccruedFees(loanDao.findAll());
    }

    public List<Loan> overdueLoans() {
        LocalDate today = LocalDate.now(clock);
        List<Loan> overdue = loanDao.findAll().stream()
                .filter(loan -> !loan.isReturned() && loan.getDueDate().isBefore(today))
                .toList();
        return withAccruedFees(overdue);
    }
}
