package com.ktb.lukas.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPostlike is a Querydsl query type for Postlike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPostlike extends EntityPathBase<Postlike> {

    private static final long serialVersionUID = 138643846L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPostlike postlike = new QPostlike("postlike");

    public final QBaseTime _super = new QBaseTime(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPost post;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QPostlike(String variable) {
        this(Postlike.class, forVariable(variable), INITS);
    }

    public QPostlike(Path<? extends Postlike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPostlike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPostlike(PathMetadata metadata, PathInits inits) {
        this(Postlike.class, metadata, inits);
    }

    public QPostlike(Class<? extends Postlike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.post = inits.isInitialized("post") ? new QPost(forProperty("post"), inits.get("post")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

