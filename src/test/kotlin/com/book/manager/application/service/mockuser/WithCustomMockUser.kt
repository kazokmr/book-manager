package com.book.manager.application.service.mockuser

import com.book.manager.application.service.security.BookManagerUserDetails
import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

/**
 * 認証済みテストユーザーを利用するためのカスタムMock
 * 各プロパティはデフォルト値なのでアノテーションパラメータで変更可能。
 * FactoryクラスにWithMockCustomUserSecurityContextFactoryを指定して、カスタムAuthenticationプリンシパル(BookManagerUserDetails)を
 * テスト実行時のContext領域にセットする
 */
@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory::class)
annotation class WithCustomMockUser(
    val id: Long = 1000L,
    val email: String = "test@example.com",
    val pass: String = "pass",
    val username: String = "test",
    val roleType: RoleType = RoleType.USER
)

class WithMockCustomUserSecurityContextFactory : WithSecurityContextFactory<WithCustomMockUser> {
    override fun createSecurityContext(user: WithCustomMockUser): SecurityContext {
        val account = Account(user.id, user.email, user.pass, user.username, user.roleType)
        val principal = BookManagerUserDetails(account)
        val auth = UsernamePasswordAuthenticationToken(principal, principal.password, principal.authorities)
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = auth
        return context
    }

}
