package cl.ucn.disc.arqsist.library;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.grpc.MemberClient;
import cl.ucn.disc.arqsist.library.grpc.MemberGrpcService;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.service.MemberService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MemberSearchTest {

    private Server server;
    private ManagedChannel channel;
    private MemberClient client;

    @BeforeEach
    void setUp() throws Exception {
        Database db = new Database("jdbc:sqlite::memory:");
        MemberService memberService = new MemberService(new MemberDao(db.connectionSource()));
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(new MemberGrpcService(memberService))
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
        client = new MemberClient(channel);
        client.register(new Member("Ada Lovelace", "ada@example.com"));
        client.register(new Member("Grace Hopper", "grace@example.com"));
        client.register(new Member("Alan Turing", "alan@example.com"));
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.close();
        }
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    void searchSubstringMatch() {
        List<Member> results = client.search("ada");
        assertEquals(1, results.size());
        assertEquals("Ada Lovelace", results.get(0).getName());
    }

    @Test
    void searchCaseInsensitive() {
        List<Member> results = client.search("ADA");
        assertEquals(1, results.size());
        assertEquals("Ada Lovelace", results.get(0).getName());
    }

    @Test
    void searchTrimWhitespace() {
        List<Member> results = client.search(" ada ");
        assertEquals(1, results.size());
        assertEquals("Ada Lovelace", results.get(0).getName());
    }

    @Test
    void searchMatchesMultipleOrderedById() {
        List<Member> results = client.search("a");
        assertEquals(3, results.size());
        assertEquals("Ada Lovelace", results.get(0).getName());
        assertEquals("Grace Hopper", results.get(1).getName());
        assertEquals("Alan Turing", results.get(2).getName());
    }

    @Test
    void searchNoMatchReturnsEmptyList() {
        List<Member> results = client.search("zzz");
        assertTrue(results.isEmpty());
    }

    @Test
    void searchWildcardCharactersAreLiteral() {
        assertTrue(client.search("%").isEmpty());
        assertTrue(client.search("_").isEmpty());
    }

    @Test
    void searchBlankQueryThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> client.search(""));
        assertThrows(IllegalArgumentException.class, () -> client.search("   "));
    }
}
