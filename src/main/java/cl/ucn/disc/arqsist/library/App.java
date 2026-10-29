package cl.ucn.disc.arqsist.library;
import cl.ucn.disc.arqsist.library.controller.BookController;
import cl.ucn.disc.arqsist.library.controller.LoanController;
import cl.ucn.disc.arqsist.library.controller.MemberController;
import cl.ucn.disc.arqsist.library.controller.ReservationController;
import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.service.*;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import java.time.Clock;
import java.time.ZoneId;

public final class App {

    public static void main(String[] args) throws Exception {
        Database db = new Database("jdbc:sqlite:database.db");
        db.seedIfEmpty();
        Clock clock = Clock.system(ZoneId.of("America/Santiago"));
        BookDao bookDao = new BookDao(db.connectionSource());
        MemberDao memberDao = new MemberDao(db.connectionSource());
        LoanDao loanDao = new LoanDao(db.connectionSource());
        ReservationDao reservationDao = new ReservationDao(db.connectionSource());
        BookService bookService = new BookService(bookDao);
        MemberService memberService = new MemberService(memberDao);
        LoanService loanService = new LoanService(loanDao, memberDao, bookService, clock);
        ReservationService reservationService = new ReservationService(reservationDao, memberDao, loanDao, bookService, clock);
        BookController bookController = new BookController(bookService);
        MemberController memberController = new MemberController(memberService);
        LoanController loanController = new LoanController(loanService);
        ReservationController reservationController = new ReservationController(reservationService);

        Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
            });
            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            }));
            bookController.register(config);
            memberController.register(config);
            loanController.register(config);
            reservationController.register(config);

            config.routes.exception(NotFoundException.class, (e, ctx) -> {
                ctx.status(404);
                ctx.result(e.getMessage() != null ? e.getMessage() : "Not Found");
            });
            config.routes.exception(IllegalArgumentException.class, (e, ctx) -> {
                ctx.status(400);
                ctx.result(e.getMessage() != null ? e.getMessage() : "Bad Request");
            });
            config.routes.exception(IllegalStateException.class, (e, ctx) -> {
                ctx.status(409);
                ctx.result(e.getMessage() != null ? e.getMessage() : "Conflict");
            });
        }).start(7070);
    }
}
