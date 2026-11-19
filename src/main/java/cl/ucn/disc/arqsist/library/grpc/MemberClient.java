package cl.ucn.disc.arqsist.library.grpc;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.service.NotFoundException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class MemberClient implements AutoCloseable {
    private static final long DEADLINE_SECONDS = 5;
    private final ManagedChannel channel;
    private final MemberServiceGrpc.MemberServiceBlockingStub stub;

    public MemberClient(String host, int port) {
        // usePlaintext disables TLS: this lab server has no certificate.
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
    }

    public MemberClient(ManagedChannel channel) {
        // One channel serves many calls. The stub reuses it.
        this.channel = channel;
        this.stub = MemberServiceGrpc.newBlockingStub(channel);
    }

    public Member register(Member member) {
        // The deadline frees the caller when the server does not answer.
        MemberMessage reply = stub.withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
                .registerMember(RegisterMemberRequest.newBuilder()
                        .setName(member.getName())
                        .setEmail(member.getEmail())
                        .build());
        return toMember(reply);
    }

    public List<Member> findAll() {
        // The request has no fields: use the shared default instance.
        ListMembersReply reply = stub.withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
                .listMembers(ListMembersRequest.getDefaultInstance());
        return reply.getMembersList().stream()
                .map(MemberClient::toMember)
                .toList();
    }

    public Member findById(int id) {
        try {
            MemberMessage reply = stub.withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
                    .getMember(GetMemberRequest.newBuilder().setId(id).build());
            return toMember(reply);
        } catch (StatusRuntimeException e) {
            // The server reports a missing member as a status code.
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new NotFoundException(e.getStatus().getDescription());
            }
            throw e;
        }
    }

    @Override
    public void close() {
        // shutdown releases the channel resources; it does not wait for the calls.
        channel.shutdown();
    }

    private static Member toMember(MemberMessage message) {
        // The domain entity has a constructor with the data and a setter for the id.
        Member member = new Member(message.getName(), message.getEmail());
        // Restore the identifier that the database generated.
        member.setId(message.getId());
        return member;
    }
}
