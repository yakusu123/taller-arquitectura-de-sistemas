package cl.ucn.disc.arqsist.library.grpc;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.service.MemberService;
import cl.ucn.disc.arqsist.library.service.NotFoundException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.List;

public final class MemberGrpcService extends MemberServiceGrpc.MemberServiceImplBase {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 50051;
    private final MemberService memberService;
    public MemberGrpcService(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public void registerMember(RegisterMemberRequest request, StreamObserver<MemberMessage> observer) {
        try {
            Member member = memberService.register(new Member(request.getName(), request.getEmail()));

            observer.onNext(toMessage(member));
            observer.onCompleted();
        } catch (RuntimeException e) {
            observer.onError(toError(e));
        }
    }

    @Override
    public void listMembers(ListMembersRequest request, StreamObserver<ListMembersReply> observer) {
        try {
            List<MemberMessage> members = memberService.findAll().stream()
                    .map(MemberGrpcService::toMessage)
                    .toList();
            observer.onNext(ListMembersReply.newBuilder().addAllMembers(members).build());
            observer.onCompleted();
        } catch (RuntimeException e) {
            observer.onError(toError(e));
        }
    }

    @Override
    public void getMember(GetMemberRequest request, StreamObserver<MemberMessage> observer) {
        try {
            observer.onNext(toMessage(memberService.findById(request.getId())));
            observer.onCompleted();
        } catch (RuntimeException e) {
            observer.onError(toError(e));
        }
    }

    @Override
    public void searchMembers(SearchMembersRequest request, StreamObserver<ListMembersReply> observer) {
        try {
            List<MemberMessage> members = memberService.search(request.getQuery()).stream()
                    .map(MemberGrpcService::toMessage)
                    .toList();

            observer.onNext(ListMembersReply.newBuilder().addAllMembers(members).build());
            observer.onCompleted();
        } catch (RuntimeException e) {
            observer.onError(toError(e));
        }
    }

    private static StatusRuntimeException toError(RuntimeException e) {
        Status status = switch (e) {
            case NotFoundException x -> Status.NOT_FOUND;
            case IllegalArgumentException x -> Status.INVALID_ARGUMENT;
            case IllegalStateException x -> Status.FAILED_PRECONDITION;
            default -> Status.INTERNAL;
        };
        return status.withDescription(e.getMessage()).withCause(e).asRuntimeException();
    }

    private static MemberMessage toMessage(Member member) {
        return MemberMessage.newBuilder()
                .setId(member.getId())
                .setName(member.getName())
                .setEmail(member.getEmail())
                .build();
    }
}
