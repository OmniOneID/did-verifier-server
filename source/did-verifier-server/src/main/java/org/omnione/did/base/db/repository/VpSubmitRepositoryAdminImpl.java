package org.omnione.did.base.db.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import org.omnione.did.base.db.constant.TransactionStatus;
import org.omnione.did.base.db.domain.QTransaction;
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
        QTransaction qTransaction = QTransaction.transaction;

        // Join with Transaction entity to access status
        BooleanExpression predicate = qVpSubmit.transactionId.eq(qTransaction.id);

        // Add additional filters based on search parameters
        BooleanExpression searchPredicate = buildPredicate(searchKey, searchValue, qTransaction);
        if (searchPredicate != null) {
            predicate = predicate.and(searchPredicate);
        }

        // Get total count with join
        long total = queryFactory
                .select(qVpSubmit.count())
                .from(qVpSubmit)
                .join(qTransaction).on(qVpSubmit.transactionId.eq(qTransaction.id))
                .where(predicate)
                .fetchOne();

        // Get results with join
        List<VpSubmit> results = queryFactory
                .selectFrom(qVpSubmit)
                .join(qTransaction).on(qVpSubmit.transactionId.eq(qTransaction.id))
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable, qVpSubmit, qTransaction))
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    public BooleanExpression buildPredicate(String searchKey, String searchValue, QTransaction qTransaction) {
        if (searchKey == null || searchValue == null || searchValue.isEmpty()) {
            return null;
        }

        switch (searchKey) {
            case "transaction":
                return qTransaction.id.eq(Long.valueOf(searchValue));
            case "status":
                // Filter by transaction status from the Transaction entity
                return qTransaction.status.eq(TransactionStatus.valueOf(searchValue));
            default:
                return null;
        }
    }

    public OrderSpecifier<?>[] getOrderSpecifier(Pageable pageable, QVpSubmit qVpSubmit, QTransaction qTransaction) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (!pageable.getSort().isSorted()) {
            // Default sort by created date descending
            orders.add(new OrderSpecifier<>(Order.DESC, qVpSubmit.createdAt));
        } else {
            // Process the sort specifications from the pageable
            for (Sort.Order order : pageable.getSort()) {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                switch (order.getProperty()) {
                    case "createdAt":
                        orders.add(new OrderSpecifier<>(direction, qVpSubmit.createdAt));
                        break;
                    case "transactionStatus":
                        orders.add(new OrderSpecifier<>(direction, qTransaction.status));
                        break;
                    default:
                        // Default to sort by created date
                        orders.add(new OrderSpecifier<>(direction, qVpSubmit.createdAt));
                        break;
                }
            }
        }

        // If no sort orders were added, add a default one
        if (orders.isEmpty()) {
            orders.add(new OrderSpecifier<>(Order.DESC, qVpSubmit.createdAt));
        }

        return orders.toArray(new OrderSpecifier[0]);
    }
}