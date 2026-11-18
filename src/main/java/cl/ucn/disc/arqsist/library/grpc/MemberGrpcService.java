package cl.ucn.disc.arqsist.library.grpc;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.service.MemberService;
import cl.ucn.disc.arqsist.library.service.NotFoundException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;

public final class MemberGrpcService extends MemberServiceGrpc.MemberServiceImplBase {
    private final MemberService memberService;
    public MemberGrpcService(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public void registerMember(RegisterMemberRequest request, StreamObserver<MemberMessage> observer) {
        Member member = memberService.register(new Member(request.getName(), request.getEmail()));

        observer.onNext(toMessage(member));
        observer.onCompleted();
    }

    @Override
    public void listMembers(ListMembersRequest request, StreamObserver<ListMembersReply> observer) {

        List<MemberMessage> members = memberService.findAll().stream()
                .map(MemberGrpcService::toMessage)
                .toList();

        observer.onNext(ListMembersReply.newBuilder().addAllMembers(members).build());
        observer.onCompleted();
    }

    @Override
    public void getMember(GetMemberRequest request, StreamObserver<MemberMessage> observer) {
        try {
            observer.onNext(toMessage(memberService.findById(request.getId())));
            observer.onCompleted();
        } catch (NotFoundException e) {
            observer.onError(Status.NOT_FOUND.withDescription(e.getMessage())
                    .withCause(e).asRuntimeException());
        }
    }

    private static MemberMessage toMessage(Member member) {
        return MemberMessage.newBuilder()
                .setId(member.getId())
                .setName(member.getName())
                .setEmail(member.getEmail())
                .build();
    }
}
