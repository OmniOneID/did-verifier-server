package org.omnione.did.base.db.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.omnione.did.base.db.domain.QVpFilter;
import org.omnione.did.base.db.domain.VpFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static org.omnione.did.base.db.domain.QVpFilter.vpFilter;

@Repository
@RequiredArgsConstructor
public class VpFilterRepositoryAdminImpl implements VpFilterRepositoryAdmin {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<VpFilter> searchVpFilterList(String searchKey, String searchValue, Pageable pageable) {
        QVpFilter qVpFilter = vpFilter;

        BooleanExpression predicate = buildPredicate(searchKey, searchValue);

        long total = queryFactory
                .select(qVpFilter.count())
                .from(qVpFilter)
                .where(predicate)
                .fetchOne();

        List<VpFilter> results = queryFactory
                .selectFrom(qVpFilter)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable, qVpFilter))
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    private OrderSpecifier<?>[]  getOrderSpecifier(Pageable pageable, QVpFilter qVpFilter) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (!pageable.getSort().isSorted()) {
            orders.add(new OrderSpecifier<>(Order.ASC, qVpFilter.createdAt));
        }

        for (Sort.Order order: pageable.getSort()) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;

            switch (order.getProperty()) {
                case "title":
                    orders.add(new OrderSpecifier<>(direction, qVpFilter.title));
                    break;
                default:
                    orders.add(new OrderSpecifier<>(Order.ASC, qVpFilter.createdAt));
                    break;
            }
        }

        return orders.toArray(new OrderSpecifier[0]);
    }

    private BooleanExpression buildPredicate(String searchKey, String searchValue) {
        QVpFilter qVpFilter = vpFilter;
        BooleanExpression predicate = Expressions.asBoolean(true).isTrue();
        if (searchKey != null && searchValue != null && !searchValue.isEmpty()) {
            switch (searchKey) {
                case "title":
                    predicate = predicate.and(qVpFilter.title.eq(searchValue));
                    break;
                case "type":
                    predicate = predicate.and(qVpFilter.type.eq(searchValue));
                    break;
                default:
                    predicate = predicate.and(Expressions.FALSE);
            }
        }

        return predicate;
    }
}
