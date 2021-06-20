package com.book.manager.presentation.config

import com.book.manager.application.service.AuthenticationService
import com.book.manager.application.service.security.BookManagerUserDetailsService
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class SecurityTestConfiguration(private val authenticationService: AuthenticationService) {

    /*  このBean定義は、Controllerクラスのテストで、認証済みユーザーをBookManagerUserDetailsServiceクラスから取得する
        場合に必要となる。
        このBeanを用意しておけば、使いたいテストメソッドで以下のようにアノテーションを追加すれば良い。
        @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "bookManagerUserDetailsService")
        但し、指定したユーザーアカウントがDBに登録してある必要があり、テスト実行時にDBに接続もしておかないといけない。
        なので、@WithSecurityContext を利用したカスタムアノテーションを使う方がテストユーザーを柔軟に作れる
     */
    @Bean
    fun bookManagerUserDetailsService(): BookManagerUserDetailsService {
        return BookManagerUserDetailsService(authenticationService)
    }
}