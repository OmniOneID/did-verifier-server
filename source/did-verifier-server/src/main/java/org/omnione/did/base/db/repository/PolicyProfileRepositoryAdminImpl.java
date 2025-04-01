package org.omnione.did.base.db.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.omnione.did.base.db.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PolicyProfileRepositoryAdminImpl implements PolicyProfileRepositoryAdmin {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PolicyProfile> searchPolicyProfileList(String searchKey, String searchValue, Pageable pageable) {

        QPolicyProfile qPolicyProfile = QPolicyProfile.policyProfile;
        BooleanExpression predicate = buildPredicate(searchKey, searchValue);

        long total = queryFactory
                .select(qPolicyProfile.count())
                .from(qPolicyProfile)
                .where(predicate)
                .fetchOne();

        List<PolicyProfile> results = queryFactory
                .selectFrom(qPolicyProfile)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable, qPolicyProfile))
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }
    private OrderSpecifier<?>[]  getOrderSpecifier(Pageable pageable, QPolicyProfile qPolicyProfile) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (!pageable.getSort().isSorted()) {
            orders.add(new OrderSpecifier<>(Order.ASC, qPolicyProfile.createdAt));
        }

        for (Sort.Order order: pageable.getSort()) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;

            switch (order.getProperty()) {
                case "title":
                    orders.add(new OrderSpecifier<>(direction, qPolicyProfile.title));
                    break;
                default:
                    orders.add(new OrderSpecifier<>(Order.ASC, qPolicyProfile.createdAt));
                    break;
            }
        }

        return orders.toArray(new OrderSpecifier[0]);
    }
    private BooleanExpression buildPredicate(String searchKey, String searchValue) {
        QPolicyProfile qPolicyProfile = QPolicyProfile.policyProfile;
        BooleanExpression predicate = Expressions.asBoolean(true).isTrue();
        if (searchKey != null && searchValue != null && !searchValue.isEmpty()) {
            switch (searchKey) {
                case "title":
                    predicate = predicate.and(qPolicyProfile.title.eq(searchValue));
                    break;
                case "authType":
                    predicate = predicate.and(qPolicyProfile.policyProfileId.eq(searchValue));
                    break;
                default:
                    predicate = predicate.and(Expressions.FALSE);
            }
        }

        return predicate;
    }
}
