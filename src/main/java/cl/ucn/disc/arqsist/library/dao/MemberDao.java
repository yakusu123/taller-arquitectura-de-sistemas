/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.dao;

import cl.ucn.disc.arqsist.library.model.Member;
import com.j256.ormlite.support.ConnectionSource;

public final class MemberDao extends BaseDao<Member> {

    public MemberDao(ConnectionSource connectionSource) {
        super(connectionSource, Member.class);
    }
}
