package cl.ucn.disc.arqsist.library.dao;
import cl.ucn.disc.arqsist.library.model.Member;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;
import java.util.List;

public final class MemberDao extends BaseDao<Member> {
    public MemberDao(ConnectionSource connectionSource) {
        super(connectionSource, Member.class);
    }

    public List<Member> searchByName(String query) {
        try {
            return dao.queryBuilder()
                    .orderBy("id", true)
                    .where()
                    .raw("instr(lower(name), lower(?)) > 0", new SelectArg(query))
                    .query();
        } catch (Exception e) {
            throw new RuntimeException("Error searching members by name", e);
        }
    }
}
