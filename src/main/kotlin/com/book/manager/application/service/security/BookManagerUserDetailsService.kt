package com.book.manager.application.service.security

import com.book.manager.application.service.AuthenticationService
import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

class BookManagerUserDetailsService(private val authenticationService: AuthenticationService) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails =
        when (val account = authenticationService.findAccount(username)) {
            null -> throw UsernameNotFoundException("無効なユーザー名です $username")
            else -> BookManagerUserDetails(account)
        }
}

data class BookManagerUserDetails(
    val id: Long,
    val email: String,
    val pass: String,
    val name: String,
    val roleType: RoleType
) : UserDetails {
    constructor(account: Account) : this(account.id, account.email, account.password, account.name, account.roleType)

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return AuthorityUtils.createAuthorityList(this.roleType.toString())
    }

    override fun getPassword(): String {
        return this.pass
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun getUsername(): String {
        return this.email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }
}