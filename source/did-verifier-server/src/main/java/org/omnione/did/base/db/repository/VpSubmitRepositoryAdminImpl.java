package org.omnione.did.base.db.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import org.omnione.did.base.db.domain.QVpSubmit;
import org.omnione.did.base.db.domain.VpSubmit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class VpSubmitRepositoryAdminImpl implements VpSubmitRepositoryAdmin {
    private final JPAQueryFactory queryFactory;
    @Override
    public Page<VpSubmit> searchVpSubmitList(String searchKey, String searchValue, Pageable pageable) {
        QVpSubmit qVpSubmit = QVpSubmit.vpSubmit;
        BooleanExpression predicate = buildPredicate(searchKey, searchValue);

        long total = queryFactory
                .select(qVpSubmit.count())
                .from(qVpSubmit)
                .where(predicate)
                .fetchOne();
        List<VpSubmit> results = queryFactory
                .selectFrom(qVpSubmit)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable, qVpSubmit))
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    public BooleanExpression buildPredicate(String searchKey, String searchValue) {
        QVpSubmit qVpSubmit = QVpSubmit.vpSubmit;
        BooleanExpression predicate = Expressions.asBoolean(true).isTrue();
        if (searchKey != null && searchValue != null && !searchValue.isEmpty()) {
            switch (searchKey) {
                case "transactionId":
                    predicate = predicate.and(qVpSubmit.transactionId.eq(Long.valueOf(searchValue)));
                    break;
                default:
                    predicate = predicate.and(Expressions.FALSE);
            }
        }

        return predicate;
    }

    public OrderSpecifier<?>[] getOrderSpecifier(Pageable pageable, QVpSubmit qVpSubmit) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (!pageable.getSort().isSorted()) {
            orders.add(new OrderSpecifier<>(Order.ASC, qVpSubmit.createdAt));
        }

        for (Sort.Order order : pageable.getSort()) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;

            order.getProperty();
            orders.add(new OrderSpecifier<>(Order.ASC, qVpSubmit.createdAt));
        }
        return orders.toArray(new OrderSpecifier[0]);
    }

}
