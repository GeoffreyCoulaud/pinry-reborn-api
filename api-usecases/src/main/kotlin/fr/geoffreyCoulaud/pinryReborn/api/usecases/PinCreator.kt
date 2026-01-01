package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Login
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.LoginError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinCreationBadLoginError
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID.randomUUID

@ApplicationScoped
class PinCreator(
    private val userAuthenticator: UserAuthenticator,
    private val tagCreator: TagCreator,
    private val pinRepository: PinRepositoryInterface,
) {
    fun createPin(
        login: Login,
        sourceUrl: String,
        mediaUrl: String,
        description: String,
        tags: List<String>,
    ): Pin {
        val user =
            try {
                userAuthenticator.authenticate(login)
            } catch (error: LoginError) {
                throw PinCreationBadLoginError(cause = error)
            }
        val tags = tags.map { tagCreator.findOrCreate(it) }
        val pin =
            Pin(
                id = randomUUID(),
                author = user,
                sourceUrl = sourceUrl,
                mediaUrl = mediaUrl,
                description = description,
                tags = tags,
            )
        return pinRepository.savePin(pin)
    }
}
