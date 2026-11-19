package cl.ucn.disc.arqsist.library;
import cl.ucn.disc.arqsist.library.grpc.MemberClient;
import cl.ucn.disc.arqsist.library.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MemberClientApp {
    private static final Logger log = LoggerFactory.getLogger(MemberClientApp.class);
    private static final String GRPC_HOST = "127.0.0.1";
    private static final int GRPC_PORT = 50051;

    public static void main(String[] args) {
        try (MemberClient client = new MemberClient(GRPC_HOST, GRPC_PORT)) {

            for (Member member : client.findAll()) {
                log.info("member {}: {} <{}>", member.getId(), member.getName(), member.getEmail());
            }

            for (String arg : args) {
                Member member = client.findById(Integer.parseInt(arg));
                log.info("member {}: {} <{}>", member.getId(), member.getName(), member.getEmail());
            }
        }
    }
}
