package cl.ucn.disc.arqsist.library.grpc;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.service.NotFoundException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class MemberClient implements AutoCloseable {
    private static final long DEADLINE_SECONDS = 5;
    private final ManagedChannel channel;
    private final MemberServiceGrpc.MemberServiceBlockingStub stub;
    public MemberClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
    }

    public MemberClient(ManagedChannel channel) {
        this.channel = channel;
        this.stub = MemberServiceGrpc.newBlockingStub(channel);
    }

    public Member register(Member member) {
        try {
            MemberMessage reply = stub.withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
                    .registerMember(RegisterMemberRequest.newBuilder()
                            .setName(member.getName())
                            .setEmail(member.getEmail())
                            .build());
            return toMember(reply);
        } catch (StatusRuntimeException e) {
            throw toException(e);
        }
    }

    public List<Member> findAll() {
        try {
            ListMembersReply reply = stub.withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
                    .listMembers(ListMembersRequest.getDefaultInstance());
            return reply.getMembersList().stream()
                    .map(MemberClient::toMember)
                    .toList();
        } catch (StatusRuntimeException e) {
            throw toException(e);
        }
    }

    public Member findById(int id) {
        try {
            MemberMessage reply = stub.withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
                    .getMember(GetMemberRequest.newBuilder().setId(id).build());
            return toMember(reply);
        } catch (StatusRuntimeException e) {
            throw toException(e);
        }
    }

    public List<Member> search(String query) {
        try {
            ListMembersReply reply = stub.withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
                    .searchMembers(SearchMembersRequest.newBuilder()
                            .setQuery(query)
                            .build());
            return reply.getMembersList().stream()
                    .map(MemberClient::toMember)
                    .toList();
        } catch (StatusRuntimeException e) {
            throw toException(e);
        }
    }

    @Override
    public void close() {
        channel.shutdown();
        try {
            if (!channel.awaitTermination(DEADLINE_SECONDS, TimeUnit.SECONDS)) {
                channel.shutdownNow();
            }
        } catch (InterruptedException e) {
            channel.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static RuntimeException toException(StatusRuntimeException e) {
        String description = e.getStatus().getDescription();
        return switch (e.getStatus().getCode()) {
            case NOT_FOUND -> new NotFoundException(description);
            case INVALID_ARGUMENT -> new IllegalArgumentException(description);
            case FAILED_PRECONDITION -> new IllegalStateException(description);
            default -> e;
        };
    }

    private static Member toMember(MemberMessage message) {
        Member member = new Member(message.getName(), message.getEmail());
        member.setId(message.getId());
        return member;
    }
}
