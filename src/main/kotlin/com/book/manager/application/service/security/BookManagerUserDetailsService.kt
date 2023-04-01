package com.book.manager.application.service.security

import com.book.manager.application.service.AuthenticationService
import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.node.MissingNode
import io.micrometer.observation.annotation.Observed
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.io.Serial

@Service
@Observed
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

    companion object {
        @Serial
        private const val serialVersionUID: Long = 3887265448650931817L
    }
}

// カスタムUserDetailsの情報をデシリアライズするためのMixin定義(org.springframework.security.jackson2.UserMixin を参考にした)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonDeserialize(using = BookManagerUserDeserializer::class)
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonIgnoreProperties(ignoreUnknown = true)
internal abstract class BookManagerUserMixin

// カスタムUserDetailsのデシリアライズ処理 (org.springframework.security.jackson2.UserDeserializer を参考にした)
class BookManagerUserDeserializer : JsonDeserializer<BookManagerUserDetails>() {
    override fun deserialize(jp: JsonParser?, ctxt: DeserializationContext?): BookManagerUserDetails {
        val mapper = jp?.codec as ObjectMapper
        val jsonNode: JsonNode = mapper.readTree(jp)

        val roleType = RoleType.valueOf(jsonNode.readJsonNode("roleType").asText())
        return BookManagerUserDetails(
            jsonNode.readJsonNode("id").asLong(),
            jsonNode.readJsonNode("email").asText(),
            jsonNode.readJsonNode("password").asText(),
            jsonNode.readJsonNode("name").asText(),
            roleType
        )
    }

    private fun JsonNode.readJsonNode(field: String): JsonNode =
        when {
            has(field) -> get(field)
            else -> MissingNode.getInstance()
        }

}