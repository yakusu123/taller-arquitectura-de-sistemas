package cl.ucn.disc.arqsist.library.service;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.model.Member;
import java.util.List;

public final class MemberService {
    private final MemberDao memberDao;

    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public Member register(Member member) {
        memberDao.create(member);
        return member;
    }

    public List<Member> findAll() {
        return memberDao.findAll();
    }
    public Member findById(int id) {
        // ORMLite returns null when the row does not exist.
        Member member = memberDao.findById(id);
        if (member == null) {
            throw new NotFoundException("Member not found: " + id);
        }
        return member;
    }
}
