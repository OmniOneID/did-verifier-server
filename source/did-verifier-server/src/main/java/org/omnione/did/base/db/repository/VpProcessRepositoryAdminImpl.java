package org.omnione.did.base.db.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import org.omnione.did.base.db.domain.QVpProcess;
import org.omnione.did.base.db.domain.VpProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class VpProcessRepositoryAdminImpl implements VpProcessRepositoryAdmin {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<VpProcess> searchVpProcessList(String searchKey, String searchValue, Pageable pageable) {
        QVpProcess qVpProcess = QVpProcess.vpProcess;
        BooleanExpression predicate = buildPredicate(searchKey, searchValue);

        long total = queryFactory
                .select(qVpProcess.count())
                .from(qVpProcess)
                .where(predicate)
                .fetchOne();

        List<VpProcess> results = queryFactory
                .selectFrom(qVpProcess)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable, qVpProcess))
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }
    private OrderSpecifier<?>[]  getOrderSpecifier(Pageable pageable, QVpProcess qVpProcess) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (!pageable.getSort().isSorted()) {
            orders.add(new OrderSpecifier<>(Order.ASC, qVpProcess.createdAt));
        }

        for (Sort.Order order: pageable.getSort()) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;

            switch (order.getProperty()) {
                case "title":
                    orders.add(new OrderSpecifier<>(direction, qVpProcess.title));
                    break;
                default:
                    orders.add(new OrderSpecifier<>(Order.ASC, qVpProcess.createdAt));
                    break;
            }
        }

        return orders.toArray(new OrderSpecifier[0]);
    }
    private BooleanExpression buildPredicate(String searchKey, String searchValue) {
        QVpProcess qVpProcess = QVpProcess.vpProcess;
        BooleanExpression predicate = Expressions.asBoolean(true).isTrue();
        if (searchKey != null && searchValue != null && !searchValue.isEmpty()) {
            switch (searchKey) {
                case "title":
                    predicate = predicate.and(qVpProcess.title.eq(searchValue));
                    break;
                case "authType":
                    predicate = predicate.and(qVpProcess.authType.eq(Integer.valueOf(searchValue)));
                    break;
                default:
                    predicate = predicate.and(Expressions.FALSE);
            }
        }

        return predicate;
    }
}
