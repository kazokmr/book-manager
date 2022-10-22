package com.book.manager.infrastructure.database.repository

import com.book.manager.domain.model.Account
import com.book.manager.domain.repository.AccountRepository
import com.book.manager.infrastructure.database.mapper.AccountDynamicSqlSupport
import com.book.manager.infrastructure.database.mapper.AccountMapper
import com.book.manager.infrastructure.database.mapper.selectByPrimaryKey
import com.book.manager.infrastructure.database.mapper.selectOne
import org.springframework.stereotype.Repository
import com.book.manager.infrastructure.database.record.Account as RecordAccount

@Repository
class AccountRepositoryImpl(private val mapper: AccountMapper) : AccountRepository {

    override fun findByEmail(email: String): Account? {
        val record = mapper.selectOne {
            where { AccountDynamicSqlSupport.account.email isEqualTo email }
        }
        return record?.let { toModel(it) }
    }

    override fun findById(id: Long): Account? {
        return mapper.selectByPrimaryKey(id)?.let { toModel(it) }
    }

    private fun toModel(record: RecordAccount): Account {
        return Account(
            record.id!!,
            record.email!!,
            record.password!!,
            record.name!!,
            record.roleType!!
        )
    }
}