package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.security

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Login.BasicAuthLogin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.usecases.UserAuthenticator
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.UserAuthenticationError
import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.IdentityProvider
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest
import io.quarkus.security.runtime.QuarkusSecurityIdentity
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class BasicAuthIdentityProvider(
    private val userAuthenticator: UserAuthenticator,
) : IdentityProvider<UsernamePasswordAuthenticationRequest> {
    @Suppress("MaxLineLength")
    override fun getRequestType(): Class<UsernamePasswordAuthenticationRequest> =
        UsernamePasswordAuthenticationRequest::class.java

    override fun authenticate(
        request: UsernamePasswordAuthenticationRequest,
        context: AuthenticationRequestContext,
    ): Uni<SecurityIdentity> =
        context.runBlocking {
            try {
                val login = BasicAuthLogin(
                    userName = request.username,
                    password = String(request.password.password),
                )
                val user = userAuthenticator.authenticate(login)
                buildSecurityIdentity(user)
            } catch (e: UserAuthenticationError) {
                throw AuthenticationFailedException("Invalid username or password", e)
            }
        }

    private fun buildSecurityIdentity(user: User): SecurityIdentity =
        QuarkusSecurityIdentity
            .builder()
            .setPrincipal { user.name }
            .addAttribute("userId", user.id)
            .addAttribute("user", user)
            .build()
}
